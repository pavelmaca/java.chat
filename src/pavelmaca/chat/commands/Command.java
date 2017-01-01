package pavelmaca.chat.commands;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Command implements Serializable {
    public Types type;

    protected HashMap<String, Object> parameters;

    public Command(Types type) {
        this.type = type;
        parameters = new HashMap<>();
    }

    public void addParametr(String key, Object value) {
        parameters.put(key, value);
    }

    public <R> R getParam(String key) {
        return (R) parameters.get(key);
    }

    public Types getType() {
        return type;
    }

    public boolean hasParam(String authorId) {
        return parameters.containsKey(authorId);
    }

    public enum Types {
        // client -> server
        HAND_SHAKE,
        AUTHENTICATION,
        ROOM_CREATE,
        USER_ROOM_JOIN,
        MESSAGE_NEW,
        ROOM_GET_AVAILABLE_LIST,
        CLOSE,

        // server -> client
        ROOM_NEW_MESSAGE,
        ROOM_USER_CONNECTED,
        ROOM_USER_DISCONNECTED, DUMMY,
      /*  ROOM_USER_JOINED,
        ROOM_USER_LEAVE,*/
    }
}
