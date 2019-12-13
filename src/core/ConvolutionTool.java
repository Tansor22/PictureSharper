package core;

import java.util.Comparator;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

public abstract class ConvolutionTool {
    protected BiFunction<Integer, Float, Number> func = (integer, aFloat) -> (integer * aFloat);
    // скорость упала нереально
    protected BinaryOperator<Number> maxBy = BinaryOperator.maxBy(Comparator.comparingInt(Number::intValue));
    protected BinaryOperator<Number> reducer = (one, another) -> one.intValue() + another.intValue();
    protected Kernel kernel = Kernel.INCREASE_SHARPNESS_3x3;
    protected int[] pixels;
    protected int[] temp;
    protected int gap;
    protected int width, height, dimension, tmpH, tmpW;
    protected int[] tmpRCanal;
    protected int[] tmpGCanal;
    protected int[] tmpBCanal;

    public ConvolutionTool(int width, int height, int[] src, Properties conf) {
        this.width = width;
        this.height = height;
        this.dimension = width * height;
        pixels = new int[dimension];
        gap = (kernel.getSize() / 2);
        tmpH = height + (2 * gap);
        tmpW = width + (2 * gap);
        // creating temp imagePixels
        FillType ft = FillType.BORDER;
        temp = ft.setBounds(src, width, gap);
        prepareTempCanals();
    }

    protected void prepareTempCanals() {
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

    protected int[] getRGB(int imagePixels) {
        int[] RGB = new int[3];
        RGB[0] = (imagePixels & 0x00FF0000) >> 16;
        RGB[1] = (imagePixels & 0x0000FF00) >> 8;
        RGB[2] = (imagePixels & 0x000000FF);
        return RGB;
    }

    protected int buildRGB(int r, int g, int b) {
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public abstract int[] process();

}
