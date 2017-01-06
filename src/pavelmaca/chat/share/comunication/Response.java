package pavelmaca.chat.share.comunication;

import java.io.Serializable;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Response implements Serializable {

    private Codes code;
    private Object body = null;

    public Response(Codes code) {
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

    public boolean hasBody() {
        return body != null;
    }

    public enum Codes {
        OK,
        ERROR,
    }

}
