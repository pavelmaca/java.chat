package pavelmaca.chat.commands;

import java.io.Serializable;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Status implements Serializable {

    private Codes code;
    private Object body;

    public Status(Codes code) {
        this.code = code;
    }

    public Codes getCode() {
        return code;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public <R> R getBody() {
        return (R) body;
    }

    public enum Codes {
        OK,
        ERROR,
    }

}
