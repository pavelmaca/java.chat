package pavelmaca.chat.share.comunication;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class ErrorResponse extends Response {

    public ErrorResponse(String message) {
        super(Codes.ERROR);
        setBody(message);
    }
}
