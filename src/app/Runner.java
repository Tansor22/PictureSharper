package app;

import core.ConvolutionTool;
import core.ParallelConvolution;
import core.SequentialConvolution;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import utils.Helper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

public class Runner {
    private int[] imagePixels;
    private int width, height;
    private boolean needMeasure = true;
    private ConvolutionTool s;


    public static void main(String[] args) {
       // Kostil
        JFXPanel jfxPanel = new JFXPanel();
        Runtime.getRuntime().addShutdownHook(new Thread(Platform::exit));

        Runner r = new Runner();
        r.setup();
        r.go();
        System.exit(0);
    }

    private void setup() {
        String pathToImage = "src/resources/img/im2.jpg";
        // works
        //pathToImage = System.getProperty("user.dir") + "/src/resources/img/im4.jpg";
        // also works
        //pathToImage = Paths.get("", "src/resources/img/im4.jpg").toFile().getAbsolutePath();
        // also works IF THERE IS / BEFORE SRC THEN DOES NOT
        //File imageFile = Paths.get("src/resources/img/im4.jpg").toFile();
        //ImageView imageView = (ImageView) scene.lookup("#imageView");
        //imageView.setImage(getImage(pathToImage, false));
        getImage(pathToImage, false);
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("app.properties"));
        } catch (IOException e) {
            Helper.showAlert("Не удалось найти файл app.properties. Будут использованы настройки по умоланию.");
        }
    }

    private Image getImage(String pathToImage, boolean debug) {

        Image image = new Image("file:" + Paths.get(pathToImage).toString());
        // FileNotFound => getPixelReader = null
        if (debug) {
            width = 4;
            height = 3;
            imagePixels = new int[height * width];
        } else {
            width = (int) image.getWidth();
            height = (int) image.getHeight();
            imagePixels = new int[height * width];
        }
        PixelReader reader = image.getPixelReader();
        Optional.ofNullable(reader).ifPresent(px -> px.getPixels(0, 0, width, height,
                PixelFormat.getIntArgbInstance(), imagePixels, 0, width));
        return image;
    }

    public void go() {
        long start = 0L;
        s = new SequentialConvolution(width, height, imagePixels, new Properties());
        if (needMeasure) {
            start = System.currentTimeMillis();
        }

        int[] processed = s.process();

        if (needMeasure) {
            long time = System.currentTimeMillis() - start;
            System.out.println("Sequential performing: ");
            System.out.println("Dimension of the image: " + width + " x " + height);
            System.out.println(String.format("Sequentially time(ms) = %d (%.2f sec)", time, toFixed(time / 1_000d, 2)));
        }
        write(processed, "jpg", new File("./processedSeq.jpg"));

        if (needMeasure) {
            start = System.currentTimeMillis();
        }

        s = new ParallelConvolution(width, height, imagePixels, new Properties());
        processed = s.process();

        if (needMeasure) {
            long time = System.currentTimeMillis() - start;
            System.out.println(String.format("Parallel performing with cores %d and %s mode: ",
                    Runtime.getRuntime().availableProcessors(), ((ParallelConvolution) s).getMode()));
            System.out.println("Dimension of the image: " + width + " x " + height);
            System.out.println(String.format("Parallel time(ms) = %d (%.2f sec)", time, toFixed(time / 1_000d, 2)));
        }
        write(processed, "jpg", new File("./processedParall.jpg"));
    }

    private static double toFixed(double d, int digits) {
        double magicNumber = Math.pow(10, digits);
        return ((long) (d * magicNumber)) / magicNumber;
    }

    private void write(int[] pixels, String ext, File file) {
        try {
            BufferedImage img = new BufferedImage(
                    width, height, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < height; y++)
                for (int x = 0; x < width; x++)
                    img.setRGB(x, y, pixels[y * width + x]);
            ImageIO.write(img, ext, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
