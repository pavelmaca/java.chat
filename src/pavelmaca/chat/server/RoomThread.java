package pavelmaca.chat.server;

import pavelmaca.chat.share.comunication.Request;
import pavelmaca.chat.server.entity.Message;
import pavelmaca.chat.server.entity.Room;
import pavelmaca.chat.server.entity.User;
import pavelmaca.chat.share.model.UserInfo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Handle all room outgoing connection to all connected users
 *
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class RoomThread implements Runnable {

    /**
     * List of connected user via socket
     */
    private final HashMap<User, Session> activeUsers = new HashMap<>();

    /**
     * All requests are stored in blocking queue
     */
    private LinkedBlockingDeque<Request> requestQueue = new LinkedBlockingDeque<>();

    /**
     * Current room info
     */
    private Room room;

    private boolean running = false;

    /**
     * Dummy request to stop running thread
     */
    final private static Request dummy = new Request(Request.Types.DUMMY);

    public RoomThread(Room room) {
        this.room = room;
    }

    /**
     * Send new message to all clients
     *
     * @param message
     */
    public void receiveMessage(Message message) {
        try {
            System.out.println("room " + room.getId() + " received message " + message.getContent());
            Request request = new Request(Request.Types.MESSAGE_NEW);
            request.addParameter("message", message.getInfoModel());
            request.addParameter("authorId", message.getAuthor().getId());
            requestQueue.putLast(request);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return all connected user via socket to this room
     */
    public Set<User> getConnectedUsers() {
        synchronized (activeUsers) {
            return activeUsers.keySet();
        }
    }

    /**
     * @param user
     * @return true, if  user is connected via socket to this room
     */
    public boolean hasUser(User user) {
        synchronized (activeUsers) {
            return activeUsers.containsKey(user);
        }
    }

    /**
     * Connect user socket to current room
     *
     * @param user
     * @param session
     */
    public void connect(User user, Session session) {
        synchronized (activeUsers) {
            System.out.println("user " + user.getName() + " connected to room " + room.getId());
            Request request = new Request(Request.Types.ROOM_USER_CONNECTED);
            request.addParameter("roomId", room.getId());
            UserInfo userInfo = user.getInfoModel();
            userInfo.setRank(room.getOwner().getId() == user.getId() ? UserInfo.Rank.OWNER : UserInfo.Rank.MEMBER);
            request.addParameter("user", userInfo);
            request.addParameter("authorId", user.getId());
            try {
                requestQueue.putLast(request);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            activeUsers.put(user, session);
        }
    }

    /**
     * Disconnect is invoked from session thread of current user
     *
     * @param user
     */
    public void disconnect(User user, boolean sendToRequestAuthor) {
        synchronized (activeUsers) {
            System.out.println("user " + user.getName() + " disconnected from room " + room.getId());
            Request request = new Request(Request.Types.ROOM_USER_DISCONNECTED);
            request.addParameter("roomId", room.getId());
            request.addParameter("userId", user.getId());
            if (!sendToRequestAuthor) {
                request.addParameter("authorId", user.getId());
            }
            try {
                requestQueue.putLast(request);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Send leave info to all clients
     *
     * @param user
     * @param force
     */
    public void leave(User user, boolean force) {
        synchronized (activeUsers) {
            System.out.println("user " + user.getName() + " leave from room " + room.getId());
            Request request = new Request(Request.Types.ROOM_USER_LEAVE);
            request.addParameter("roomId", room.getId());
            request.addParameter("userId", user.getId());
            if (!force) {
                request.addParameter("authorId", user.getId());
            }

            try {
                requestQueue.putLast(request);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public boolean isRunning() {
        return running;
    }

    public Room getRoom() {
        return room;
    }

    /**
     * Stop current thread by pushing dummy message to queue
     */
    private void stopThread() {
        try {
            requestQueue.putLast(dummy);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        running = true;
        System.out.println("Starting thread for room " + room.getId());
        while (!activeUsers.isEmpty()) {
            try {
                Request request = requestQueue.takeFirst();
                if (request.equals(dummy)) {
                    continue;
                }
                // send message in parallel thread to all users
                synchronized (activeUsers) {
                    Iterator<Map.Entry<User, Session>> iterator = activeUsers.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<User, Session> userSessionEntry = iterator.next();

                        if (!request.hasParam("authorId") || userSessionEntry.getKey().getId() != (Integer) request.getParam("authorId")) {
                            userSessionEntry.getValue().sendRequest(request);
                        }

                        // remove user from queue after sending room disconnect or leave
                        if ((request.getType() == Request.Types.ROOM_USER_DISCONNECTED || request.getType() == Request.Types.ROOM_USER_LEAVE) && userSessionEntry.getKey().getId() == (Integer) request.getParam("userId")) {
                            iterator.remove();
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Stopping thread for room " + room.getId());
        running = false;
    }

    public void changeRoomName(String newName) {
        getRoom().setName(newName);
        Request request = new Request(Request.Types.ROOM_CHANGE_NAME);
        request.addParameter("roomId", room.getId());
        request.addParameter("name", newName);
        try {
            requestQueue.putLast(request);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void disconnectAll() {
        synchronized (activeUsers) {
            Object[] users = activeUsers.keySet().toArray();
            for (Object user : users) {
                disconnect((User) user, true);
            }
        }
    }

    public void banUser(User banUser) {
        // disconnect user, if is connected
        if (hasUser(banUser)) {
            leave(banUser, true);
        }

        // notify others about ban
        Request request = new Request(Request.Types.ROOM_USER_BAN);
        request.addParameter("roomId", room.getId());
        UserInfo banUserInfo = banUser.getInfoModel();
        banUserInfo.setStatus(UserInfo.Status.BANNED);
        request.addParameter("user", banUserInfo);
        try {
            requestQueue.putLast(request);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void removeUserBan(int userId) {
        // notify others about ban removal
        Request request = new Request(Request.Types.ROOM_USER_BAN_REMOVE);
        request.addParameter("roomId", room.getId());
        request.addParameter("userId", userId);
        try {
            requestQueue.putLast(request);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
