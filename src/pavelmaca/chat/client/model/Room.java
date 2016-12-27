package pavelmaca.chat.client.model;

import java.util.ArrayList;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Room {
    protected String name;
    private User owner;

    private ArrayList<User> userList;

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

    public void addUser(User user){
        userList.add(user);
    }

    public ArrayList<User> getUserList(){
        return userList;
    }
}
