package fr.univ_eiffel.java2brick;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.imageio.ImageIO;

public class Paver {

    private final int NUM_BRICK_POS_PROPERTIES = 4;
    private final int SUB_BRICK_PREVIEW_IMAGE_SIZE = 30;

    private final String pavingCode;

    // all the information to recronstruct the paving
    private final ArrayList<BrickPosition> brickPositions;

    // database connection to insert the paving
    private final DataBase db;

    public Paver(String pavingCode, DataBase db, String fileStockPath, String fileImageTextPath,
            String outputPavingFilePath,
            List<Brick> stock) throws Exception {
        Objects.requireNonNull(db);
        Objects.requireNonNull(fileImageTextPath);
        Objects.requireNonNull(fileStockPath);

        this.pavingCode = pavingCode;
        this.db = db;

        // we execute the paving algorithm with the API ProcessBuilder
        ProcessBuilder processBuilder = new ProcessBuilder("../img2brickC/main_container", fileStockPath,
                fileImageTextPath,
                outputPavingFilePath);

        // we start the program
        Process process = processBuilder.start();

        // to display the errors of the C program
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String printedLine;
            while ((printedLine = reader.readLine()) != null) {
                System.out.println(printedLine);
            }
        }

        // we wait until the program is finished
        int exitCode = process.waitFor();

        if (exitCode == 1) {

            throw new Exception(
                    "the c program had an error : get the error by executing the program directly in shell : ../img2brickC/main_container"
                            + fileStockPath + " " + fileImageTextPath + " " + outputPavingFilePath);
        }

        this.brickPositions = new ArrayList<>();

        System.out.println("Saving your LEGO...");

        // we need the stock to get the price of each brick
        getPavingData(outputPavingFilePath, stock);

        // we insert the paving into the database;
        insertPavingIntoDataBase();

