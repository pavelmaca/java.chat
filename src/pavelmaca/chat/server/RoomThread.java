package pavelmaca.chat.server;

import pavelmaca.chat.client.model.Message;
import pavelmaca.chat.client.model.Room;
import pavelmaca.chat.client.model.User;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class RoomThread implements Runnable {

    private HashMap<User, Session> activeUsers = new HashMap<>();

    private LinkedBlockingDeque<Message> messagesQueue = new LinkedBlockingDeque<>();
    private Room room;

    private boolean running = false;

    public RoomThread(Room room, User firstUser, Session firstUserSession) {
        this.room = room;
        activeUsers.put(firstUser, firstUserSession);
    }

    public void recieveMessage(Message message) {
        try {
            messagesQueue.putLast(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void connect(User user, Session session) {
        synchronized (activeUsers) {
            activeUsers.put(user, session);
        }
    }

    /**
     * Disonect is invoked from session thread of current user
     * @param user
     */
    public void disconnect(User user) {
        synchronized (activeUsers) {
            activeUsers.remove(user);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public Room getRoom(){
        return room;
    }

    @Override
    public void run() {
        running = true;
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
        running = false;
    }
}
