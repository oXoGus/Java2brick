package fr.univ_eiffel.java2brick;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class DataBase {
    private final Connection conn;

    // we connect to the data base
    public DataBase() throws Exception {

        // we need to load the driver
        Class.forName("com.mysql.cj.jdbc.Driver");

        // we get the credentials for the connection
        // from a .properties file
        Properties properties = getProperties("env.properties");

        String connectionURL = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");

        // the connevtion to the database will not word if you are on eduroam
        System.out.println("Connection to the DataBase...");
        this.conn = DriverManager.getConnection(connectionURL, user, password);
        System.out.println("Connected to the DataBase !");

    }

    private static Properties getProperties(String path) throws IOException {
        Properties properties = new Properties();

        // we open the file
        try (FileInputStream propertiesFile = new FileInputStream(path)) {

            // we load the properties
            properties.load(propertiesFile);
            return properties;
        }
    }

    public ResultSet getAllStock() throws SQLException {
        // we use a prepareStatement
        PreparedStatement req = conn.prepareStatement("SELECT brick_code, quantity, price FROM brick");
        req.executeQuery(); // executeQuery for the SELECT

        return req.getResultSet();
    }

    public String getOriginalImageType(String pavingCode) throws SQLException {
        Objects.requireNonNull(pavingCode);

        PreparedStatement req = conn
                .prepareStatement("SELECT paving.original_image_type FROM paving WHERE paving.paving_code = ? ");
        req.setString(1, pavingCode);
        req.executeQuery();

        ResultSet res = req.getResultSet();

        boolean isResultExist = res.next(); // to get the first line

        // if there is no paving with this code
        if (!isResultExist) {
            return "";
        }

        return res.getString(1);
    }

    public void insertPavingDetail(String pavingCode, List<BrickPosition> brickPositions) throws SQLException {
        Objects.requireNonNull(pavingCode);
        Objects.requireNonNull(brickPositions);

        PreparedStatement req = conn.prepareStatement(
                "INSERT INTO paving_detail (paving_code, brick_code, x, y, direction, price) VALUES (?, ?, ?, ?, ?, ?)");

        // we use a batch to regroup some of the request
        // we sub divide the batch to update the progress bar

        int brickPositionsSize = brickPositions.size();
        int batchSize = 200;

        int c = 0;

        for (BrickPosition brickPosition : brickPositions) {

            req.setString(1, pavingCode);
            req.setString(2, brickPosition.brick().getBrickCode());
            req.setInt(3, brickPosition.x());
            req.setInt(4, brickPosition.y());
            req.setInt(5, brickPosition.direction());
            req.setInt(6, brickPosition.brick().price());

            req.addBatch();

            c++;

            if (c % batchSize == 0) {
                req.executeBatch();

                System.out.printf("Saving your LEGO... %.2f%%\n", ((double) c / brickPositionsSize) * 100);
            }
        }

        // we insert the last brick position
        if (c % batchSize != 0) {
            req.executeBatch();

            System.out.printf("Saving your LEGO... %.2f%%\n", ((double) c / brickPositionsSize) * 100);
        }

    }

    public void free() throws SQLException {
        conn.close();
    }

}
