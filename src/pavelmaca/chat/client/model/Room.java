package pavelmaca.chat.client.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Room implements Serializable {
    protected String name;
    private User owner;
    private int id;

    private ArrayList<User> userList;

    private ArrayList<Message> messages;

    public Room(String name, User owner) {
        this.name = name;
        this.owner = owner;
        this.userList = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public User getOwner() {
        return owner;
    }

    public void addUser(User user) {
        userList.add(user);
    }

    public ArrayList<User> getUserList() {
        return userList;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public int getId() {
        return id;
    }

    public static class Pair implements Serializable{
        public int id;
        public String name;

        public Pair(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
