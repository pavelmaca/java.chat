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
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class RoomThread implements Runnable {

    private HashMap<User, Session> activeUsers = new HashMap<>();

    private LinkedBlockingDeque<Request> requestQueue = new LinkedBlockingDeque<>();
    private Room room;

    private boolean running = false;
    final private static Request dummy = new Request(Request.Types.DUMMY);

    public RoomThread(Room room) {
        this.room = room;
    }

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

    public Set<User> getConnectedUsers() {
        synchronized (activeUsers) {
            return activeUsers.keySet();
        }
    }

    public boolean hasUser(User user) {
        synchronized (activeUsers) {
            return activeUsers.containsKey(user);
        }
    }

    public void connect(User user, Session session) {
        synchronized (activeUsers) {
            System.out.println("user " + user.getName() + " connected to room " + room.getId());
            Request request = new Request(Request.Types.ROOM_USER_CONNECTED);
            request.addParameter("roomId", room.getId());
            UserInfo userInfo = user.getInfoModel();
            userInfo.setRank(room.getOwner().getId() == user.getId() ? UserInfo.Rank.OWNER : UserInfo.Rank.MEMEBER);
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
     * Disonect is invoked from session thread of current user
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

          /*  activeUsers.remove(user);
            if (activeUsers.isEmpty()) {
                stopThread();
            }*/
        }
    }

    public void leave(User user, boolean force) {
        synchronized (activeUsers) {
            System.out.println("user " + user.getName() + " leave from room " + room.getId());
            Request request = new Request(Request.Types.ROOM_USER_LEAVE);
            request.addParameter("roomId", room.getId());
            request.addParameter("userId", user.getId());
            if(!force){
                request.addParameter("authorId", user.getId());
            }

            try {
                requestQueue.putLast(request);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

          /*  activeUsers.remove(user);
            if (activeUsers.isEmpty()) {
                stopThread();
            }*/
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

                        //TODO make it as property ?
                        if (!request.hasParam("authorId") || userSessionEntry.getKey().getId() != (Integer) request.getParam("authorId")) {
                            userSessionEntry.getValue().sendCommand(request);
                        }

                        // remove user from queue after sending room disconnect
                        if ((request.getType() == Request.Types.ROOM_USER_DISCONNECTED ||  request.getType() == Request.Types.ROOM_USER_LEAVE) && userSessionEntry.getKey().getId() == (Integer) request.getParam("userId")) {
                            iterator.remove();
                        }
                    }
                    /*
                    activeUsers.entrySet().parallelStream().forEach(e -> {
                        //TODO make it as property ?
                        if (!request.hasParam("authorId") || e.getKey().getId() != (Integer) request.getParam("authorId")) {
                            e.getValue().sendCommand(request);
                        }
                    });*/
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
        Request request = new Request(Request.Types.ROOM_CHANHE_NAME);
        request.addParameter("roomId", room.getId());
        request.addParameter("name", newName);
        try {
            requestQueue.putLast(request);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void disconnetAll() {
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
