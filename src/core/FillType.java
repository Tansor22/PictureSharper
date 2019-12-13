package core;

import java.util.function.BiFunction;

public enum FillType {
    BORDER() {
        @Override
        @BlackBox
        public int[] setBounds(int[] a, int columns, int gap) {
            int length = a.length;
            int rows = length / columns;
            int tmpCols = columns + (2 * gap);
            int tmpRows = rows + (2 * gap);
            int[] tmp = new int[tmpCols * tmpRows];

            populate(tmp, tmpCols, (x, y) -> {
                // by changing these vars' names it's possible to populate differently
                // e.g. isBottom => isTop, isTop => isBottom ==> populating will be changed
                boolean isRight = x >= gap;
                boolean isLeft = x < columns + gap;
                boolean isBottom = y >= gap;
                boolean isTop = y < rows + gap;

                if (isLeft && isRight && isTop && isBottom)
                    // in bounds
                    return a[(y - gap) * columns + (x - gap)];
                else if (isRight && isTop && isLeft)
                    // up
                    return a[x - gap];
                else if (isLeft && isTop && isBottom)
                    // right
                    return a[columns * (y - gap)];
                else if (isRight && isTop && isBottom)
                    // left
                    return a[(y - gap) * columns + (columns - 1)];
                else if (isRight && isLeft && isBottom)
                    // bottom
                    return a[(rows - 1) * columns + (x - gap)];

                    // angles
                else if (isTop && isLeft)
                    // left upper angle
                    return a[0];
                else if (isLeft && isBottom)
                    // left bottom angle
                    return a[(rows - 1) * columns];
                else if (isRight && isTop)
                    // right upper angle
                    return a[columns - 1];
                    //isRight && isBottom
                    // right bottom angle
                else return a[rows * columns - 1];

            });
            return tmp;
        }
    },
    ZEROS(){
        @Override
        @BlackBox
        public int[] setBounds(int[] a, int columns, int gap) {
            int length = a.length;
            int rows = length / columns;
            int tmpCols = columns + (2 * gap);
            int tmpRows = rows + (2 * gap);
            int[] tmp = new int[tmpCols * tmpRows];
            populate(tmp, tmpCols, (x, y) ->
                    (x >= gap && x < columns + gap && y >= gap && y < rows + gap) ? a[(y - gap) * columns + (x - gap)] : 0);
            return tmp;
        }
    };

    private static void populate(int a[], int columns, BiFunction<Integer, Integer, Integer> setter) {
        for (int y = 0; y < a.length / columns; y++)
            for (int x = 0; x < columns; x++)
                a[y * columns + x] = setter.apply(x, y);
    }

    public abstract int[] setBounds(int[] a, int columns, int gap);
}
