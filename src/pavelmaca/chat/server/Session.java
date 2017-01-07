package pavelmaca.chat.server;

import pavelmaca.chat.share.Lambdas;
import pavelmaca.chat.share.comunication.ErrorResponse;
import pavelmaca.chat.share.comunication.Request;
import pavelmaca.chat.share.comunication.Response;
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
import java.util.*;

/**
 * Represent client connection via socket and handle all incoming requests
 *
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Session implements Runnable {

    /**
     * Connected user
     */
    private User currentUser;

    /**
     * Session state
     */
    private States state;

    private Socket clientSocket;
    private ObjectInputStream inputStream = null;
    private ObjectOutputStream outputStream = null;

    private RoomManager roomManager;

    private UserRepository userRepository;
    private RoomRepository roomRepository;
    private MessageRepository messageRepository;

    /**
     * Map of request handlers by Types
     */
    private HashMap<Request.Types, Lambdas.Function1<Request>> requestHandlers = new HashMap<>();

    public Session(Socket clientSocket, RoomManager roomManager, UserRepository userRepository, RoomRepository roomRepository, MessageRepository messageRepository) {
        this.state = States.NEW;
        this.clientSocket = clientSocket;
        this.roomManager = roomManager;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.messageRepository = messageRepository;

        // Setup handlers
        requestHandlers.put(Request.Types.HAND_SHAKE, this::handleHandShake);
        requestHandlers.put(Request.Types.AUTHENTICATION, this::handleAuthentication);
        requestHandlers.put(Request.Types.USER_STATUS, this::handleUserStatus);
        requestHandlers.put(Request.Types.CLOSE, request -> this.closeSession());
        requestHandlers.put(Request.Types.ROOM_GET_AVAILABLE_LIST, this::handleRetrieveAvailableRoomList);
        requestHandlers.put(Request.Types.ROOM_CREATE, this::handleCreateRoom);
        requestHandlers.put(Request.Types.MESSAGE_NEW, this::handleMessageReceiver);
        requestHandlers.put(Request.Types.ROOM_USER_JOIN, this::handleJoinRoom);
        requestHandlers.put(Request.Types.ROOM_USER_LEAVE, this::handleLeaveRoom);
        requestHandlers.put(Request.Types.LOGOUT, this::handleLogout);
        requestHandlers.put(Request.Types.USER_CHANGE_PASSWORD, this::handleChangeUserPassword);
        requestHandlers.put(Request.Types.ROOM_CHANGE_NAME, this::handleChangeRoomName);
        requestHandlers.put(Request.Types.ROOM_CHANGE_PASSWORD, this::handleChangeRoomPassword);
        requestHandlers.put(Request.Types.ROOM_REMOVE_PASSWORD, this::handleRemoveRoomPassword);
        requestHandlers.put(Request.Types.ROOM_DELETE, this::handleDeleteRoom);
        requestHandlers.put(Request.Types.ROOM_USER_BAN, this::handleBanRoomUser);
        requestHandlers.put(Request.Types.ROOM_USER_BAN_REMOVE, this::handleRemoveBanRoomUser);
    }


    @Override
    public void run() {
        try {
            inputStream = new ObjectInputStream(clientSocket.getInputStream());
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());

            while (!clientSocket.isClosed()) {
                try {
                    Request request = (Request) inputStream.readObject();
                    synchronized (outputStream) { // prevent other thread send messages to client, until request is processed
                        processRequest(request);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            closeSession();
        } catch (IOException e) {
            e.printStackTrace();
            closeSession();
        }
    }

    /**
     * Process request from client
     *
     * @param request
     */
    private void processRequest(Request request) {
        System.out.println("received: " + request); // debug output

        // check access to request type
        if (Arrays.stream(state.getAllowedCommands()).noneMatch(x -> x == request.type)) {
            sendResponse(new ErrorResponse("No access to request: " + request.type));
            return;
        }

        if (!requestHandlers.containsKey(request.getType())) {
            sendResponse(new ErrorResponse("Unknown request type" + request.type));
            return;
        }

        // call handler to precess request
        Lambdas.Function1 handler = requestHandlers.get(request.getType());
        handler.apply(request);
    }

    private boolean sendResponse(Response response) {
        // debug: print outgoing error responses
        if (response.hasBody() && response.getCode() == Response.Codes.ERROR) {
            System.out.println("user " + currentUser.getId() + " - " + response.getBody());
        }

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

    /// client request handlers

    private void handleHandShake(Request request) {
        if (sendResponse(new Response(Response.Codes.OK))) {
            state = States.GUEST;
        }
    }

    private void handleAuthentication(Request request) {
        Response response = new Response(Response.Codes.OK);

        String username = request.getParam("username");
        String password = request.getParam("password");

        currentUser = userRepository.authenticate(username, password);
        if (currentUser == null) {
            sendResponse(new Response(Response.Codes.ERROR));
            return;
        }

        response.setBody(currentUser.getInfoModel());
        if (sendResponse(response)) {
            state = States.AUTHENTICATED;
        }

    }

    private void handleUserStatus(Request request) {
        HashMap<Integer, RoomStatus> activeRoomsStatus = new HashMap<>();
        ArrayList<Room> activeRooms = roomRepository.getActiveRooms(currentUser);
        activeRooms.forEach(room -> {
            RoomStatus roomStatus = getRoomStatus(room);
            activeRoomsStatus.put(room.getId(), roomStatus);
        });

        Response response = new Response(Response.Codes.OK);
        response.setBody(activeRoomsStatus);
        sendResponse(response);
    }

    private RoomStatus getRoomStatus(Room room) {
        RoomThread roomThread = roomManager.joinRoomThread(room, currentUser, this);

        HashMap<Integer, User> connectedUsers = new HashMap<>();
        roomThread.getConnectedUsers().forEach(user -> connectedUsers.put(user.getId(), user));

        TreeSet<UserInfo> userList = new TreeSet<>();
        roomRepository.getConnectedUsers(room).forEach(
                user -> {
                    UserInfo userInfo = user.getInfoModel();
                    if (connectedUsers.containsKey(userInfo.getId())) {
                        userInfo.setStatus(UserInfo.Status.ONLINE);
                    } else {
                        userInfo.setStatus(UserInfo.Status.OFFLINE);
                    }

                    if (room.getOwner().getId() == user.getId()) {
                        userInfo.setRank(UserInfo.Rank.OWNER);
                    } else {
                        userInfo.setRank(UserInfo.Rank.MEMBER);
                    }
                    userList.add(userInfo);
                });

        //load banned users
        userList.addAll(roomRepository.getBannedUsers(room));

        RoomStatus roomStatus = new RoomStatus(room.getInfoModel(), userList);

        ArrayList<MessageInfo> messageHistory = messageRepository.getHistory(room, 50);
        messageHistory.forEach(roomStatus::addMessage);

        return roomStatus;
    }

    private void handleRetrieveAvailableRoomList(Request request) {
        System.out.println("room list request received");

        ArrayList<RoomInfo> roomList = roomRepository.getAllAvailable(currentUser);
        Response response = new Response(Response.Codes.OK);
        response.setBody(roomList);
        sendResponse(response);
    }

    private void handleCreateRoom(Request request) {
        System.out.println("new room request received");

        String roomName = request.getParam("name");
        String roomPassword = request.getParam("password");
        Room room = roomRepository.createRoom(roomName, currentUser, roomPassword);
        roomRepository.joinRoom(room, currentUser);

        roomManager.joinRoomThread(room, currentUser, this);

        Response response = new Response(Response.Codes.OK);
        response.setBody(getRoomStatus(room));
        sendResponse(response);
    }

    private void handleMessageReceiver(Request request) {
        System.out.println("new message received");

        String text = request.getParam("text");
        int roomId = request.getParam("roomId");


        if (!roomManager.isConnected(currentUser, roomId)) {
            // user is not connected to this room!
            System.out.println("User is not connected to room " + roomId);
            return;
        }

        RoomThread roomThread = roomManager.getThread(roomId);

        Message message = messageRepository.save(text, roomThread.getRoom(), currentUser);
        if (message != null) {
            roomThread.receiveMessage(message);
        }

        System.out.println("from: " + currentUser.getName() + " message: " + text + " room:" + roomId);
    }

    private void handleJoinRoom(Request request) {
        System.out.println("join room request received");

        int roomId = request.getParam("roomId");
        String password = request.getParam("password");

        Room room = roomRepository.findOneById(roomId);
        if (room == null) {
            sendResponse(new ErrorResponse("Invalid room ID"));
            return;
        }

        if (!room.isPasswordValid(password)) {
            sendResponse(new ErrorResponse("Invalid password"));
            return;
        }

        roomRepository.joinRoom(room, currentUser);
        roomManager.joinRoomThread(room, currentUser, this);

        Response response = new Response(Response.Codes.OK);
        response.setBody(getRoomStatus(room));
        sendResponse(response);
    }

    private void handleLeaveRoom(Request request) {
        System.out.println("leave room request received");

        int roomId = request.getParam("roomId");

        roomRepository.leaveRoom(roomId, currentUser.getId());

        RoomThread roomThread = roomManager.getThread(roomId);
        roomThread.leave(currentUser, false);
        roomManager.purgeRoomThread(roomThread.getRoom());
    }


    private void handleLogout(Request request) {
        state = States.GUEST;
        roomManager.getAllConnectedThreads(currentUser).parallelStream().forEach(roomThread -> {
            roomThread.disconnect(currentUser, false);
            roomManager.purgeRoomThread(roomThread.getRoom());
        });
    }

    private void handleChangeUserPassword(Request request) {
        String newPassword = request.getParam("password");
        if (newPassword.length() > 0) {
            boolean status = userRepository.changePassword(currentUser, newPassword);
            if (status) {
                sendResponse(new Response(Response.Codes.OK));
                return;
            }
        }
        sendResponse(new ErrorResponse("Cant change password, try again later."));
    }

    private void handleChangeRoomName(Request request) {
        String newName = request.getParam("name");
        int roomId = request.getParam("roomId");
        RoomThread roomThread = roomManager.getThread(roomId);
        Room room = roomThread.getRoom();
        if (newName.length() > 0 && room.getOwner().getId() == currentUser.getId()) {
            boolean status = roomRepository.changeName(room, newName);
            if (status) {
                roomThread.changeRoomName(newName);
                sendResponse(new Response(Response.Codes.OK));
                return;
            }
        }
        sendResponse(new ErrorResponse("Can't change password, try again later."));
    }

    private void handleChangeRoomPassword(Request request) {
        String newPassword = request.getParam("password");
        int roomId = request.getParam("roomId");
        RoomThread roomThread = roomManager.getThread(roomId);
        Room room = roomThread.getRoom();
        if (newPassword.length() > 0 && room.getOwner().getId() == currentUser.getId()) {
            boolean status = roomRepository.changePassword(room, newPassword);
            if (status) {
                room.setPassword(newPassword);
                sendResponse(new Response(Response.Codes.OK));
                return;
            }
        }
        sendResponse(new ErrorResponse("Cant change password, try again later."));
    }

    private void handleRemoveRoomPassword(Request request) {
        int roomId = request.getParam("roomId");
        RoomThread roomThread = roomManager.getThread(roomId);
        Room room = roomThread.getRoom();
        if (room.getOwner().getId() == currentUser.getId()) {
            boolean status = roomRepository.removePassword(room);
            if (status) {
                room.setPassword(null);
                sendResponse(new Response(Response.Codes.OK));
                return;
            }
        } else {
            sendResponse(new ErrorResponse("Not allowed"));
            return;
        }

        sendResponse(new ErrorResponse("Cant change password, try again later."));
    }

    private void handleDeleteRoom(Request request) {
        int roomId = request.getParam("roomId");
        RoomThread roomThread = roomManager.getThread(roomId);
        Room room = roomThread.getRoom();
        if (room.getOwner().getId() == currentUser.getId()) {
            boolean status = roomRepository.delete(room);
            if (status) {
                roomThread.disconnectAll();
                roomManager.purgeRoomThread(room);
                sendResponse(new Response(Response.Codes.OK));
                return;
            }
        } else {
            sendResponse(new ErrorResponse("Not allowed"));
            return;
        }

        sendResponse(new ErrorResponse("Cant delete room, try again later."));
    }

    private void handleRemoveBanRoomUser(Request request) {
        int roomId = request.getParam("roomId");
        int userId = request.getParam("userId");
        RoomThread roomThread = roomManager.getThread(roomId);
        Room room = roomThread.getRoom();
        if (room.getOwner().getId() == currentUser.getId()) {
            boolean status = roomRepository.removeBanUser(room, userId);
            if (status) {
                roomThread.removeUserBan(userId);
                sendResponse(new Response(Response.Codes.OK));
                return;
            }
        } else {
            sendResponse(new ErrorResponse("Not allowed"));
            return;
        }

        sendResponse(new ErrorResponse("Cant ban user, try again later."));
    }

    private void handleBanRoomUser(Request request) {
        int roomId = request.getParam("roomId");
        int userId = request.getParam("userId");
        RoomThread roomThread = roomManager.getThread(roomId);
        Room room = roomThread.getRoom();
        if (room.getOwner().getId() == currentUser.getId()) {
            boolean status = roomRepository.banUser(room, userId);
            if (status) {
                User bannedUser = userRepository.findOneById(userId);
                roomThread.banUser(bannedUser);
                sendResponse(new Response(Response.Codes.OK));
                return;
            }
        } else {
            sendResponse(new ErrorResponse("Not allowed"));
            return;
        }

        sendResponse(new ErrorResponse("Cant ban user, try again later."));
    }


    /**
     * Send request to client
     *
     * @param request
     */
    void sendRequest(Request request) {
        try {
            synchronized (outputStream) {
                System.out.println("send to: " + currentUser.getName() + " thread:" + Thread.currentThread().getId() + "; " + request);
                outputStream.writeObject(request);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close current socket
     */
    private void closeSession() {
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

        roomManager.getAllConnectedThreads(currentUser).parallelStream().forEach(roomThread -> {
            roomThread.disconnect(currentUser, false);
            roomManager.purgeRoomThread(roomThread.getRoom());
        });
    }

    /**
     * List of session state and allowed requests from clients
     */
    enum States {
        NEW(new Request.Types[]{
                Request.Types.HAND_SHAKE,
                Request.Types.CLOSE
        }),
        GUEST(new Request.Types[]{
                Request.Types.AUTHENTICATION,
                Request.Types.CLOSE
        }),

        AUTHENTICATED(new Request.Types[]{
                Request.Types.ROOM_CREATE,
                Request.Types.ROOM_GET_AVAILABLE_LIST,
                Request.Types.ROOM_USER_JOIN,
                Request.Types.ROOM_USER_LEAVE,
                Request.Types.MESSAGE_NEW,
                Request.Types.CLOSE,
                Request.Types.LOGOUT,
                Request.Types.USER_CHANGE_PASSWORD,
                Request.Types.USER_STATUS,
                Request.Types.ROOM_CHANGE_NAME,
                Request.Types.ROOM_CHANGE_PASSWORD,
                Request.Types.ROOM_REMOVE_PASSWORD,
                Request.Types.ROOM_DELETE,
                Request.Types.ROOM_USER_BAN,
                Request.Types.ROOM_USER_BAN_REMOVE,
        });

        protected Request.Types[] allowedCommands;

        States(Request.Types[] allowedCommands) {
            this.allowedCommands = allowedCommands;
        }

        public Request.Types[] getAllowedCommands() {
            return allowedCommands;
        }
    }
}
