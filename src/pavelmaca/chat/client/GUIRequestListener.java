package pavelmaca.chat.client;

import pavelmaca.chat.client.gui.window.Main;
import pavelmaca.chat.share.comunication.Request;

import javax.swing.*;

/**
 * Created by Assassik on 05.01.2017.
 */
public class GUIRequestListener implements Runnable {

    Main mainWindow;
    Session session;

    boolean running;

    public GUIRequestListener(Main mainWindow, Session session) {
        this.mainWindow = mainWindow;
        this.session = session;
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                Request request = session.getUpdateQueue().takeFirst();
                if (request.type == Request.Types.DUMMY) {
                    running = false;
                    break;
                }
                processRequest(request);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void processRequest(Request request) {
        SwingUtilities.invokeLater(() -> {
            // updating GUI, so need to run inside EDT
            // System.out.println("event thread: " + SwingUtilities.isEventDispatchThread());

            switch (request.getType()) {
                case ROOM_USER_CONNECTED:
                    //TODO when adin re-join room, no icon is displayed
                    mainWindow.userConnected(
                            request.getParam("roomId"),
                            request.getParam("user")
                    );
                    break;
                case ROOM_USER_DISCONNECTED:
                    mainWindow.userDisconnected(
                            request.getParam("roomId"),
                            request.getParam("userId")
                    );
                    break;

                case ROOM_USER_LEAVE:
                    mainWindow.userLeft(
                            request.getParam("roomId"),
                            request.getParam("userId")
                    );
                    break;
                case MESSAGE_NEW:
                    mainWindow.messageReceived(request.getParam("message"));
                    break;
                case CLOSE:
                    running = false;
                    //mainWindow.disconect();
                    break;
                default:
                    System.out.println("Invalid request " + request.getType());
            }
        });
    }
}
