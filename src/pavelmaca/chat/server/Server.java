package pavelmaca.chat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Server {
    private static Database database;

    public static void main(String[] args) {

        // connect to DB
        Properties properties = Configurator.getDatabaseConfig("database.properties");
        database = new Database(properties);
        if (!database.isConnected()) {
            return;
        }

        // start listening for clients
        int listeningPort = Configurator.getListeningPort();
        startListeningForClients(listeningPort);
    }

    private static void startListeningForClients(int port) {
        RoomManager roomManager = new RoomManager();
        try {
            ServerSocket serverSocket = new ServerSocket(port);

            System.out.println("Listening on port " + port + "...");
            while (true) {
                //accept a connection;
                Socket clientSocket = serverSocket.accept();
                System.out.println("client connected");

                //create a thread to deal with the client;
                new Thread(new Session(clientSocket, roomManager, database.getUserRepository(), database.getRoomRepository(), database.getMessageRepository())).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        database.closeConnection();
    }


}
