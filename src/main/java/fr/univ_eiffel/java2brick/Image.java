package fr.univ_eiffel.java2brick;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import javax.imageio.ImageIO;

import fr.univ_eiffel.java2brick.ColorInterpolator.Interpolator;

public class Image {

    private final List<String> IMAGE_TYPE_ACCEPTED = List.of("png", "jpeg");

    private BufferedImage image;
    private String imageType;

    public Image(String pathString) throws IOException {
        Objects.requireNonNull(pathString);

        File file = new File(pathString);
        Path path = file.toPath();

        // we get the format name without the .
        imageType = Files.probeContentType(path).split("/")[1];

        // we only accept PNG and JPEG image
        if (!IMAGE_TYPE_ACCEPTED.contains(imageType)) {
            throw new IllegalArgumentException("the Compressor accept only png and jpeg file");
        }

        this.image = ImageIO.read(file);
    }

    public void write(String pathString) throws IOException {
        Objects.requireNonNull(pathString);

        File file = new File(pathString);
        Path path = file.toPath();

        // we get the format name without the .
        String imageType = Files.probeContentType(path).split("/")[1];

        // we only accept PNG and JPEG image
        if (!IMAGE_TYPE_ACCEPTED.contains(imageType)) {
            throw new IllegalArgumentException("the Compressor accept only png and jpeg file");
        }

        // we write the current image into the file specified
        ImageIO.write(image, imageType, new File(pathString));
    }

    private void verifyNewDimensions(int newHeight, int newWidth) {
        if (newHeight >= image.getHeight() && newWidth >= image.getWidth()) {
            throw new IllegalArgumentException("the new height and width must be less than the actual dimensions");
        }
    }

    // by default we compress the image by 2
    public void compressImage(Interpolator interpolator) {
        compressImage(image.getHeight() / 2, image.getWidth() / 2, interpolator);
    }

    public void compress(int newHeight, int newWidth, Interpolator interpolator) {

        verifyNewDimensions(newHeight, newWidth);

        if (newWidth == 0 || newHeight == 0) {
            throw new IllegalStateException("the image cannot be reduced ");
        }

        int originalHeight = image.getHeight();
        int originalWidth = image.getWidth();

        // we reduce the resolution step by step depending of the interpolator
        // to don't looe any pixel

        // we devide by 2 the each steps
        int midHeight = originalHeight / 2;
        int midWidth = originalWidth / 2;
        /*
         * while (midHeight > newHeight && midWidth > newWidth) {
         * System.out.println("compressing...");
         * 
         * compressImage(interpolator);
         * 
         * midHeight /= 2;
         * midWidth /= 2;
         * }
         */

        // the last one we just compres to the desired size
        System.out.println("compressing...");
        compressImage(newHeight, newWidth, interpolator);
        System.out.println("Image compressed !");
    }

    private void compressImage(int newHeight, int newWidth, Interpolator interpolator) {
        verifyNewDimensions(newHeight, newWidth);

        if (newWidth == 0 || newHeight == 0) {
            throw new IllegalStateException("the image cannot be reduced ");
        }

        int heightRatio = image.getHeight() / newHeight;
        int widthRatio = image.getWidth() / newWidth;

        // we create the image with the new dimensions
        BufferedImage compressedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                compressedImage.setRGB(x, y, interpolator.interpolate(image, widthRatio * x, heightRatio * y));
                // System.out.printf("loading : %.2f %%\n", (double) (y * newWidth + x) /
                // (newWidth * newHeight) * 100);
            }
        }

        // we replace the image
        image = compressedImage;

    }

    // write the image in text form to be used by the paving algorithm
    public void writeTextImageFile(String filePath) {

        try (FileWriter f = new FileWriter(filePath)) {
            // we print each the color of each pixel without the '#' spaced by a ' '
            for (int y = 0; y < image.getHeight(); y++) {

                // to dont have a blank line at the end of the file
                if (y != 0) {
                    f.write('\n');
                }
                for (int x = 0; x < image.getWidth(); x++) {

                    // to dont have a ' ' at the end of each line
                    if (x != 0) {
                        f.write(' ');
                    }
                    f.write(pixelRGBToHEXString(image.getRGB(x, y)));
                }
            }
            System.out.println("text image has been created");

        } catch (IOException e) {
            System.out.println("an error occured while writing in the image file");
            e.printStackTrace();
        }

    }

    private String pixelRGBToHEXString(int argb) {

        // the rgb contains the alpha byte
        // we remove it
        int rgb = argb & 0xFFFFFF;

        // we convert it in hex
        String hexString = Integer.toHexString(rgb);

        // we add the 0 to the left to have a 6 length string
        while (hexString.length() < 6) {
            hexString = '0' + hexString;
        }

        return hexString.toUpperCase();
    }
}
