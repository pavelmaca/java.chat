package pavelmaca.chat.client;

import pavelmaca.chat.client.gui.window.Main;
import pavelmaca.chat.share.comunication.Request;

import javax.swing.*;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class GUIRequestListener implements Runnable {

    private Main mainWindow;
    private Session session;

    private boolean running;

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

                // DUMMY request is used to stop thread after unexpected closed session
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

    /**
     * Process request from server by its Type
     *
     * @param request server request
     */
    private void processRequest(Request request) {
        SwingUtilities.invokeLater(() -> {
            // updating GUI, so need to run inside EDT
            // System.out.println("event thread: " + SwingUtilities.isEventDispatchThread());

            switch (request.getType()) {
                case ROOM_USER_CONNECTED:
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
                case ROOM_CHANHE_NAME:
                    mainWindow.roomChangeName(request.getParam("roomId"),
                            request.getParam("name"));
                    break;

                case ROOM_USER_BAN:
                    mainWindow.roomBanUser(request.getParam("roomId"),
                            request.getParam("user"));
                    break;
                case ROOM_USER_BAN_REMOVE:
                    mainWindow.roomRemoveBanUser(request.getParam("roomId"),
                            request.getParam("userId"));
                    break;
                default:
                    System.out.println("Invalid request " + request.getType());
            }
        });
    }
}
