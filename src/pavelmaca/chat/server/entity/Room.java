package pavelmaca.chat.server.entity;

import pavelmaca.chat.share.model.RoomInfo;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Room {
    protected String name;
    private User owner;
    private int id;

    public Room(int id, String name, User owner) {
        this.id = id;
        this.name = name;
        this.owner = owner;
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
        return new RoomInfo(id, name);
    }

}
