package pavelmaca.chat.server;

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

    private LinkedBlockingDeque<Message> messagesQueue = new LinkedBlockingDeque<>();
    private Room room;

    private boolean running = false;

    public RoomThread(Room room) {
        this.room = room;
    }

    public void recieveMessage(Message message) {
        try {
            System.out.println("room " + room.getId() + " received message " + message.getContent());
            messagesQueue.putLast(message);
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
            activeUsers.remove(user);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public Room getRoom() {
        return room;
    }

    @Override
    public void run() {
        running = true;
        System.out.println("Starting thread for room " + room.getId());
        while (activeUsers.size() > 0) {
            System.out.println("running room thread" + Thread.currentThread().getId());
            try {
                // TODO: this is blocking method, need to use dummy to close this thread
                Message message = messagesQueue.takeFirst();
                // send message in parallel thread to all users
                synchronized (activeUsers) {
                    activeUsers.entrySet().parallelStream().forEach(e -> {
                        if (e.getKey() != message.getAuthor()) {
                            e.getValue().sendMessage(message);
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