        System.out.println("LEGO saved!");
    }

    private void getPavingData(String outputPavingFilePath, List<Brick> stock) {

        try {
            BufferedReader f = new BufferedReader(new FileReader(outputPavingFilePath));

            String brickPositionString = f.readLine();

            // one line is one brick position
            while (brickPositionString != null) {
                brickPositions.add(stringToBrickPosition(brickPositionString, stock));
                brickPositionString = f.readLine();
            }

            f.close();
        } catch (IOException e) {
            System.out.println("Error during the read of the paving file : " + outputPavingFilePath);
        }
    }

    private BrickPosition stringToBrickPosition(String brickPositionString, List<Brick> stock) {
        String[] brickPositionProperties = brickPositionString.split(" ");

        // we verify that the line is valid
        if (brickPositionProperties.length != NUM_BRICK_POS_PROPERTIES) {
            throw new IllegalArgumentException(
                    "the brick position string line : " + brickPositionString + " is not valid");
        }

        int brickId = Integer.parseInt(brickPositionProperties[0]);

        int x = Integer.parseInt(brickPositionProperties[1]);

        int y = Integer.parseInt(brickPositionProperties[2]);

        int direction = Integer.parseInt(brickPositionProperties[3]);

        // the position is an integer between 0 and 3
        if (direction < 0 || direction > 3) {
            throw new IllegalArgumentException("the direction of the brick is invalid ");
        }

        // the info in the stock
        Brick brick = stock.get(brickId);

        return new BrickPosition(brick, x, y, direction);
    }

    private boolean isDifferentSubBrickLeft(List<List<Integer>> brickMatrix, int xBrick, int yBrick) {
        //
        if (xBrick == 0 || brickMatrix.get(yBrick).get(xBrick - 1) == 0) {
            return true;
        }
        return false;
    }

    private boolean isDifferentSubBrickRight(List<List<Integer>> brickMatrix, int xBrick, int yBrick) {
        //
        if (xBrick == brickMatrix.get(0).size() - 1 || brickMatrix.get(yBrick).get(xBrick + 1) == 0) {
            return true;
        }
        return false;
    }

    private boolean isDifferentSubBrickTop(List<List<Integer>> brickMatrix, int xBrick, int yBrick) {
        //
        if (yBrick == 0 || brickMatrix.get(yBrick - 1).get(xBrick) == 0) {
            return true;
        }
        return false;
    }

    private boolean isDifferentSubBrickDown(List<List<Integer>> brickMatrix, int xBrick, int yBrick) {
        //
        if (yBrick == brickMatrix.size() - 1 || brickMatrix.get(yBrick + 1).get(xBrick) == 0) {
            return true;
        }
        return false;
    }

    private boolean isPixelBorder(List<List<Integer>> brickMatrix, int xBrick, int yBrick, int subX, int subY) {
        // if the subX or subY is at the border of the subBrick

        if (subX > 0 && subX < SUB_BRICK_PREVIEW_IMAGE_SIZE - 1 && subY > 0
                && subY < SUB_BRICK_PREVIEW_IMAGE_SIZE - 1) {
            return false;
        }

        boolean res = false;

        // sub x at the left
        if (subX == 0) {
            res = res || isDifferentSubBrickLeft(brickMatrix, xBrick, yBrick);
        }

        // right
        if (subX == SUB_BRICK_PREVIEW_IMAGE_SIZE - 1) {
            res = res || isDifferentSubBrickRight(brickMatrix, xBrick, yBrick);
        }

        // top
        if (subY == 0) {
            res = res || isDifferentSubBrickTop(brickMatrix, xBrick, yBrick);
        }

        if (subY == SUB_BRICK_PREVIEW_IMAGE_SIZE - 1) {
            res = res || isDifferentSubBrickDown(brickMatrix, xBrick, yBrick);
        }

        return res;

    }

    private void writeSubBrick(BrickPosition brickPosition, int xBrick, int yBrick, BufferedImage previewImage,
            List<List<Integer>> brickMatrix) {

        // we recreate a photo realistic image of a sub brick (a squarre with a round in
        // the middle)

        Color brickColor = brickPosition.brick().getColor();

        // the circle in the middle will be slightly lighter than the brick color
        int circleColor = brickColor.brighter().getRGB();

        // the edge of the brick (to see the delimitation of the brick)
        // will be darker than the brick color
        int borderColor = brickColor.darker().getRGB();

        int brickColorInt = brickColor.getRGB();

        double circleCenterX = SUB_BRICK_PREVIEW_IMAGE_SIZE / 2;
        double circleCenterY = SUB_BRICK_PREVIEW_IMAGE_SIZE / 2;

        // circle size
        double circleRadius = SUB_BRICK_PREVIEW_IMAGE_SIZE / 4;

        // the sub brick is
        for (int subY = 0; subY < SUB_BRICK_PREVIEW_IMAGE_SIZE; subY++) {
            for (int subX = 0; subX < SUB_BRICK_PREVIEW_IMAGE_SIZE; subX++) {

                int pixelColor;

                // we calculate the distance between the (x, y) and the center

                double distanceFromCenter = Math
                        .sqrt(Math.pow(subX - circleCenterX, 2) + Math.pow(subY - circleCenterY, 2));

                // if the current pixel is inside the circle radius
                if (distanceFromCenter < circleRadius) {

                    // color of the the circle
                    pixelColor = circleColor;
                } else {
                    pixelColor = brickColorInt;
                }

                // if we are on the border of a brick
                if (isPixelBorder(brickMatrix, xBrick, yBrick, subX, subY)) {
                    pixelColor = borderColor;
                }

                // IndexOutOfBoudException only in the container
                try {
                    // we write the color
                    previewImage.setRGB((brickPosition.x() + xBrick) * SUB_BRICK_PREVIEW_IMAGE_SIZE + subX,
                            (brickPosition.y() + yBrick) * SUB_BRICK_PREVIEW_IMAGE_SIZE + subY, pixelColor);

                } catch (Exception e) {
                    int pixelX = (brickPosition.x() + xBrick) * SUB_BRICK_PREVIEW_IMAGE_SIZE + subX;
                    int pixelY = (brickPosition.y() + yBrick) * SUB_BRICK_PREVIEW_IMAGE_SIZE + subY;

                    System.out.println("pixel (" + pixelX + ", " + pixelY + ") out of the image");
                }

            }
        }

    }

    private void writeBrick(BrickPosition brickPosition, BufferedImage previewImage) {

        // the matrix of the brick in the direction
        List<List<Integer>> brickMatrix = brickPosition.brick().getPositionMatrix(brickPosition.direction());

        for (int yBrick = 0; yBrick < brickMatrix.size(); yBrick++) {
            for (int xBrick = 0; xBrick < brickMatrix.get(0).size(); xBrick++) {
                writeSubBrick(brickPosition, xBrick, yBrick, previewImage, brickMatrix);
            }
        }
    }

    public void writePreviewImage(String previewImagePath, int imageHeight, int imageWidth) throws IOException {
        Objects.requireNonNull(previewImagePath);

        // we create the buffered image
        BufferedImage previewImage = new BufferedImage(imageWidth * SUB_BRICK_PREVIEW_IMAGE_SIZE,
                imageHeight * SUB_BRICK_PREVIEW_IMAGE_SIZE, BufferedImage.TYPE_INT_RGB);

        // for each brick
        for (BrickPosition brickPosition : brickPositions) {

            // we draw the brick
            writeBrick(brickPosition, previewImage);
        }
        ImageIO.write(previewImage, "png", new File(previewImagePath));
    }

    private void insertPavingIntoDataBase() throws SQLException {
        // we let the database handle the requests
        db.insertPavingDetail(pavingCode, brickPositions);
    }
}
