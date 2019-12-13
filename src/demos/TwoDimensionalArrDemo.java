package demos;

public class TwoDimensionalArrDemo {
    public static void main(String[] args) {
        int[] arr = new int[]{
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12,
        };
        int length = arr.length;
        int columns = 4;
        int rows = length / columns;
        // iteration like one dimension
        for (int i = 0; i < length; i++) {
            System.out.print(arr[i] + " ");
        }
        System.out.println('\n');
        // iteration like two dimension (in that case items whose index is greater than columns * rows are hidden for iteration)
        // THE ONLY ONE TRUE WAY
        for (int i = 0; i < rows; i++) { // Y cord
            for (int j = 0; j < columns; j++) { // X cord
                System.out.print(arr[i * columns + j] + " ");
            }
            System.out.println();
        }
        System.out.println();

        // getting row number of an item
        for (int i = 0; i < length; i++)
            System.out.println("Row number (Y cord) of " + arr[i] + " (index: " + i + ") is " + i / columns);

        System.out.println();

        // getting column number of an item
        for (int i = 0; i < length; i++)
            System.out.println("Column number (X cord) of " + arr[i] + " (index: " + i + ") is " + i % columns);

        System.out.println();

        // swapping rows and columns (transpose)
        if (rows == columns) {
            // transposing can ONLY be done in case of square matrix, otherwise ArrayIndexOutOfBoundsException causes
            int[] transpose = new int[length];
            for (int i = 0; i < columns; i++) {
                for (int j = 0; j < rows; j++) {
                    transpose[i * rows + j] = arr[j * rows + i];
                    System.out.print(transpose[i * rows + j] + " ");
                }
                System.out.println();
            }

            System.out.println();
        }

        // accessing all the column values at a given index
        int [] columnValues = new int[rows];
        int index = columns - 1; // last column
        for (int i = 0; i < rows; i++) {
            columnValues[i] = arr[columns * i + index];
            System.out.print(columnValues[i] + " ");
        }

        System.out.println();

        // accessing all the row values at a given index
        int [] rowValues = new int[columns];
        index = rows - 1; // last row
        for (int i = 0; i < columns; i++) {
            rowValues[i] = arr[index * columns + i];
            System.out.print(rowValues[i] + " ");
        }
    }
}
