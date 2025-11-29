package fr.univ_eiffel.java2brick.ColorInterpolator;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class BicubicInterpolation implements Interpolator {

    private static double CUBIC_FUNCTION_COEF = -0.5;

    @Override
    public int interpolate(BufferedImage image, int x, int y) {

        // we place the new pixel at the center of a square of 16 pixel
        double xOffset = 0.5;
        double yOffset = 0.5;

        // we create the 4x4 matrix of each componante
        int[][] redComponentMatrix = createRedComponanteMatrix(image, x, y);
        int[][] greenComponentMatrix = createGreenComponanteMatrix(image, x, y);
        int[][] blueComponentMatrix = createBlueComponanteMatrix(image, x, y);

        // we calculate the interpolation for each row and each componante
        double[] colRed = interpolateRows(redComponentMatrix, xOffset);
        double[] colGreen = interpolateRows(greenComponentMatrix, xOffset);
        double[] colBlue = interpolateRows(blueComponentMatrix, xOffset);

        // we interpolate all the rows
        double redValue = interpolateCol(colRed, yOffset);
        double greenValue = interpolateCol(colGreen, yOffset);
        double blueValue = interpolateCol(colBlue, yOffset);

        // we get a int value between 0 and 255
        int red = clampValue(redValue);
        int green = clampValue(greenValue);
        int blue = clampValue(blueValue);

        return new Color(red, green, blue).getRGB();

    }

    private double[] interpolateRows(int[][] rows, double x) {
        double[] rowInterpolated = new double[4];

        for (int i = 0; i < 4; i++) {
            rowInterpolated[i] = interpolateRow(rows[i], x);
        }

        return rowInterpolated;
    }

    private int clampValue(double value) {
        int valInt = (int) Math.round(value);

        return Math.max(0, Math.min(255, valInt));
    }

    private double interpolateRow(int[] row, double x) {

        return row[0] * weightFunction(1 + x) + row[1] * weightFunction(x)
                + row[0] * weightFunction(1 - x) + row[3] * weightFunction(2 - x);
    }

    private double interpolateCol(double[] col, double y) {

        return col[0] * weightFunction(1 + y) + col[1] * weightFunction(y)
                + col[0] * weightFunction(1 - y) + col[3] * weightFunction(2 - y);
    }

    private Color getClosestPixelValid(BufferedImage image, int x, int y) {
        if (Interpolators.isPixelExist(image, x, y)) {
            return new Color(image.getRGB(x, y));
        }

        int X;
        int Y;

        // for the border artifacts

        // at the left of the image
        if (x < 0) {
            X = 0;

            if (!Interpolators.isPixelExist(image, X, y)) {

                if (y < 0) {
                    Y = 0;
                } else {
                    Y = image.getHeight() - 1;
                }
            } else {
                Y = y;
            }
            return new Color(image.getRGB(X, Y));
        }

        // at the right
        if (x > image.getWidth()) {
            X = image.getWidth() - 1;

            if (!Interpolators.isPixelExist(image, X, y)) {

                if (y < 0) {
                    Y = 0;
                } else {
                    Y = image.getHeight() - 1;
                }
            } else {
                Y = y;
            }
            return new Color(image.getRGB(X, Y));
        }

        X = x; // x is already inside the image

        // at the top
        if (y < 0) {
            Y = 0;
        } else {
            Y = image.getHeight() - 1;
        }

        return new Color(image.getRGB(X, Y));

    }

    private int[][] createRedComponanteMatrix(BufferedImage image, int x, int y) {
        int[][] matrix = new int[4][4];

        int initX = x - 2;
        int initY = y - 2;

        for (int i = 0; i < 4; i++) {
            int Y = initY + i;
            for (int j = 0; j < 4; j++) {
                int X = initX + j;

                matrix[i][j] = getClosestPixelValid(image, X, Y).getRed();
            }
        }
        return matrix;
    }

    private int[][] createGreenComponanteMatrix(BufferedImage image, int x, int y) {
        int[][] matrix = new int[4][4];

        int initX = x - 2;
        int initY = y - 2;

        for (int i = 0; i < 4; i++) {
            int Y = initY + i;
            for (int j = 0; j < 4; j++) {
                int X = initX + j;

                matrix[i][j] = getClosestPixelValid(image, X, Y).getGreen();

            }
        }

        return matrix;
    }

    private int[][] createBlueComponanteMatrix(BufferedImage image, int x, int y) {
        int[][] matrix = new int[4][4];

        int initX = x - 2;
        int initY = y - 2;

        for (int i = 0; i < 4; i++) {
            int Y = initY + i;
            for (int j = 0; j < 4; j++) {
                int X = initX + j;

                matrix[i][j] = getClosestPixelValid(image, X, Y).getBlue();
            }
        }
        return matrix;
    }

    // sinc of the distance between
    private double weightFunction(double d) {

        d = Math.abs(d);

        double a = CUBIC_FUNCTION_COEF;

        if (d > 1 && d < 2) {
            return a * Math.pow(d, 3) - 5 * a * Math.pow(d, 2) + 8 * a * d - 4 * a;
        }

        if (d <= 1) {
            return (a + 2) * Math.pow(d, 3) - (a + 3) * Math.pow(d, 2) + 1;
        }

        return 0;
    }

    @Override
    public int optimalNumberOfInterpolation(int xRatio, int yRatio) {
        // to don't miss any pixel if the ratio is larger than 4 the 4x4 grid cannot use
        // all the deleted pixel
        // there will be lost
        return (int) Math.ceil(Math.max(xRatio, yRatio) / 4);
    }

}
