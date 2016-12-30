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

    public void addParametr(String key, Object value){
        parameters.put(key, value);
    }

    public HashMap<String, Object> getParametrs(){
        return parameters;
    }

    public enum Types {
        HAND_SHAKE,
        AUTHENTICATION,
        GET_IDENTITY,
        CLOSE,
    }
}
