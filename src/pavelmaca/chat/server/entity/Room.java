package pavelmaca.chat.server.entity;

import pavelmaca.chat.share.model.RoomInfo;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Room {
    protected String name;
    private User owner;
    private int id;
    private String password;

    public Room(int id, String name, User owner, String password) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public User getOwner() {
        return owner;
    }

    public int getId() {
        return id;
    }

    public RoomInfo getInfoModel() {
        boolean hasPassword = !(password == null || password.isEmpty());
        return new RoomInfo(id, name, hasPassword);
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPasswordValid(String password) {
        return this.password.equals(password);
    }
}
