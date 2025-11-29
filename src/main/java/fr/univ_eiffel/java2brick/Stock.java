package fr.univ_eiffel.java2brick;

import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Stock {
    private final DataBase dataBase;
    private ArrayList<Brick> bricks;

    public Stock(DataBase dt) throws Exception {
        this.dataBase = dt;

        this.bricks = new ArrayList<>();

        getStockFromDataBase();
    }

    // or we instanciate the dataBase
    public Stock() throws Exception {
        this.dataBase = new DataBase();

        this.bricks = new ArrayList<>();

        getStockFromDataBase();
    }

    private void getStockFromDataBase() throws SQLException {
        ResultSet res = dataBase.getAllStock();

        // we create the Brick from the db
        // for each brick in the db
        // res is initialy not to the first row so we don't skip it

        int brickId = 0;
        while (res.next()) {

            String brickCode = res.getString("brick_code");
            long quantity = res.getLong("quantity");
            int price = res.getInt("price");

            // the brick id is the index of the brick in the stock
            bricks.add(new Brick(brickId, brickCode, quantity, price));
            brickId++;
        }

        res.close();
    }

    public void writeStockFile(String filePath) {
        // we use a close with resources to automaticly close the file
        try (FileWriter f = new FileWriter(filePath)) {

            boolean isFirstLine = true;
            // one brick per line
            for (Brick brick : bricks) {

                // we write only the one in stock
                // if (!brick.isOutOfStock()) {
                //
                // if (!isFirstLine) {
                // f.write('\n');
                // }
                //
                // f.write(brick.toString());
                // isFirstLine = false;
                //
                // }

                // we don't use the stock
                if (!isFirstLine) {
                    f.write('\n');
                }
                f.write(brick.toString());
                isFirstLine = false;

            }

            System.out.println("Stock file created");

        } catch (Exception e) {
            System.out.println("an error occured while writing in the stock file");
            e.printStackTrace();
        }
    }

    public List<Brick> getStock() {
        return List.copyOf(bricks);
    }

}
