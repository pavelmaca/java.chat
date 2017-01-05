package pavelmaca.chat.client;

import com.alee.laf.WebLookAndFeel;
import pavelmaca.chat.client.gui.window.Chat;
import pavelmaca.chat.client.gui.window.Connection;
import pavelmaca.chat.client.gui.window.JoinRoom;
import pavelmaca.chat.client.gui.window.Login;
import pavelmaca.chat.share.comunication.Request;
import pavelmaca.chat.server.entity.User;
import pavelmaca.chat.share.model.RoomInfo;
import pavelmaca.chat.share.model.RoomStatus;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Properties;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class ClientApp implements Runnable {

    Session session;
    Properties properties;

    public static void main(String[] args) {
        try {
            // WebLookAndFeel.install ();
            UIManager.setLookAndFeel(new WebLookAndFeel());
            //  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.invokeLater(new ClientApp());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            session = new Session();
            properties = Configuration.loadConfig();
            openConnectionWindow();
        } catch (Throwable e) {
            e.printStackTrace();
            session.close();
        }
    }

    private void openConnectionWindow() {
        Connection connectionWindow = new Connection();
        connectionWindow.setDefaults(
                properties.getProperty("host", ""),
                properties.getProperty("port", ""),
                Boolean.valueOf(properties.getProperty("save", Boolean.toString(true)))
        );
        connectionWindow.onSubmit((String serverIp, Integer serverPort, Boolean save) -> {
            if (session.connect(serverIp, serverPort)) {
                System.out.println("connection success");

                if (save) {
                    properties.setProperty("host", serverIp);
                    properties.setProperty("port", String.valueOf(serverPort));
                } else {
                    properties.setProperty("host", "");
                    properties.setProperty("port", "");
                }
                properties.setProperty("save", Boolean.toString(save));
                Configuration.saveConfig(properties);

                connectionWindow.close();
                openAuthenticationWindow();
            } else {
                connectionWindow.showError("Can't connect to server.");
            }
        });
    }

    private void openAuthenticationWindow() {
        Login loginWindow = new Login();
        loginWindow.onSubmit((username, password) -> {
            ArrayList<RoomStatus> roomStatuInfos = session.authenticate(username, password);
            if (roomStatuInfos == null) {
                loginWindow.showError("Invalid credencials.");
                return;
            }

            System.out.println("authenticated");

            loginWindow.close();

            openChatWindow(roomStatuInfos, new User(0, username, password));
        });
        loginWindow.onCancel(() -> {
            loginWindow.close();
            session.close();
            openConnectionWindow();
        });

    }

    private void openChatWindow(ArrayList<RoomStatus> roomStatuInfos, User identity) {
        if (identity == null) {
            openAuthenticationWindow();
            return;
        }

        Chat chatWindow = new Chat(roomStatuInfos, identity);
        chatWindow.addWindowsCloseListener((isLogout) -> {
            System.out.println("Closing chat window");
            if (isLogout) {
                session.logout();
            } else {
                session.close();
            }
        });

        chatWindow.onMessageSubmit((text, roomId) -> {
            session.sendMessage(text, roomId);
        });

        chatWindow.setDisconnectListener(() -> {
            chatWindow.close();
            openConnectionWindow();
        });

        chatWindow.setLogoutListener(() -> {
            chatWindow.close();
            openAuthenticationWindow();
        });

        //  System.out.println("event thread: " + SwingUtilities.isEventDispatchThread());

        //update listener
        new Thread(new GUIRequestListener(chatWindow, session)).start();

        chatWindow.onRoomCreated(() -> openJoinRoomWindow(chatWindow));

    }

    private void openJoinRoomWindow(Chat chatWindow) {
        ArrayList<RoomInfo> roomList = session.getAvailableRoomList();
        System.out.println("event thred: " +
                SwingUtilities.isEventDispatchThread());

        JoinRoom joinRoomWindow = new JoinRoom(roomList);
        joinRoomWindow.onJoinSubmit(roomId -> {
            RoomStatus room = session.joinRoom(roomId);
            if (room != null) {
                chatWindow.addRoom(room, true);
                joinRoomWindow.close();
            }
            // TODO room authentication
        });

        joinRoomWindow.onNewRoomSubmit(roomName -> {
            RoomStatus room = session.createRoom(roomName);
            if (room != null) {
                chatWindow.addRoom(room, true);
                joinRoomWindow.close();
            }
        });

    }
}
