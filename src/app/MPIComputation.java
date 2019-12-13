package app;

import core.FillType;
import core.Kernel;
import javafx.embed.swing.JFXPanel;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import mpi.MPI;
import utils.MPIUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

import static utils.MPIUtils.*;

public class MPIComputation {
    protected static Kernel kernel = Kernel.INCREASE_SHARPNESS_5x5;
    protected static BiFunction<Integer, Float, Number> func = (integer, aFloat) -> (integer * aFloat);
    protected static BinaryOperator<Number> reducer = (one, another) -> one.intValue() + another.intValue();
    protected static int[] pixels = new int[10_000_000];
    protected static int[] temp;
    protected static int[] gap = new int[1];
    protected static int[] batchSize = new int[1];
    protected static int[] width = new int[1], height = new int[1], dimension = new int[1];
    protected static int[] tmpRCanal = new int[10_000_000];
    protected static int[] tmpGCanal = new int[10_000_000];
    protected static int[] tmpBCanal = new int[10_000_000];
    protected static boolean debug = false;

    public static void main(String[] args) {
        MPI.Init(args);

        // preparing
        if (isMaster()) {
            prepare();
        }

        // shared vars

        // sending batch var
        broadcast(DataType.INT, batchSize, 0, 1);

        // sending width var
        broadcast(DataType.INT, width, 0, 1);

        // sending dimension var
        broadcast(DataType.INT, dimension, 0, 1);

        // sending gap var
        broadcast(DataType.INT, gap, 0, 1);

        // calculating
        int tmpW = width[0] + (2 * gap[0]);
        int[] rCanal = new int[dimension[0]];
        int[] gCanal = new int[dimension[0]];
        int[] bCanal = new int[dimension[0]];
        int begin = Rank() * batchSize[0];
        int end = begin + batchSize[0];
        double start = jtime();

        System.out.printf("%d process is ready to process elements from %d to %d.\n", Rank(), begin, end);


        // chunk data to process
        scatter(DataType.INT, tmpRCanal, begin, batchSize[0]);

        scatter(DataType.INT, tmpGCanal, begin, batchSize[0]);

        scatter(DataType.INT, tmpBCanal, begin, batchSize[0]);


        // logic
        for (int i = begin; i < end; i++) {
            int tmpY = (i / width[0]) + gap[0];
            int tmpX = (i % width[0]) + gap[0];
            kernel.apply(tmpX, tmpY, tmpRCanal, tmpW, rCanal, func, reducer);
            kernel.apply(tmpX, tmpY, tmpGCanal, tmpW, gCanal, func, reducer);
            kernel.apply(tmpX, tmpY, tmpBCanal, tmpW, bCanal, func, reducer);
            pixels[i] = buildRGB(rCanal[i], gCanal[i], bCanal[i]);
        }

        System.out.printf("%d process has finished to process elements from %d to %d.\n", Rank(), begin, end);
        // gathering all chunks in root
        gather(DataType.INT, pixels, begin, batchSize[0]);

        double finish = MPIUtils.jtime();

        master(() -> {
            System.out.printf("Time (s.) = %.10f\n", (finish - start));
            write(pixels, "jpg", new File("./processedMPI.jpg"));
            return 0;
        });

        MPI.Finalize();
        System.exit(0);
    }

    protected static void prepareTempCanals() {
        int tmpH = height[0] + (2 * gap[0]);
        int tmpW = width[0] + (2 * gap[0]);
        tmpRCanal = new int[tmpW * tmpH];
        tmpGCanal = new int[tmpW * tmpH];
        tmpBCanal = new int[tmpW * tmpH];
        for (int i = 0; i < tmpH; i++)
            for (int j = 0; j < tmpW; j++) {
                int[] rgb = getRGB(temp[i * tmpW + j]);
                tmpRCanal[i * tmpW + j] = rgb[0];
                tmpGCanal[i * tmpW + j] = rgb[1];
                tmpBCanal[i * tmpW + j] = rgb[2];
            }
    }

    private static void prepare() {
        JFXPanel jfxPanel = new JFXPanel();
        String pathToImage = "src/resources/img/im5.jpg";
        Image image = new Image("file:" + Paths.get(pathToImage).toString());
        if (debug) {
            width[0] = 4;
            height[0] = 3;
        } else {
            width[0] = (int) image.getWidth();
            height[0] = (int) image.getHeight();
        }

        dimension[0] = width[0] * height[0];
        int[] imagePixels = new int[dimension[0]];
        pixels = new int[dimension[0]];

        // FileNotFound => getPixelReader = null
        PixelReader reader = image.getPixelReader();
        Optional.ofNullable(reader).ifPresent(px -> px.getPixels(0, 0, width[0], height[0],
                PixelFormat.getIntArgbInstance(), imagePixels, 0, width[0]));


        gap[0] = (kernel.getSize() / 2);
        // creating temp imagePixels
        FillType ft = FillType.BORDER;
        temp = ft.setBounds(imagePixels, width[0], gap[0]);
        prepareTempCanals();

        // batch resolving
        batchSize[0] = dimension[0] / Size();
        System.out.println("Dimension = " + dimension[0]);
        // sending batch to the rest processes
        System.out.printf("Master (Rank = %d) has sent batch value = %d to the rest of processes.\n", Rank(), batchSize[0]);

    }

    protected static int[] getRGB(int imagePixels) {
        int[] RGB = new int[3];
        RGB[0] = (imagePixels & 0x00FF0000) >> 16;
        RGB[1] = (imagePixels & 0x0000FF00) >> 8;
        RGB[2] = (imagePixels & 0x000000FF);
        return RGB;
    }

    protected static int buildRGB(int r, int g, int b) {
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static void write(int[] pixels, String ext, File file) {
        try {
            BufferedImage img = new BufferedImage(
                    width[0], height[0], BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < height[0]; y++)
                for (int x = 0; x < width[0]; x++)
                    img.setRGB(x, y, pixels[y * width[0] + x]);
            ImageIO.write(img, ext, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
