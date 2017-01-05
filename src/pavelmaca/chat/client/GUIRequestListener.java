package pavelmaca.chat.client;

import pavelmaca.chat.client.gui.window.Chat;
import pavelmaca.chat.share.comunication.Request;

import javax.swing.*;

/**
 * Created by Assassik on 05.01.2017.
 */
public class GUIRequestListener implements Runnable {

    Chat chatWindow;
    Session session;

    boolean running;

    public GUIRequestListener(Chat chatWindow, Session session) {
        this.chatWindow = chatWindow;
        this.session = session;
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                Request request = session.getUpdateQueue().takeFirst();
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
                    chatWindow.userConnected(
                            request.getParam("roomId"),
                            request.getParam("user")
                    );
                    break;
                case ROOM_USER_DISCONNECTED:
                    chatWindow.userDisconnected(
                            request.getParam("roomId"),
                            request.getParam("userId")
                    );
                    break;
                case MESSAGE_NEW:
                    chatWindow.messageRecieved(request.getParam("message"));
                    break;
                case CLOSE:
                    running = false;
                default:
                    System.out.println("Invalid request " + request.getType());
            }
        });
    }
}
