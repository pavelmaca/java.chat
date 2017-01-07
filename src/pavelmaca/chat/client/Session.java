package pavelmaca.chat.client;

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
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Session implements Runnable {

    private Socket socket;
    private ObjectOutputStream outputStream = null;
    private ObjectInputStream inputStream = null;

    private boolean running;

    private ArrayBlockingQueue<Response> responseQueue = new ArrayBlockingQueue<>(1);
    private LinkedBlockingDeque<Request> updateQueue = new LinkedBlockingDeque<>();

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
                    System.out.println("Received coomand " + request.getType());
                    updateQueue.putLast(request);
                }
            } catch (IOException | InterruptedException | ClassNotFoundException e) {
                if (e instanceof SocketException && e.getMessage().equals("Connection reset")) {
                    running = false;
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

    public HashMap<Integer, RoomStatus> getStatus() {
        Request request = new Request(Request.Types.USER_STATUS);

        Response response = sendRequest(request);

        if (response.getCode() == Response.Codes.OK) {
            return response.getBody();
        }
        return null;
    }

    public void sendMessage(String text, int roomId) {
        Request request = new Request(Request.Types.MESSAGE_NEW);
        request.addParameter("text", text);
        request.addParameter("roomId", roomId);
        sendRequestWithoutResponse(request);
    }

    public ArrayList<RoomInfo> getAvailableRoomList() {
        Request request = new Request(Request.Types.ROOM_GET_AVAILABLE_LIST);
        Response response = sendRequest(request);
        if (response.getCode() == Response.Codes.OK) {
            return response.getBody();
        }
        return new ArrayList<>();
    }

    public RoomStatus createRoom(String name) {
        Request request = new Request(Request.Types.ROOM_CREATE);
        request.addParameter("name", name);
        Response response = sendRequest(request);
        if (response.getCode() == Response.Codes.OK) {
            return response.getBody();
        }
        return null;
    }

    public RoomStatus joinRoom(int roomId) {
        Request request = new Request(Request.Types.ROOM_USER_JOIN);
        request.addParameter("roomId", roomId);
        Response response = sendRequest(request);
        if (response.getCode() == Response.Codes.OK) {
            return response.getBody();
        }
        return null;
    }

    public void leaveRoom(int roomId) {
        Request request = new Request(Request.Types.ROOM_USER_LEAVE);
        request.addParameter("roomId", roomId);
        sendRequestWithoutResponse(request);
    }

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

    private synchronized void sendRequestWithoutResponse(Request request) {
        try {
            outputStream.writeObject(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logout() {
        sendRequestWithoutResponse(new Request(Request.Types.LOGOUT));
    }

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

    public boolean changePassword(String newPassword) {
        Request request = new Request(Request.Types.USER_CHANGE_PASSWORD);
        request.addParameter("password", newPassword);
        Response response = sendRequest(request);
        if (response.getCode() == Response.Codes.OK) {
            return true;
        }
        return false;
    }
}
