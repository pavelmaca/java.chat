package pavelmaca.chat.share.model;

import pavelmaca.chat.server.entity.User;

import java.io.Serializable;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class UserInfo implements Serializable {
    private int id;
    private String name;

    public UserInfo(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public  static UserInfo fromEntity(User user){
        return new UserInfo(user.getId(), user.getName());
    }
}
