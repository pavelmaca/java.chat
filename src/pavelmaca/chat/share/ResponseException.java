package pavelmaca.chat.share;

import pavelmaca.chat.share.comunication.Response;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class ResponseException extends Exception {

    private Response response;

    public ResponseException(Response response) {
        this.response = response;
    }

    @Override
    public String getMessage() {
        return response.getBody();
    }
}
