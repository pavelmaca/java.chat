package pavelmaca.chat.client;

import pavelmaca.chat.commands.Command;
import pavelmaca.chat.commands.Status;
import pavelmaca.chat.share.model.RoomInfo;
import pavelmaca.chat.share.model.RoomStatus;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
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

    private ArrayBlockingQueue<Status> responseQueue = new ArrayBlockingQueue<>(1);
    private LinkedBlockingDeque<Command> updateQueue = new LinkedBlockingDeque<>();

    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                Object inputObject = inputStream.readObject();

                if (inputObject instanceof Status) {
                    responseQueue.put((Status) inputObject);
                    //handleResponse((Status) inputObject);
                } else {
                    Command command = (Command) inputObject;
                    System.out.println("Received coomand " + command.getType());
                    updateQueue.putLast(command);
                }
            } catch (IOException | InterruptedException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public LinkedBlockingDeque<Command> getUpdateQueue() {
        return updateQueue;
    }

    /*   protected void handleResponse(Status response){
        String requestHash = response.getRequestHash();

        Command command = requestQuee.get(handleResponse);
        if(command == null){
            return;
        }

        command.onResponse(response);
    }*/

    public boolean connect(String serverIp, int serverPort) {
        try {
            socket = new Socket(serverIp, serverPort);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());

            // start listening for incoming data
            new Thread(this).start();

            // performe heandshake
            Command request = new Command(Command.Types.HAND_SHAKE);
            Status response = sendRequest(request);
            return response.getCode() == Status.Codes.OK;
        } catch (IOException e) {
            //e.printStackTrace();
            return false;
        }
    }

    public ArrayList<RoomStatus> authenticate(String username, String password) {
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
        sendRequestWithoutResponse(command);
    }

    public ArrayList<RoomInfo> getAvailableRoomList() {
        Command command = new Command(Command.Types.ROOM_GET_AVAILABLE_LIST);
        Status response = sendRequest(command);
        if (response.getCode() == Status.Codes.OK) {
            return response.getBody();
        }
        return new ArrayList<>();
    }

    public RoomStatus createRoom(String name) {
        Command command = new Command(Command.Types.ROOM_CREATE);
        command.addParametr("name", name);
        Status response = sendRequest(command);
        if (response.getCode() == Status.Codes.OK) {
            return response.getBody();
        }
        return null;
    }

    public RoomStatus joinRoom(int roomId) {
        Command command = new Command(Command.Types.USER_ROOM_JOIN);
        command.addParametr("roomId", roomId);
        Status response = sendRequest(command);
        if (response.getCode() == Status.Codes.OK) {
            return response.getBody();
        }
        return null;
    }

    private Status sendRequest(Command command) {
        // prevent making more requests until response is processed
        synchronized (outputStream) {
            try {
                outputStream.writeObject(command);
                return responseQueue.take();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        return new Status(Status.Codes.ERROR);
    }

    private synchronized void sendRequestWithoutResponse(Command command) {
        try {
            outputStream.writeObject(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        running = false;

        Command command = new Command(Command.Types.CLOSE);
      //  sendRequest(command);

        // unblock terminate update listener
        try {
            updateQueue.putFirst(command);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("c0");
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("c1");
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("c2");
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("c3");
    }

}
