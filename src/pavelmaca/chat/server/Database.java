package pavelmaca.chat.server;



import pavelmaca.chat.server.repository.RoomRepository;
import pavelmaca.chat.server.repository.UserRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Database {
    Connection connection;

    // Repositories
    private UserRepository userRepository;
    private RoomRepository roomRepository;

    public Database(Properties properties) {
        connect(properties);
    }

    private void connect(Properties properties) {
        String url = "jdbc:mysql://" + properties.getProperty("host") + ":" + properties.getProperty("port")
                + "/" + properties.getProperty("database");

        try {
            connection = DriverManager.getConnection(url, properties);
        } catch (SQLException e) {
            System.out.println("Could not connect to database, check your configuration.");
        }
    }

    public boolean isConnected(){
        try {
            return connection.isValid(100);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void closeConnection(){
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public UserRepository getUserRepository(){
        if(userRepository== null){
            userRepository = new UserRepository(connection);
        }
        return userRepository;
    }

    public RoomRepository getRoomRepository(){
        if(roomRepository== null){
            roomRepository = new RoomRepository(connection, getUserRepository());
        }
        return roomRepository;
    }
}
