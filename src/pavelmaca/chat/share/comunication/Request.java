package pavelmaca.chat.share.comunication;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Request implements Serializable {
    public Types type;

    private HashMap<String, Object> parameters;

    public Request(Types type) {
        this.type = type;
        parameters = new HashMap<>();
    }

    public void addParameter(String key, Object value) {
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

    @Override
    public String toString() {
       return "Request{" +
                "type=" + type +
                ", parameters=" + parameters +
                '}';
    }

    public enum Types {
        // client -> server
        HAND_SHAKE,
        AUTHENTICATION,
        ROOM_CREATE,
        ROOM_USER_JOIN,
        MESSAGE_NEW,
        ROOM_GET_AVAILABLE_LIST,
        CLOSE,

        // server -> client
        ROOM_USER_CONNECTED,
        ROOM_USER_DISCONNECTED,
        DUMMY,
        LOGOUT,
        USER_CHANGE_PASSWORD,
        USER_STATUS,
        ROOM_USER_LEAVE, ROOM_CHANHE_NAME, ROOM_CHANGE_PASSWORD, ROOM_REMOVE_PASSWORD, ROOM_DELETE, ROOM_USER_BAN, ROOM_USER_BAN_REMOVE,
      /*  ROOM_USER_JOINED,
        ROOM_USER_LEAVE,*/
    }
}
