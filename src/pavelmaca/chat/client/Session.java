package pavelmaca.chat.client;

import pavelmaca.chat.client.model.User;
import pavelmaca.chat.commands.Command;
import pavelmaca.chat.commands.Status;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

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

    public boolean authenticate(String username, String password) {
        Command command = new Command(Command.Types.AUTHENTICATION);
        command.addParametr("username", username);
        command.addParametr("password", password);

        Status response = sendRequest(command);

        return response.getCode() == Status.Codes.OK;
    }

    public User getIdentity() {
        Command command = new Command(Command.Types.GET_IDENTITY);

        Status response = sendRequest(command);
        if (response.getCode() == Status.Codes.OK) {
            return (User) response.getBody();
        }
        return null;
    }

    private Status sendRequest(Command command) {
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
