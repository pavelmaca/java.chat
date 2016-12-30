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
    public RoomRepository(Connection connection) {
        super(connection);
    }

    public Room createRoom(String name, User owner) {
        try {
            Room room = new Room(name, owner);
            PreparedStatement statement = connection.prepareStatement("INSERT INTO room (name, owner_id) VALUES(?, ?)");
            statement.setString(1, room.getName());
            statement.setInt(2, room.getOwner().getId());

            statement.executeUpdate();

            System.out.println("Room " + name + " created");
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
                    "WHERE rb.id IS NULL");
            statement.setInt(1, user.getId());

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
