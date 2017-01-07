package pavelmaca.chat.server;

import pavelmaca.chat.server.entity.Room;
import pavelmaca.chat.server.entity.User;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handle running room threads
 *
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class RoomManager {

    private HashMap<Integer, RoomThread> threads;

    public RoomManager() {
        threads = new HashMap<>();
    }

    /**
     * Open new room thread for first user, or just add user to running one.
     * If room thread instance exist, but it's stopped, restart it
     *
     * @param room
     * @param user
     * @param session
     * @return room thread, where client socket is connected
     */
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

    /**
     * Shutdown all empty running rooms
     *
     * @param room
     */
    public synchronized void purgeRoomThread(Room room) {
        if (threads.containsKey(room.getId())) {
            RoomThread roomThread = threads.get(room.getId());
            if (!roomThread.isRunning()) {
                threads.remove(room.getId());
            }
        }
    }

    /**
     * Check if user is connected to room
     *
     * @param user
     * @param roomId
     * @return true, if user have active socket to room
     */
    public synchronized boolean isConnected(User user, int roomId) {
        RoomThread roomThread = getThread(roomId);
        return roomThread != null && roomThread.hasUser(user);
    }

    /**
     * @param roomId
     * @return running room thread instance
     */
    public synchronized RoomThread getThread(int roomId) {
        if (threads.containsKey(roomId)) {
            return threads.get(roomId);
        }
        return null;
    }

    /**
     * @param user
     * @return all room thread, where user is connected
     */
    public synchronized List<RoomThread> getAllConnectedThreads(User user) {
        return threads.values().stream()
                .filter(roomThread -> roomThread.hasUser(user))
                .collect(Collectors.toList());
    }

}
