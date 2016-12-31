package pavelmaca.chat.server;

import pavelmaca.chat.client.model.Room;
import pavelmaca.chat.client.model.User;

import java.util.HashMap;

/**
 * Handling running room threads
 *
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class RoomManager {

    private HashMap<Room, RoomThread> threads;

    public RoomManager() {
        threads = new HashMap<>();
    }

    public synchronized RoomThread joinRoomThread(Room room, User user, Session session) {
        RoomThread roomThread;
        if (!threads.containsKey(room)) {
            roomThread = new RoomThread(room, user, session);
            threads.put(room, roomThread);
        } else {
            roomThread = threads.get(room);
            roomThread.connect(user, session);
        }

        if(!roomThread.isRunning()){
            new Thread(roomThread).start();
        }
        return roomThread;
    }

    public synchronized void purgeRoomThread(Room room) {
        if (threads.containsKey(room)) {
            RoomThread roomThread = threads.get(room);
            if(!roomThread.isRunning()){
                threads.remove(room);
            }
        }
    }
}
