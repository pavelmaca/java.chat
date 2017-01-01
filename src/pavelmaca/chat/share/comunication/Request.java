package pavelmaca.chat.share.comunication;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Request implements Serializable {
    private HashMap<String, Object> parameters;
    Code code;

    public Request(Code code) {
        this.code = code;
        parameters = new HashMap<>();
    }

    public void addParametr(String key, Object value) {
        parameters.put(key, value);
    }

    public <R> R getParam(String key) {
        return (R) parameters.get(key);
    }

    public enum Code {
        HAND_SHAKE,
        AUTHENTICATION,
        ROOM_CREATE,
        JOIN_ROOM,
        MESSAGE_NEW,
        ROOM_GET_AVAILABLE_LIST,
        CLOSE;
    }
}


