package pavelmaca.chat.server.repository;

import pavelmaca.chat.server.entity.Message;
import pavelmaca.chat.server.entity.Room;
import pavelmaca.chat.server.entity.User;
import pavelmaca.chat.share.model.MessageInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class MessageRepository extends Repository {
    public MessageRepository(Connection connection) {
        super(connection);
    }

    public Message save(String text, Room roomId, User author) {
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO message (content, author_id, room_id) VALUES(?, ?, ?)", new String[]{"id"});
            statement.setString(1, text);
            statement.setInt(2, author.getId());
            statement.setInt(3, roomId.getId());
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                System.out.println("Message " + text + " saved");
                return new Message(generatedKeys.getInt(1), text, author);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<MessageInfo> getHistory(Room room, int limit) {
        ArrayList<MessageInfo> messages = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement("" +
                    "SELECT m.content, m.author_id, m.timestamp, u.name AS author_name FROM message m " +
                    "JOIN user u ON u.id = m.author_id " +
                    "WHERE m.room_id = ? " +
                    "LIMIT ?");
            statement.setInt(1, room.getId());
            statement.setInt(2, limit);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                MessageInfo messageInfo = new MessageInfo(
                        resultSet.getString("content"),
                        resultSet.getInt("author_id"),
                        resultSet.getDate("timestamp"),
                        resultSet.getString("author_name")
                );
                messages.add(messageInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }
}
