package fr.univ_eiffel.java2brick.ColorInterpolator;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

public class Interpolators {
    public static int averageColor(List<Color> colorList) {
        int redSum = 0;
        int greenSum = 0;
        int blueSum = 0;

        for (Color color : colorList) {
            redSum += color.getRed();
            greenSum += color.getGreen();
            blueSum += color.getBlue();
        }

        return new Color(redSum / colorList.size(), greenSum / colorList.size(), blueSum / colorList.size())
                .getRGB();
    }

    public static boolean isPixelExist(BufferedImage image, int x, int y) {
        return x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight();
    }

    public static int averagePonderateColor(List<Color> colors, List<Double> coefs) {
        double redSum = 0;
        double greenSum = 0;
        double blueSum = 0;

        for (int i = 0; i < colors.size(); i++) {
            Color color = colors.get(i);
            double coef = coefs.get(i);

            redSum += color.getRed() * coef;
            greenSum += color.getGreen() * coef;
            blueSum += color.getBlue() * coef;
        }

        double coefSum = coefs.stream().mapToDouble(Double::doubleValue).sum();

        return new Color((int) (redSum / coefSum), (int) (greenSum / coefSum), (int) (blueSum / coefSum))
                .getRGB();
    }

}
