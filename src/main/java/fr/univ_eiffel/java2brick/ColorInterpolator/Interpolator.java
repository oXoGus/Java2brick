package fr.univ_eiffel.java2brick.ColorInterpolator;

import java.awt.image.BufferedImage;

public interface Interpolator {
    int interpolate(BufferedImage image, int x, int y);

    // to dont lose pixels if the ratio is too big
    int optimalNumberOfInterpolation(int xRatio, int yRatio);
}
