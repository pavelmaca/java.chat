package pavelmaca.chat.server;

import pavelmaca.chat.client.model.Room;
import pavelmaca.chat.client.model.User;

import java.util.ArrayList;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class RoomThread implements Runnable {

    private ArrayList<User> activeUsers;

    private Room room;

    public RoomThread(Room room, User firstUser) {
        this.room = room;
        activeUsers = new ArrayList<>();
        activeUsers.add(firstUser);
    }

    public void recieveMessage(String text, User user) {
        // TODO recieve message
    }

    public void connect(User user) {
        activeUsers.add(user);
    }

    public void disconnect(User user) {
        activeUsers.remove(user);
    }

    @Override
    public void run() {
        while (activeUsers.size() > 0) {
            System.out.println("running room thread" + Thread.currentThread().getId());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
