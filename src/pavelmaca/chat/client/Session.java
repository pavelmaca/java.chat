package pavelmaca.chat.client;

import pavelmaca.chat.share.ResponseException;
import pavelmaca.chat.share.comunication.Request;
import pavelmaca.chat.share.comunication.Response;
import pavelmaca.chat.share.model.RoomInfo;
import pavelmaca.chat.share.model.RoomStatus;
import pavelmaca.chat.share.model.UserInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Thread listening for input requests in the background of GUI
 * creating queue for GUIRequestListener
 *
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Session implements Runnable {

    private Socket socket;
    private ObjectOutputStream outputStream = null;
    private ObjectInputStream inputStream = null;

    private boolean running;

    private ArrayBlockingQueue<Response> responseQueue = new ArrayBlockingQueue<>(1);
    private LinkedBlockingDeque<Request> updateQueue = new LinkedBlockingDeque<>();

    // used to unlock blocked threads after unexpected socket close event
    private static final Request DUMMY = new Request(Request.Types.DUMMY);

    @Override
    public void run() {
        running = true;
        while (running && !socket.isClosed()) {
            try {
                Object inputObject = inputStream.readObject();

                if (inputObject instanceof Response) {
                    responseQueue.put((Response) inputObject);
                } else {
                    Request request = (Request) inputObject;
                    System.out.println("received " + request); // debug output
                    updateQueue.putLast(request);
                }
            } catch (IOException | InterruptedException | ClassNotFoundException e) {
                if (e instanceof SocketException && e.getMessage().equals("Connection reset")) {
                    running = false; //stop after server is closed
                }
                e.printStackTrace();
            }
        }

        if (socket.isClosed()) {
            running = false;
        }

        // unblock all thread locked on updateQueue
        try {
            updateQueue.putLast(DUMMY);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public LinkedBlockingDeque<Request> getUpdateQueue() {
        return updateQueue;
    }

    /**
     * Connect to server and make hand shake to confirm integrity
     *
     * @param serverIp
     * @param serverPort
     * @return true on success
     */
    public boolean connect(String serverIp, int serverPort) {
        try {
            socket = new Socket(serverIp, serverPort);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());

            // start listening for incoming data
            new Thread(this).start();

            // performe heandshake
            Request request = new Request(Request.Types.HAND_SHAKE);
            Response response = sendRequest(request);
            return response.getCode() == Response.Codes.OK;
        } catch (IOException e) {
            //e.printStackTrace();
            return false;
        }
    }

    /**
     * Login user
     *
     * @param username
     * @param password
     * @return User info or NULL
     */
    public UserInfo authenticate(String username, String password) {
        Request request = new Request(Request.Types.AUTHENTICATION);
        request.addParameter("username", username);
        request.addParameter("password", password);

        Response response = sendRequest(request);

        if (response.getCode() == Response.Codes.OK) {
            return response.getBody();
        }
        return null;
    }

    /**
     * @return History for all connected rooms for initial setup
     */
    public HashMap<Integer, RoomStatus> getStatus() {
        Request request = new Request(Request.Types.USER_STATUS);

        Response response = sendRequest(request);

        if (response.getCode() == Response.Codes.OK) {
            return response.getBody();
        }
        return null;
    }

    /**
     * Send new message to server
     *
     * @param text
     * @param roomId
     */
    public void sendMessage(String text, int roomId) {
        Request request = new Request(Request.Types.MESSAGE_NEW);
        request.addParameter("text", text);
        request.addParameter("roomId", roomId);
        sendRequestWithoutResponse(request);
    }

    /**
     * @return all available rooms for current user
     */
    public ArrayList<RoomInfo> getAvailableRoomList() {
        Request request = new Request(Request.Types.ROOM_GET_AVAILABLE_LIST);
        Response response = sendRequest(request);
        if (response.getCode() == Response.Codes.OK) {
            return response.getBody();
        }
        return new ArrayList<>();
    }

    /**
     * Create new room
     *
     * @param name
     * @param password
     * @return Room info
     */
    public RoomStatus createRoom(String name, String password) {
        Request request = new Request(Request.Types.ROOM_CREATE);
        request.addParameter("name", name);
        request.addParameter("password", password);
        Response response = sendRequest(request);
        if (response.getCode() == Response.Codes.OK) {
            return response.getBody();
        }
        return null;
    }

    /**
     * Join existing room
     *
     * @param roomId
     * @param password
     * @return Room info
     * @throws ResponseException on server error
     */
    public RoomStatus joinRoom(int roomId, String password) throws ResponseException {
        Request request = new Request(Request.Types.ROOM_USER_JOIN);
        request.addParameter("roomId", roomId);
        request.addParameter("password", password);
        Response response = sendRequest(request);
        if (response.getCode() == Response.Codes.OK) {
            return response.getBody();
        } else {
            throw new ResponseException(response);
        }
    }

    /**
     * Leave room
     *
     * @param roomId
     */
    public void leaveRoom(int roomId) {
        Request request = new Request(Request.Types.ROOM_USER_LEAVE);
        request.addParameter("roomId", roomId);
        sendRequestWithoutResponse(request);
    }

    /**
     * Send request to server and expecting response
     * Block output stream, to prevent conflict with incoming responses
     *
     * @param request
     * @return server response
     */
    private Response sendRequest(Request request) {
        // prevent making more requests until response is processed
        synchronized (outputStream) {
            try {
                outputStream.writeObject(request);
                return responseQueue.take();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        return new Response(Response.Codes.ERROR);
    }

    /**
     * Send request to server, without response
     *
     * @param request
     */
    private synchronized void sendRequestWithoutResponse(Request request) {
        try {
            outputStream.writeObject(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Logout user
     */
    public void logout() {
        sendRequestWithoutResponse(new Request(Request.Types.LOGOUT));
    }

    /**
     * Close socket and all connections
     */
    public void close() {
        running = false;

        // unblock terminate update listener
        try {
            Request request = new Request(Request.Types.CLOSE);
            //sendRequest(request);
            updateQueue.putFirst(request);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Change user password
     *
     * @param newPassword
     * @return true on success
     */
    public boolean changePassword(String newPassword) {
        Request request = new Request(Request.Types.USER_CHANGE_PASSWORD);
        request.addParameter("password", newPassword);
        Response response = sendRequest(request);
        return response.getCode() == Response.Codes.OK;
    }

    /**
     * Rename room
     *
     * @param roomId
     * @param newRoomName
     * @return true on success
     */
    public boolean roomChangeName(int roomId, String newRoomName) {
        Request request = new Request(Request.Types.ROOM_CHANHE_NAME);
        request.addParameter("name", newRoomName);
        request.addParameter("roomId", roomId);
        Response response = sendRequest(request);
        return response.getCode() == Response.Codes.OK;
    }

    /**
     * Change password for room
     *
     * @param roomId
     * @param newPassword
     * @throws ResponseException on server error
     */
    public void roomChangePassword(int roomId, String newPassword) throws ResponseException {
        Request request = new Request(Request.Types.ROOM_CHANGE_PASSWORD);
        request.addParameter("password", newPassword);
        request.addParameter("roomId", roomId);
        Response response = sendRequest(request);
        if (response.getCode() != Response.Codes.OK) {
            throw new ResponseException(response);
        }
    }

    /**
     * Remove password from the room
     *
     * @param roomId
     * @throws ResponseException on server error
     */
    public void roomRemovePassword(int roomId) throws ResponseException {
        Request request = new Request(Request.Types.ROOM_REMOVE_PASSWORD);
        request.addParameter("roomId", roomId);
        Response response = sendRequest(request);
        if (response.getCode() != Response.Codes.OK) {
            throw new ResponseException(response);
        }
    }

    /**
     * Delete room
     *
     * @param roomId
     * @throws ResponseException on server error
     */
    public void roomDelete(int roomId) throws ResponseException {
        Request request = new Request(Request.Types.ROOM_DELETE);
        request.addParameter("roomId", roomId);
        Response response = sendRequest(request);
        if (response.getCode() != Response.Codes.OK) {
            throw new ResponseException(response);
        }
    }

    /**
     * Ban user in room
     *
     * @param roomId
     * @param userId
     * @throws ResponseException on server error
     */
    public void userBan(int roomId, int userId) throws ResponseException {
        Request request = new Request(Request.Types.ROOM_USER_BAN);
        request.addParameter("roomId", roomId);
        request.addParameter("userId", userId);
        Response response = sendRequest(request);
        if (response.getCode() != Response.Codes.OK) {
            throw new ResponseException(response);
        }
    }

    /**
     * Remove user ban in room
     *
     * @param roomId
     * @param userId
     * @throws ResponseException on server error
     */
    public void userRemoveBan(int roomId, int userId) throws ResponseException {
        Request request = new Request(Request.Types.ROOM_USER_BAN_REMOVE);
        request.addParameter("roomId", roomId);
        request.addParameter("userId", userId);
        Response response = sendRequest(request);
        if (response.getCode() != Response.Codes.OK) {
            throw new ResponseException(response);
        }
    }
}
