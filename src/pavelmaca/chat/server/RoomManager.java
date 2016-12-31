package pavelmaca.chat.server;

import pavelmaca.chat.server.entity.Room;
import pavelmaca.chat.server.entity.User;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handling running room threads
 *
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class RoomManager {

    private HashMap<Integer, RoomThread> threads;

    public RoomManager() {
        threads = new HashMap<>();
    }

    public synchronized RoomThread joinRoomThread(Room room, User user, Session session) {
        RoomThread roomThread;
        if (!threads.containsKey(room.getId())) {
            roomThread = new RoomThread(room);
            threads.put(room.getId(), roomThread);
        } else {
            roomThread = threads.get(room.getId());
        }

        roomThread.connect(user, session);

        if (!roomThread.isRunning()) {
            new Thread(roomThread).start();
        }
        return roomThread;
    }

    public synchronized void purgeRoomThread(Room room) {
        if (threads.containsKey(room.getId())) {
            RoomThread roomThread = threads.get(room.getId());
            if (!roomThread.isRunning()) {
                threads.remove(room.getId());
            }
        }
    }

    public synchronized boolean isConnected(User user, int roomId) {
        RoomThread roomThread = getThread(roomId);
        return roomThread != null && roomThread.hasUser(user);
    }

    public synchronized RoomThread getThread(int roomId) {
        if (threads.containsKey(roomId)) {
            return threads.get(roomId);
        }
        return null;
    }

    public synchronized List<RoomThread> getAllConnectedThreads(User user) {
        return threads.values().stream()
                .filter(roomThread -> roomThread.hasUser(user))
                .collect(Collectors.toList());
    }
}
