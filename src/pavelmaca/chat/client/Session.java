package pavelmaca.chat.client;

import pavelmaca.chat.client.model.Room;
import pavelmaca.chat.client.model.User;
import pavelmaca.chat.commands.Command;
import pavelmaca.chat.commands.Status;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Session {

    private Socket socket;
    private ObjectOutputStream outputStream = null;
    private ObjectInputStream inputStream = null;

    public boolean connect(String serverIp, int serverPort) {
        try {
            socket = new Socket(serverIp, serverPort);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());

            // performe heandshake
            Command request = new Command(Command.Types.HAND_SHAKE);
            Status response = sendRequest(request);
            return response.getCode() == Status.Codes.OK;
        } catch (IOException e) {
            //e.printStackTrace();
            return false;
        }
    }

    public User authenticate(String username, String password) {
        Command command = new Command(Command.Types.AUTHENTICATION);
        command.addParametr("username", username);
        command.addParametr("password", password);

        Status response = sendRequest(command);

        if (response.getCode() == Status.Codes.OK) {
            return response.getBody();
        }
        return null;
    }

    public void sendMessage(String text, int roomId) {
        Command command = new Command(Command.Types.MESSAGE_NEW);
        command.addParametr("text", text);
        command.addParametr("roomId", roomId);
        sendRequest(command);
    }

    public ArrayList<Room.Pair> getAvailableRoomList() {
        Command command = new Command(Command.Types.ROOM_GET_LIST);
        Status response = sendRequest(command);
        if (response.getCode() == Status.Codes.OK) {
            return response.getBody();
        }
        return new ArrayList<>();
    }

    public Room createRoom(String name) {
        Command command = new Command(Command.Types.ROOM_CREATE);
        command.addParametr("name", name);
        Status response = sendRequest(command);
        if (response.getCode() == Status.Codes.OK) {
            return response.getBody();
        }
        return null;
    }

    public Room joinRoom(int roomId){
        Command command = new Command(Command.Types.USER_JOIN_ROOM);
        command.addParametr("roomId", roomId);
        Status response = sendRequest(command);
        if (response.getCode() == Status.Codes.OK) {
            return response.getBody();
        }
        return null;
    }

    private synchronized Status sendRequest(Command command) {
        try {
            outputStream.writeObject(command);
            return (Status) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new Status(Status.Codes.ERROR);
    }

    public void close() {
        Command command = new Command(Command.Types.CLOSE);
        sendRequest(command);

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


}
