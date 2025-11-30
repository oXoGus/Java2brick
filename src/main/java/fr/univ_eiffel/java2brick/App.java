package fr.univ_eiffel.java2brick;

import java.io.File;

import fr.univ_eiffel.java2brick.ColorInterpolator.*;

public class App {
    public static void main(String[] args) throws Exception {

        // we verify that all the args are present
        if (args.length < 4) {
            throw new IllegalArgumentException(
                    "you must enter the path to the paving folder, the paving code height and width of the paving in args");
        }

        // we define the args
        String pavingCode = args[1];

        // were all the paving are stored
        String pavingFolderPath = args[0] + "/" + pavingCode;

        // we connect to the database
        DataBase db = new DataBase();

        String originalImageType = db.getOriginalImageType(pavingCode);

        // we can verify that the paving exist inside the database
        if (originalImageType.equals("")) {
            throw new IllegalArgumentException("the paving code : " + pavingCode + " doesn't exist in the database");
        }

        int width = Integer.parseInt(args[2]);
        int height = Integer.parseInt(args[3]);

        // we verify the directory containing the paving is created

        File pavingFolder = new File(pavingFolderPath);

        File[] files = pavingFolder.listFiles();

        // pavingFolder is null if the directory doesn't exists
        if (files == null) {
            throw new IllegalArgumentException("there is no directory named : " + pavingCode);
        }

        String originalImagePath = null;

        // the paving original image path is always pavingCode/original-image.ext
        // we search it
        for (File f : files) {

            if (f.getName().equals("original-image." + originalImageType)) {
                originalImagePath = f.getPath();
                break;
            }
        }

        // if the original image was not in the directory
        if (originalImagePath == null) {
            throw new IllegalArgumentException(
                    "There is no original image file in the paving directory : " + pavingCode);
        }

        // we intantiate the image
        Image image = new Image(originalImagePath);

        // we compress the image to desired dimension
        image.compress(height, width, new BicubicInterpolation());

        // optional, just to see teh difference between the paving preview
        String compressedImagePath = pavingFolderPath + "/compressed-image." + originalImageType;

        image.write(compressedImagePath);

        String textImagePath = pavingFolderPath + "/compressed-image.txt";

        // we write the compressed image into text file used by the paving algorithm
        image.writeTextImageFile(textImagePath);

        // we get the stock from the database
        Stock stock = new Stock(db);

        // we write the file
        String stockFilePath = pavingFolderPath + "/stock.txt";

        // we write the stock file for the paving algorithm
        stock.writeStockFile(stockFilePath);

        // we intantiate the paver to start the paving algorithm
        String outputPavingFilePath = pavingFolderPath + "/paving.txt";
        Paver paving = new Paver(pavingCode, db, stockFilePath, textImagePath, outputPavingFilePath,
                stock.getStock());

        // we write the paving preview
        String pavingPreviewFilePath = pavingFolderPath + "/paving-preview.png";

        System.out.println("Creating the preview image...");

        // of the same size of the compressed image
        paving.writePreviewImage(pavingPreviewFilePath, height, width);

        System.out.print("Preview image created!");

        db.free();

        return;
    }
}
