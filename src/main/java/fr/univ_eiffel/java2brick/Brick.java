package fr.univ_eiffel.java2brick;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Brick {

    // the id used by the C program
    private final int brickId;
    private final String brickCode;
    private long quantity;
    private final int price;
    private final Color color;
    private final String colorHex;
    private final String lacunaryPosition;
    private final int height;
    private final int width;

    // at i matrix of postion when the direction of the brick is i
    private List<List<List<Integer>>> brickPositionMatrix;

    public Brick(int brickId, String brickCode, long quantity, int price) {
        Objects.requireNonNull(brickCode);

        if (quantity < 0) {
            throw new IllegalArgumentException("the quantity must be positive");
        }

        if (price <= 0) {
            throw new IllegalArgumentException("the price must be more than 0 cents");
        }

        this.brickId = brickId;
        this.brickCode = brickCode;
        this.quantity = quantity;
        this.price = price;

        String colorPart = brickCode.split("/")[1];

        // if the brick has lacunary postion
        if (colorPart.contains("-")) {

            this.lacunaryPosition = colorPart.split("-")[1];
            this.colorHex = colorPart.split("-")[0];
            this.color = HEXStringToColor(colorHex);

        } else {
            this.colorHex = colorPart;
            this.color = HEXStringToColor(colorHex);
            this.lacunaryPosition = "";
        }

        String[] dimensions = brickCode.split("/")[0].split("-");

        this.width = Integer.parseInt(dimensions[0]);

        this.height = Integer.parseInt(dimensions[1]);

        // calculate all the position matrix
        this.brickPositionMatrix = calculateAllPositionMatrix();
    }

    private Color HEXStringToColor(String hexString) {
        Objects.requireNonNull(hexString);

        int red = Integer.parseInt(hexString.substring(0, 2), 16);

        int green = Integer.parseInt(hexString.substring(2, 4), 16);
        int blue = Integer.parseInt(hexString.substring(4, 6), 16);

        return new Color(red, green, blue);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null)
            return false;

        if (!getClass().equals(other.getClass()))
            return false;

        Brick other2 = (Brick) other;
        return brickCode.equals(other2.brickCode);// two brick are the same if they have the same number
    }

    public boolean isOutOfStock() {
        return quantity == 0;
    }

    @Override
    public String toString() {

        // in the format of the stock file
        // [brick Code] [color] [height] [width] [flattened matixe] [price] [quantity]
        //
        String colorHex = brickCode.split("/")[1];

        String flatenedMatrix = getFlattenedMatrixFromLacunaryPosition(lacunaryPosition, height, width);

        // we don'u use the stock

        String[] brickProperties = new String[] { String.valueOf(brickId), colorHex, String.valueOf(height),
                String.valueOf(width),
                flatenedMatrix,
                String.valueOf(price), "9999" };

        return String.join(" ", brickProperties);
    }

    private String getFlattenedMatrixFromLacunaryPosition(String lacunaryPosition, int height, int width) {
        StringBuilder flatenedMatrix = new StringBuilder();

        // if there are not lacunary position
        // flattend matix is full
        if (lacunaryPosition == null) {

            int pos = 0;
            while (pos < height * width) {
                flatenedMatrix.append("1");
                pos++;
            }
            return flatenedMatrix.toString();
        }

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                // 0 if there is no brick part in the coord
                boolean isEmpty = lacunaryPosition.contains(String.valueOf(i * width + j));

                if (isEmpty) {
                    flatenedMatrix.append("0");
                } else {
                    flatenedMatrix.append("1");
                }
            }
        }

        return flatenedMatrix.toString();
    }

    public int price() {
        return price;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public String getBrickCode() {
        return brickCode;
    }

    public List<List<Integer>> getPositionMatrix(int dir) {
        return brickPositionMatrix.get(dir);
    }

    public Color getColor() {
        return color;
    }

    public List<List<List<Integer>>> calculateAllPositionMatrix() {

        // we cannot use a table because his size is fixed and all the same
        // for each sub table

        // we create dynamicly the list usign a array list
        ArrayList<List<List<Integer>>> allBrickPositions = new ArrayList<>();

        // we create the matrix for the dir = 0
        ArrayList<List<Integer>> currentDir = new ArrayList<>();
        for (int y = 0; y < height; y++) {

            ArrayList<Integer> currentRow = new ArrayList<>();
            for (int x = 0; x < width; x++) {
                if (lacunaryPosition.contains(String.valueOf(y * width + x))) {
                    currentRow.add(0);
                } else {
                    currentRow.add(1);
                }
            }

            // we add the row
            currentDir.add(List.copyOf(currentRow));
        }

        // we add the matrix
        allBrickPositions.add(List.copyOf(currentDir));

        // for each direction
        for (int dir = 1; dir < 4; dir++) {

            // we read the matrix of the previous direction
            int yLen = allBrickPositions.get(dir - 1).get(0).size();
            int xLen = allBrickPositions.get(dir - 1).size();

            currentDir = new ArrayList<>();

            // create the matrix
            for (int y = 0; y < yLen; y++) {

                ArrayList<Integer> currentRow = new ArrayList<>();
                for (int x = 0; x < xLen; x++) {
                    currentRow.add(allBrickPositions.get(dir - 1).get(x).get(y));
                }

                currentDir.add(List.copyOf(currentRow));
            }

            allBrickPositions.add(List.copyOf(currentDir));
        }

        // we return a unmodified list
        return List.copyOf(allBrickPositions);
    }
}
