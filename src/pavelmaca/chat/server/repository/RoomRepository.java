package pavelmaca.chat.server.repository;

import pavelmaca.chat.server.entity.Room;
import pavelmaca.chat.server.entity.User;
import pavelmaca.chat.share.model.RoomInfo;
import pavelmaca.chat.share.model.UserInfo;

import java.sql.*;
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

    public Room createRoom(String name, User owner, String password) {
        try {

            PreparedStatement statement = connection.prepareStatement("INSERT INTO room (name, owner_id, password) VALUES(?, ?, ?)", new String[]{"id"});
            statement.setString(1, name);
            statement.setInt(2, owner.getId());

            if (password.isEmpty()) {
                statement.setNull(3, Types.VARCHAR);
            } else {
                statement.setString(3, password);
            }

            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                System.out.println("Room " + name + " created");
                return new Room(generatedKeys.getInt(1), name, owner, password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

  /*  public Room joinRoom(int roomId, User user) {
        Room room = findOneById(roomId);
        return joinRoom(room, user);
    }*/

    public Room findOneById(int roomId) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT owner_id, id, name, password FROM room WHERE id = ?");
            statement.setInt(1, roomId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                User owner = userRepository.findOneById(resultSet.getInt("owner_id"));
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String password = resultSet.getString("password");
                return new Room(id, name, owner, password);
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

    public void leaveRoom(int roomId, int userId) {
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM room_user WHERE room_id = ? AND user_id = ?");
            statement.setInt(1, roomId);
            statement.setInt(2, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<RoomInfo> getAllAvailable(User user) {
        try {
            PreparedStatement statement = connection.prepareStatement("" +
                    "SELECT r.id, r.name, r.password FROM room r " +
                    "LEFT JOIN room_block rb ON rb.room_id = r.id AND rb.user_id = ? " +
                    "LEFT JOIN room_user ru ON ru.room_id = r.id AND ru.user_id = ? " +
                    "WHERE rb.id IS NULL AND ru.id IS NULL AND r.deleted != 1");
            statement.setInt(1, user.getId());
            statement.setInt(2, user.getId());

            ResultSet resultSet = statement.executeQuery();
            ArrayList<RoomInfo> rooms = new ArrayList<>();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String password = resultSet.getString("password");

                boolean hasPassword = !(password == null || password.isEmpty());
                rooms.add(new RoomInfo(id, name, hasPassword));
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
                    "SELECT r.owner_id, r.id, r.name, r.password FROM room r " +
                    "JOIN room_user ru ON ru.room_id = r.id AND ru.user_id = ? " +
                    "WHERE r.deleted != 1");
            statement.setInt(1, user.getId());

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                User owner = userRepository.findOneById(resultSet.getInt("owner_id"));
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String password = resultSet.getString("password");
                rooms.add(new Room(id, name, owner, password));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
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

    public ArrayList<UserInfo> getBannedUsers(Room room) {
        ArrayList<UserInfo> users = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT u.id, u.name " +
                    "FROM room_block rb " +
                    "LEFT JOIN user u ON rb.user_id = u.id " +
                    "WHERE rb.room_id =  ? ");
            statement.setInt(1, room.getId());

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                UserInfo userInfo = new UserInfo(id, name);
                userInfo.setStatus(UserInfo.Status.BANNED);
                users.add(userInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    public boolean changeName(Room room, String newName) {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE room SET name = ? WHERE id = ? ");
            statement.setString(1, newName);
            statement.setInt(2, room.getId());

            statement.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean changePassword(Room room, String newPassword) {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE room SET password = ? WHERE id = ? ");
            statement.setString(1, newPassword);
            statement.setInt(2, room.getId());

            statement.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean removePassword(Room room) {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE room SET password = ? WHERE id = ? ");
            statement.setNull(1, Types.VARCHAR);
            statement.setInt(2, room.getId());

            statement.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(Room room) {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE room SET deleted = ? WHERE id = ? ");
            statement.setInt(1, 1);
            statement.setInt(2, room.getId());

            statement.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean banUser(Room room, int userId) {
        leaveRoom(room.getId(), userId);

        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO room_block (room_id, user_id) VALUES(?, ?)");
            statement.setInt(1, room.getId());
            statement.setInt(2, userId);
            statement.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean removeBanUser(Room room, int userId) {
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM room_block WHERE room_id = ? AND user_id = ?");
            statement.setInt(1, room.getId());
            statement.setInt(2, userId);
            statement.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
