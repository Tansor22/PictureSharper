package utils;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Properties;


public class Helper {
    public static void printMatrix(int[] m, int columns) {
        int length = m.length;
        int rows = length / columns;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++)
                System.out.print(m[i * columns + j] + " ");
            System.out.print('\n');
        }
        System.out.println("------------------------");
    }

    public static void printMatrix(String mes, int[] m, int columns) {
        System.out.println(mes);
        printMatrix(m, columns);
    }

    public static <T> T readFromProperties(Properties pr) {
return null;
    }

    public static int getSample(int[] src, int[] dest) {
        int n = dest.length;
        int begin = (int) (0 + (src.length - n - 1) * Math.random());
        for (int i = begin; i < begin + n; i++) {
            dest[i - begin] = src[i];
        }
        return begin;
    }

    public static int[] getSample(int[] src, int begin, int n) {
        int[] output = new int[n];
        for (int i = begin; i < begin + n; i++) {
            output[i - begin] = src[i];
        }
        return output;
    }

    public static void printVector(int[] v) {
        for (int i = 0; i < v.length; i++) {
            System.out.print(v[i] + " ");
        }
    }

    public static boolean isInteger(String str) {
        if (str.equals("") || str == null) return false;
        for (int i = 0; i < str.length(); i++)
            if (!Character.isDigit(str.charAt(i))) return false;
        return true;
    }

    public static boolean isDouble(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            ++i;
        }
        int integerPartSize = 0;
        int exponentPartSize = -1;
        while (i < length) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                if (c == '.' && integerPartSize > 0 && exponentPartSize == -1) {
                    exponentPartSize = 0;
                } else {
                    return false;
                }
            } else if (exponentPartSize > -1) {
                ++exponentPartSize;
            } else {
                ++integerPartSize;
            }
            ++i;
        }
        if ((str.charAt(0) == '0' && i > 1 && exponentPartSize < 1)
                || exponentPartSize == 0 || (str.charAt(length - 1) == '.')) {
            return false;
        }
        return true;
    }

    public static void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image("/resources/img/icon.png"));
        alert.setTitle("Предупреждение");
        alert.setHeaderText("Что-то пошло не так");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
