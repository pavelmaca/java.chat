package pavelmaca.chat.share.model;

import java.io.Serializable;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class RoomInfo implements Serializable {
    private int id;
    private String name;
    private int ownerId;

    public RoomInfo(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
