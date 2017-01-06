package pavelmaca.chat.client;

import com.alee.laf.WebLookAndFeel;
import pavelmaca.chat.client.gui.window.Connect;
import pavelmaca.chat.client.gui.window.Main;
import pavelmaca.chat.client.gui.window.Login;
import pavelmaca.chat.server.entity.User;
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
        Connect connectWindow = new Connect();
        connectWindow.setDefaults(
                properties.getProperty("host", ""),
                properties.getProperty("port", ""),
                Boolean.valueOf(properties.getProperty("save", Boolean.toString(true)))
        );
        connectWindow.onSubmit((String serverIp, Integer serverPort, Boolean save) -> {
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

                connectWindow.close();
                openAuthenticationWindow();
            } else {
                connectWindow.showError("Can't connect to server.");
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

    private void openChatWindow(ArrayList<RoomStatus> roomStatusInfo, User identity) {
        if (identity == null) {
            openAuthenticationWindow();
            return;
        }

        Main mainWindow = new Main(session, roomStatusInfo, identity);
        mainWindow.addDisconnectListener(() -> {
            mainWindow.close();
            openConnectionWindow();
        });

        mainWindow.addLogoutListener(() -> {
            mainWindow.close();
            openAuthenticationWindow();
        });

        //  System.out.println("event thread: " + SwingUtilities.isEventDispatchThread());

        // request listener
        new Thread(new GUIRequestListener(mainWindow, session)).start();

        //mainWindow.onJoinRoomClicked((e) -> openJoinRoomWindow(mainWindow));
    }


}
