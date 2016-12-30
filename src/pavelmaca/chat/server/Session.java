package pavelmaca.chat.server;

import pavelmaca.chat.client.model.User;
import pavelmaca.chat.commands.Command;
import pavelmaca.chat.commands.Status;
import pavelmaca.chat.server.repository.UserRepository;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Session implements Runnable {

    private User user;

    private States state;

    private Socket clientSocket;
    private ObjectInputStream inputStream = null;
    private ObjectOutputStream outputStream = null;

    private UserRepository userRepository;

    public Session(Socket clientSocket, UserRepository userRepository) {
        this.state = States.NEW;
        this.clientSocket = clientSocket;
        this.userRepository = userRepository;
    }

    @Override
    public void run() {
        try {
            inputStream = new ObjectInputStream(clientSocket.getInputStream());
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());

            // TODO add socket to room threads, for relaying new messages

            while (!clientSocket.isClosed()) {
                try {
                    System.out.println("wait for next command");
                    Command command = (Command) inputStream.readObject();
                    processCommand(command);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            close();
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }

    private void processCommand(Command command) {
        if (!Arrays.stream(state.getAllowedCommands()).anyMatch(x -> x == command.type)) {
            System.out.println("no access to command: " + command.type);
            sendResponse(new Status(Status.Codes.ERROR));
        }

        switch (command.type) {
            case CLOSE:
                close();
                break;
            case HAND_SHAKE:
                handleHandShake(command);
                break;
            case AUTHENTICATION:
                handleAuthentication(command);
                break;
            case GET_IDENTITY:
                handleGetIdentity(command);
                break;
            default:
                System.out.println("Uknown handler for command type" + command.type);
                sendResponse(new Status(Status.Codes.ERROR));
        }

        // TODO run authentication -> setup User

        // TODO send all connected rooms -> add user to all room threads

        // TODO: klient posílá zprávu + místnost do která chce zprávu odeslat
        // TODO musí proběhnout kontrola oprávnění
    }

    protected boolean sendResponse(Status response) {
        try {
            outputStream.writeObject(response);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    private void handleHandShake(Command command) {
        System.out.println("hand shake received");
        if (sendResponse(new Status(Status.Codes.OK))) {
            state = States.GUEST;
        }
    }

    protected void handleAuthentication(Command command) {
        System.out.println("authentication request received");

        // TODO check login credencials
        Status response = new Status(Status.Codes.OK);
        HashMap<String, Object> params = command.getParametrs();

        String username = (String) params.get("username");
        String password = (String) params.get("password");

        user = userRepository.authenticate(username, password);
        if (user == null) {
            sendResponse(new Status(Status.Codes.ERROR));
            return;
        }
        response.setBody(user);

        if (sendResponse(response)) {
            state = States.AUTHENTICATED;
        }
    }

    protected void handleGetIdentity(Command command) {
        System.out.println("identity request received");

        Status response = new Status(Status.Codes.OK);
        response.setBody(user);
        sendResponse(response);
    }

    private void close() {
        System.out.println("closing session");
        try {
            if (inputStream != null)
                inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (outputStream != null)
                outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    enum States {
        NEW(new Command.Types[]{Command.Types.HAND_SHAKE, Command.Types.CLOSE}),
        GUEST(new Command.Types[]{Command.Types.AUTHENTICATION, Command.Types.CLOSE}),
        AUTHENTICATED(new Command.Types[]{Command.Types.CLOSE, Command.Types.GET_IDENTITY});

        protected Command.Types[] allowedCommands;

        States(Command.Types[] allowedCommands) {
            this.allowedCommands = allowedCommands;
        }

        public Command.Types[] getAllowedCommands() {
            return allowedCommands;
        }
    }
}
