package pavelmaca.chat.server.repository;

import pavelmaca.chat.server.entity.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class UserRepository extends Repository {

    public UserRepository(Connection connection) {
        super(connection);
    }

    private User findByName(String name) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT id, name, password FROM user u WHERE name =  ? LIMIT 1");
            statement.setString(1, name);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return new User(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User findOneById(int id) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT id, name, password FROM user u WHERE id =  ? LIMIT 1");
            statement.setInt(1, id);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return new User(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    private User addUser(String username, String password) {
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO user (name, password) VALUES(?, ?)", new String[]{"id"});
            statement.setString(1, username);
            statement.setString(2, password);

            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                System.out.println("created new user");
                return new User(generatedKeys.getInt(1), username, password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User authenticate(String username, String password) {
        User user = findByName(username);

        // new user, create him in DB
        if (user == null) {
            user = addUser(username, password);
            if (user == null) {
                return null;
            }
        }

        if (user.comparePasswords(password)) {
            return user;
        }
        return null;
    }

    public boolean changePassword(User user, String newPassword) {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE user SET password = ? WHERE id = ? ");
            statement.setString(1, newPassword);
            statement.setInt(2, user.getId());

            statement.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
