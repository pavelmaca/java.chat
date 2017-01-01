package pavelmaca.chat.server;

import pavelmaca.chat.commands.Command;
import pavelmaca.chat.commands.Status;
import pavelmaca.chat.server.entity.Message;
import pavelmaca.chat.server.entity.Room;
import pavelmaca.chat.server.entity.User;
import pavelmaca.chat.server.repository.MessageRepository;
import pavelmaca.chat.server.repository.RoomRepository;
import pavelmaca.chat.server.repository.UserRepository;
import pavelmaca.chat.share.model.MessageInfo;
import pavelmaca.chat.share.model.RoomInfo;
import pavelmaca.chat.share.model.RoomStatus;
import pavelmaca.chat.share.model.UserInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Session implements Runnable {

    private User user;

    private States state;

    private Socket clientSocket;
    private ObjectInputStream inputStream = null;
    private ObjectOutputStream outputStream = null;

    private RoomManager roomManager;

    private UserRepository userRepository;
    private RoomRepository roomRepository;
    private MessageRepository messageRepository;

    public Session(Socket clientSocket, RoomManager roomManager, UserRepository userRepository, RoomRepository roomRepository, MessageRepository messageRepository) {
        this.state = States.NEW;
        this.clientSocket = clientSocket;
        this.roomManager = roomManager;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    public void run() {
        try {
            inputStream = new ObjectInputStream(clientSocket.getInputStream());
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());

            while (!clientSocket.isClosed()) {
                try {
                    System.out.println("wait for next command");
                    Command command = (Command) inputStream.readObject();
                    synchronized (outputStream) { // prevent other thread send messages to client, until request is proccessed
                        processCommand(command);
                    }
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
        if (Arrays.stream(state.getAllowedCommands()).noneMatch(x -> x == command.type)) {
            System.out.println("no access to command: " + command.type);
            sendResponse(new Status(Status.Codes.ERROR));
            return;
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
            case ROOM_GET_AVAILABLE_LIST:
                handleRetriveAvalibleRoomList(command);
                break;
            case ROOM_CREATE:
                handleCreateRoom(command);
                break;
            case MESSAGE_NEW:
                handleMessageReceiver(command);
                break;
            case USER_ROOM_JOIN:
                handleJoinRoom(command);
                break;
            default:
                System.out.println("Uknown handler for command type" + command.type);
                sendResponse(new Status(Status.Codes.ERROR));
        }
    }

    private boolean sendResponse(Status response) {
        try {
            synchronized (outputStream) {
                outputStream.writeObject(response);
            }
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

    private void handleAuthentication(Command command) {
        // TODO only one connection per user
        System.out.println("authentication request received");

        Status response = new Status(Status.Codes.OK);

        String username = command.getParam("username");
        String password = command.getParam("password");

        user = userRepository.authenticate(username, password);
        if (user == null) {
            sendResponse(new Status(Status.Codes.ERROR));
            return;
        }

        ArrayList<RoomStatus> activeRoomsStatus = new ArrayList<>();
        ArrayList<Room> activeRooms = roomRepository.getActiveRooms(user);
        activeRooms.forEach(room -> {
            activeRoomsStatus.add(getRoomStatus(room));
        });


        response.setBody(activeRoomsStatus);

        if (sendResponse(response)) {
            state = States.AUTHENTICATED;
        }
    }

    private RoomStatus getRoomStatus(Room room) {
        RoomThread roomThread = roomManager.joinRoomThread(room, user, this);
        Set<User> activeUsers = roomThread.getConnectedUsers();

        ArrayList<UserInfo> userInfos = new ArrayList<>();
        activeUsers.forEach(user -> {
            userInfos.add(user.getInfoModel());
        });

        RoomStatus roomStatus = new RoomStatus(room.getInfoModel(), userInfos);

        ArrayList<MessageInfo> messageHistory = messageRepository.getHistory(room, 50);
        messageHistory.forEach(roomStatus::addMessage);

        return roomStatus;
    }

    private void handleRetriveAvalibleRoomList(Command command) {
        System.out.println("room list request received");

        ArrayList<RoomInfo> roomList = roomRepository.getAllAvailable(user);
        Status response = new Status(Status.Codes.OK);
        response.setBody(roomList);
        sendResponse(response);
    }

    private void handleCreateRoom(Command command) {
        System.out.println("new room request received");

        String roomName = command.getParam("name");
        Room room = roomRepository.createRoom(roomName, user);
        roomRepository.joinRoom(room, user);

        roomManager.joinRoomThread(room, user, this);

        Status response = new Status(Status.Codes.OK);
        response.setBody(getRoomStatus(room));
        sendResponse(response);
    }

    private void handleMessageReceiver(Command command) {
        System.out.println("new message received");

        String text = command.getParam("text");
        int roomId = command.getParam("roomId");


        if (!roomManager.isConnected(user, roomId)) {
            // user is not connected to this room!
            System.out.println("User is not connected to room " + roomId);
            return;
        }

        RoomThread roomThread = roomManager.getThread(roomId);

        Message message = messageRepository.save(text, roomThread.getRoom(), user);
        if (message != null) {
            roomThread.recieveMessage(message);
        }

        System.out.println("from: " + user.getName() + " message: " + text + " room:" + roomId);
    }

    private void handleJoinRoom(Command command) {
        System.out.println("join room request recieved");

        int roomId = command.getParam("roomId");

        Room room = roomRepository.joinRoom(roomId, user);
        roomManager.joinRoomThread(room, user, this);

        Status response = new Status(Status.Codes.OK);
        response.setBody(getRoomStatus(room));
        sendResponse(response);
    }

    public void sendDisconect() {
        sendCommand(new Command(Command.Types.CLOSE));
    }

    public void sendCommand(Command command) {
        try {
            synchronized (outputStream) {
                outputStream.writeObject(command);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        roomManager.getAllConnectedThreads(user).parallelStream().forEach(roomThread -> {
            roomThread.disconnect(user);
            roomManager.purgeRoomThread(roomThread.getRoom());
        });
    }

    enum States {
        NEW(new Command.Types[]{
                Command.Types.HAND_SHAKE,
                Command.Types.CLOSE
        }),
        GUEST(new Command.Types[]{
                Command.Types.AUTHENTICATION,
                Command.Types.CLOSE
        }),

        AUTHENTICATED(new Command.Types[]{
                Command.Types.ROOM_CREATE,
                Command.Types.ROOM_GET_AVAILABLE_LIST,
                Command.Types.USER_ROOM_JOIN,
                Command.Types.MESSAGE_NEW,
                Command.Types.CLOSE,
        });

        protected Command.Types[] allowedCommands;

        States(Command.Types[] allowedCommands) {
            this.allowedCommands = allowedCommands;
        }

        public Command.Types[] getAllowedCommands() {
            return allowedCommands;
        }
    }
}
