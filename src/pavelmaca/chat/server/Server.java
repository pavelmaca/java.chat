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
    private static RoomManager roomManager;

    public static void main(String[] args) {

        // connect to DB
        Properties properties = Configurator.getDatabaseConfig("database.properties");
        database = new Database(properties);
        if (!database.isConnected()) {
            return;
        }

        // start listenning for clients
        int listeningPort = Configurator.getListeningPort();
        startListening(listeningPort);

    }

    private static void startListening(int port) {
        roomManager = new RoomManager();

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
                new Thread(new Session(clientSocket, roomManager, database.getUserRepository(), database.getRoomRepository(), database.getMessageRepository())).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
