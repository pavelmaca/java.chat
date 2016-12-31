package pavelmaca.chat.server.repository;

import pavelmaca.chat.client.model.Room;
import pavelmaca.chat.client.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

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

    public ArrayList<Room.Pair> getAllAvailable(User user) {
        try {
            PreparedStatement statement = connection.prepareStatement("" +
                    "SELECT r.id, r.name FROM room r " +
                    "LEFT JOIN room_block rb ON rb.room_id = r.id AND rb.user_id = ? " +
                    "LEFT JOIN room_user ru ON ru.room_id = r.id AND ru.user_id = ? " +
                    "WHERE rb.id IS NULL AND ru.id IS NULL");
            statement.setInt(1, user.getId());
            statement.setInt(2, user.getId());

            ResultSet resultSet = statement.executeQuery();
            ArrayList<Room.Pair> rooms = new ArrayList<>();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                rooms.add(new Room.Pair(id, name));
            }
            return rooms;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
