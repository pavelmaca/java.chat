package pavelmaca.chat.share;

import pavelmaca.chat.share.comunication.Response;

/**
 * Created by Assassik on 07.01.2017.
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
