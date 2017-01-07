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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoomInfo roomInfo = (RoomInfo) o;

        return id == roomInfo.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
