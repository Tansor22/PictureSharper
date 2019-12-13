package core;


import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

public enum Kernel {
    DEBUG(new float[][]{
            {1.0f, 1.0f, 1.0f},
            {1.0f, 1.0f, 1.0f},
            {1.0f, 1.0f, 1.0f}
    }, 3),
    INCREASE_SHARPNESS_3x3(new float[][]{
            {-1.0f, -1.0f, -1.0f},
            {-1.0f, 9.0f, -1.0f},
            {-1.0f, -1.0f, -1.0f}
    }, 3),
    INCREASE_SHARPNESS_5x5(new float[][]{
            {-1.0f, -1.0f, -1.0f, -1.0f, -1.0f},
            {-1.0f, 1.0f, 1.0f, 1.0f, -1.0f},
            {-1.0f, 1.0f, 7.0f, 1.0f, -1.0f},
            {-1.0f, 1.0f, 1.0f, 1.0f, -1.0f},
            {-1.0f, 1.0f, 1.0f, 1.0f, -1.0f}
    }, 5),
    INCREASE_SHARPNESS_7x7(new float[][]{
            {-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f},
            {-1.0f, 9.0f, 9.0f, 9.0f, -1.0f, -1.0f, -1.0f},
            {-1.0f, 9.0f, 9.0f, 9.0f, -1.0f, -1.0f, -1.0f},
            {-1.0f, 9.0f, 9.0f, 9.0f, -1.0f, -1.0f, -1.0f},
            {-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f},
            {-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f},
            {-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f}
    }, 7),
    BLUR(new float[][]{
            {0.1111f, 0.1111f, 0.1111f},
            {0.1111f, 0.1111f, 0.1111f},
            {0.1111f, 0.1111f, 0.1111f}
    }, 3);

    private float[][] kernel;
    private int size;
    private int min = 0;
    private int max = 255;

    Kernel(float[][] kernel, int size) {
        this.kernel = kernel;
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public void apply(int x, int y, int[] region, int width, int[] pixels, BiFunction<Integer, Float, Number> func,
                      BinaryOperator<Number> reducer) {
        Number[] values = new Number[size * size];
        int k = 0;
        int gap = (size / 2);
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++) {
                // cords in bounded array
                int tmpX = x + j - gap;
                int tmpY = y + i - gap;
                values[k++] = func.apply(region[tmpY * width + tmpX], kernel[i][j]).floatValue();
            }
        // cords in unbounded array
        pixels[x - gap + (y - gap) * (width - 2 * gap)] = clip(
                Stream.of(values)
                        .reduce(reducer)
                        .orElseThrow(() -> new IllegalStateException("По каким-то причинам не удалось применить функцию активации!"))
        );
    }

    private int clip(Number val) {
        return val.intValue() < min ? min : val.intValue() > max ? max : val.intValue();
    }
}
