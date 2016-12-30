package pavelmaca.chat.server;

import java.util.ArrayList;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class RoomThread implements Runnable {

    private ArrayList<Session> activeUsers;

    @Override
    public void run() {
        while (activeUsers.size() > 0){

        }
    }
}
