package pavelmaca.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Server {


    public static void main(String[] args) {

        // connect to DBf
        Connection connection = createDatabaseConnection();
        if (connection == null) {
            return;
        }

        int listeningPort = Configurator.getListeningPort();

        startListening(listeningPort);

        try {
            Statement statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // start listenning for clients
    }

    private static Connection createDatabaseConnection() {
        Properties properties = Configurator.getDatabaseConfig("database.properties");
        String url = "jdbc:mysql://" + properties.getProperty("host") + ":" + properties.getProperty("port")
                + "/" + properties.getProperty("database");

        try {
            return DriverManager.getConnection(url, properties);
        } catch (SQLException e) {
            System.out.println("Could not connect to database, check your configuration.");
            return null;
        }
    }

    private static void startListening(int port) {
        try {
            // TODO start in own thread, or not?
            // TODO exit command?
            ServerSocket serverSocket = new ServerSocket(port);

            System.out.println("Listening on port " + port + "...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("client connected");
                //accept a connection;
                //create a thread to deal with the client;
                new Thread(new Session(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
