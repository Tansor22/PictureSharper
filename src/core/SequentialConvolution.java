package core;

import java.util.Properties;

public class SequentialConvolution extends ConvolutionTool {
    public SequentialConvolution(int width, int height, int[] src, Properties conf) {
        super(width, height, src, conf);
    }

    @Override
    public int[] process() {
        int[] rCanal = new int[dimension];
        int[] gCanal = new int[dimension];
        int[] bCanal = new int[dimension];
        for (int tmpY = gap; tmpY < height + gap; tmpY++) {
            for (int tmpX = gap; tmpX < width + gap; tmpX++) {
                kernel.apply(tmpX, tmpY, tmpRCanal, tmpW, rCanal, func, reducer);
                kernel.apply(tmpX, tmpY, tmpGCanal, tmpW, gCanal, func, reducer);
                kernel.apply(tmpX, tmpY, tmpBCanal, tmpW, bCanal, func, reducer);

                int x = tmpX - gap;
                int y = tmpY - gap;
                pixels[x + y * width] = buildRGB(rCanal[x + y * width], gCanal[x + y * width], bCanal[x + y * width]);
            }
        }
        return pixels;
    }
}
