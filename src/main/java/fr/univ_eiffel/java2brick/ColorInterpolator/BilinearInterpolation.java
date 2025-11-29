package fr.univ_eiffel.java2brick.ColorInterpolator;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class BilinearInterpolation implements Interpolator {
    @Override
    public int interpolate(BufferedImage image, int x, int y) {

        // we return the average colors of the 4 pixels nearby

        // the list of all the color nearby the pixel
        ArrayList<Color> colorList = new ArrayList<>();

        // the main pixel
        colorList.add(new Color(image.getRGB(x, y)));

        // the top pixel
        if (Interpolators.isPixelExist(image, x, y - 1)) {
            colorList.add(new Color(image.getRGB(x, y - 1)));
        }

        // the left pixel
        if (Interpolators.isPixelExist(image, x - 1, y)) {
            colorList.add(new Color(image.getRGB(x - 1, y)));
        }

        // the right pixel
        if (Interpolators.isPixelExist(image, x + 1, y)) {
            colorList.add(new Color(image.getRGB(x + 1, y)));
        }

        // the bottom pixel
        if (Interpolators.isPixelExist(image, x, y + 1)) {
            colorList.add(new Color(image.getRGB(x, y + 1)));
        }

        return Interpolators.averageColor(List.copyOf(colorList));
    }

    @Override
    public int optimalNumberOfInterpolation(int xRatio, int yRatio) {
        // to don't miss any pixel if the ratio is larger than 2
        // there will be lost
        return (int) Math.floor(Math.max(xRatio, yRatio) / 2);
    }

}
