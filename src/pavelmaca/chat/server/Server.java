package pavelmaca.chat.server;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Server {


    public static void main(String[] args) {

        // connect to DBf
        Connection connection = createDatabaseConnection();
        if(connection == null){
            return;
        }

        try {
            Statement statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // start listenning for clients
    }

    private static Connection createDatabaseConnection() {
        String configFile = "database.properties";
        Properties properties = new Properties();
        try {
            // load connection from config, if exists
            FileInputStream inputStream = new FileInputStream(configFile);
            properties.load(inputStream);
        } catch (IOException e) {
            //e.printStackTrace();

            // Setup connection
            System.out.println("Can not load database configuration.");
            Scanner scanner = new Scanner(System.in);
            System.out.println("host:");
            properties.setProperty("host", scanner.next());

            System.out.println("port:");
            properties.setProperty("port", scanner.next());

            System.out.println("username:");
            properties.setProperty("user", scanner.next());

            System.out.println("password:");
            properties.setProperty("password", scanner.next());

            System.out.println("database:");
            properties.setProperty("database", scanner.next());
            try {
                FileOutputStream outputStream = new FileOutputStream(configFile);
                properties.store(outputStream, null);
            } catch (IOException e1) {
                //e1.printStackTrace();
                System.out.println("Can not save database configuration.");
            }
        }

        String url = "jdbc:mysql://" + properties.getProperty("host") + ":" + properties.getProperty("port")
                + "/" + properties.getProperty("database");

        try {
            return DriverManager.getConnection(url, properties);
        } catch (SQLException e) {
            System.out.println("Could not connect to database, check your configuration.");
            return null;
        }
    }
}
