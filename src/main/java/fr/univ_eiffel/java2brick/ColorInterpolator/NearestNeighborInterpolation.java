package fr.univ_eiffel.java2brick.ColorInterpolator;

import java.awt.image.BufferedImage;

public class NearestNeighborInterpolation implements Interpolator {
    @Override
    public int interpolate(BufferedImage image, int x, int y) {
        return image.getRGB(x, y);
    }

    @Override
    public int optimalNumberOfInterpolation(int xRatio, int yRatio) {
        return (int) Math.floor(Math.max(xRatio, yRatio) / 2);
    }
}
