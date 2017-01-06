package pavelmaca.chat.server.repository;

import pavelmaca.chat.server.entity.Room;
import pavelmaca.chat.server.entity.User;
import pavelmaca.chat.share.model.RoomInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class RoomRepository extends Repository {
    UserRepository userRepository;

    public RoomRepository(Connection connection, UserRepository userRepository) {
        super(connection);
        this.userRepository = userRepository;
    }

    public Room createRoom(String name, User owner) {
        try {

            PreparedStatement statement = connection.prepareStatement("INSERT INTO room (name, owner_id) VALUES(?, ?)", new String[]{"id"});
            statement.setString(1, name);
            statement.setInt(2, owner.getId());

            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                System.out.println("Room " + name + " created");
                return new Room(generatedKeys.getInt(1), name, owner);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Room joinRoom(int roomId, User user) {
        Room room = findOneById(roomId);
        return joinRoom(room, user);
    }

    private Room findOneById(int roomId) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT owner_id, id, name FROM room WHERE id = ?");
            statement.setInt(1, roomId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                User owner = userRepository.findOneById(resultSet.getInt(1));
                return new Room(resultSet.getInt(2), resultSet.getString(3), owner);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Room joinRoom(Room room, User user) {
        if (room == null) {
            return null;
        }
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO room_user (room_id, user_id) VALUES(?, ?)");
            statement.setInt(1, room.getId());
            statement.setInt(2, user.getId());
            statement.executeUpdate();

            return room;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<RoomInfo> getAllAvailable(User user) {
        try {
            PreparedStatement statement = connection.prepareStatement("" +
                    "SELECT r.id, r.name FROM room r " +
                    "LEFT JOIN room_block rb ON rb.room_id = r.id AND rb.user_id = ? " +
                    "LEFT JOIN room_user ru ON ru.room_id = r.id AND ru.user_id = ? " +
                    "WHERE rb.id IS NULL AND ru.id IS NULL");
            statement.setInt(1, user.getId());
            statement.setInt(2, user.getId());

            ResultSet resultSet = statement.executeQuery();
            ArrayList<RoomInfo> rooms = new ArrayList<>();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                rooms.add(new RoomInfo(id, name));
            }
            return rooms;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get all currently joined rooms for user
     *
     * @param user
     * @return
     */
    public ArrayList<Room> getActiveRooms(User user) {
        ArrayList<Room> rooms = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement("" +
                    "SELECT r.owner_id, r.id, r.name FROM room r " +
                    "JOIN room_user ru ON ru.room_id = r.id AND ru.user_id = ? ");
            statement.setInt(1, user.getId());

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                User owner = userRepository.findOneById(resultSet.getInt("owner_id"));
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                rooms.add(new Room(id, name, owner));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public int countUsers(Room room) {
        int count = 0;
        try {
            PreparedStatement statement = connection.prepareStatement("" +
                    "SELECT COUNT(ru.user_id) AS total FROM room_user ru " +
                    "WHERE ru.room_id = ? ");
            statement.setInt(1, room.getId());

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public ArrayList<User> getConnectedUsers(Room room) {
        ArrayList<User> users = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT u.id, u.name, u.password " +
                    "FROM room_user ru " +
                    "LEFT JOIN user u ON ru.user_id = u.id " +
                    "WHERE ru.room_id =  ? ");
            statement.setInt(1, room.getId());

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                users.add(new User(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }
}
