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

    /*
    public HashMap<String, Object> getParametrs() {
        return parameters;
    }
    */

    public <R> R getParam(String key) {
        return (R) parameters.get(key);
    }

    public enum Types {
        // client outcoming
        HAND_SHAKE,
        AUTHENTICATION,
        ROOM_CREATE,
        USER_ROOM_JOIN,
        USER_ROOM_CONNECT,
        MESSAGE_NEW,
        ROOM_GET_AVAILABLE_LIST,
        GET_HISTORY,
        CLOSE,

        // client incoming
        ROOM_NEW_MESSAGE,
        ROOM_JOIN_USER,
        ROOM_LEAVE_USER,
        ROOM_CONNECT_USER,
        ROOM_DISCONNECT_USER,
    }
}
