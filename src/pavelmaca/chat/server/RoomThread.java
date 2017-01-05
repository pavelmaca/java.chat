package pavelmaca.chat.server;

import pavelmaca.chat.share.comunication.Request;
import pavelmaca.chat.server.entity.Message;
import pavelmaca.chat.server.entity.Room;
import pavelmaca.chat.server.entity.User;

import java.util.HashMap;
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
            request.addParameter("user", user.getInfoModel());
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
    public void disconnect(User user) {
        synchronized (activeUsers) {
            System.out.println("user " + user.getName() + " disconnected from room " + room.getId());
            Request request = new Request(Request.Types.ROOM_USER_DISCONNECTED);
            request.addParameter("roomId", room.getId());
            request.addParameter("userId", user.getId());
            request.addParameter("authorId", user.getId());
            try {
                requestQueue.putLast(request);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            activeUsers.remove(user);
            if (activeUsers.isEmpty()) {
                stopThread();
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
                    activeUsers.entrySet().parallelStream().forEach(e -> {
                        if (e.getKey().getId() != (Integer) request.getParam("authorId")) {
                            e.getValue().sendCommand(request);
                        }
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Stopping thread for room " + room.getId());
        running = false;
    }
}
