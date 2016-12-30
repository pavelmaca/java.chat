package pavelmaca.chat.server.repository;

import pavelmaca.chat.client.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class UserRepository {
    Connection connection;

    public UserRepository(Connection connection) {
        this.connection = connection;
    }

    private User findByName(String name) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT name, password FROM user u WHERE name =  ? LIMIT 1");
            statement.setString(1, name);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return new User(resultSet.getString(1), resultSet.getString(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean addUser(User user) {
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO user (name, password) VALUES(?, ?)");
            statement.setString(1, user.getName());
            statement.setString(2, user.getPassword());
            statement.executeUpdate();
            System.out.println("created new user");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public User authenticate(String username, String password) {
        User user = findByName(username);

        // new user, create him in DB
        if (user == null) {
            user = new User(username, password);
            if (!addUser(user)) {
                return null;
            }
        }

        if (user.comparePasswords(password)) {
            return user;
        }
        return null;
    }
}
