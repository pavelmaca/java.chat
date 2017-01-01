package pavelmaca.chat.server;

import pavelmaca.chat.commands.Command;
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

    private LinkedBlockingDeque<Command> commandQueue = new LinkedBlockingDeque<>();
    private Room room;

    private boolean running = false;
    final private static Command dummy = new Command(Command.Types.DUMMY);

    public RoomThread(Room room) {
        this.room = room;
    }

    public void recieveMessage(Message message) {
        try {
            System.out.println("room " + room.getId() + " received message " + message.getContent());
            Command command = new Command(Command.Types.MESSAGE_NEW);
            command.addParametr("message", message.getInfoModel());
            command.addParametr("authorId", message.getAuthor().getId());
            commandQueue.putLast(command);
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
            Command command = new Command(Command.Types.ROOM_USER_CONNECTED);
            command.addParametr("roomId", room.getId());
            command.addParametr("user", user.getInfoModel());
            command.addParametr("authorId", user.getId());
            try {
                commandQueue.putLast(command);
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
            Command command = new Command(Command.Types.ROOM_USER_DISCONNECTED);
            command.addParametr("roomId", room.getId());
            command.addParametr("userId", user.getId());
            command.addParametr("authorId", user.getId());
            try {
                commandQueue.putLast(command);
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
     * Stop current thread by pushing dummy message to quee
     */
    private void stopThread() {
        try {
            commandQueue.putLast(dummy);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        running = true;
        System.out.println("Starting thread for room " + room.getId());
        while (!activeUsers.isEmpty()) {
            System.out.println("running room thread" + Thread.currentThread().getId());
            try {
                Command command = commandQueue.takeFirst();
                if (command.equals(dummy)) {
                    continue;
                }
                // send message in parallel thread to all users
                synchronized (activeUsers) {
                    activeUsers.entrySet().parallelStream().forEach(e -> {
                        if (e.getKey().getId() != (Integer) command.getParam("authorId")) {
                            e.getValue().sendCommand(command);
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
