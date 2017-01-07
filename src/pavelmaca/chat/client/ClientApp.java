package pavelmaca.chat.client;

import com.alee.laf.WebLookAndFeel;
import pavelmaca.chat.client.gui.window.Connect;
import pavelmaca.chat.client.gui.window.Main;
import pavelmaca.chat.client.gui.window.Login;
import pavelmaca.chat.share.model.RoomStatus;
import pavelmaca.chat.share.model.UserInfo;

import javax.swing.*;
import java.util.HashMap;
import java.util.Properties;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class ClientApp implements Runnable {

    private Session session;
    private Properties properties;

    public static void main(String[] args) {
        try {
            // Setup custom Swing look and feel
            UIManager.setLookAndFeel(new WebLookAndFeel());

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

    /**
     * Open connection setting window
     */
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

    /**
     * Open authorization window
     */
    private void openAuthenticationWindow() {
        Login loginWindow = new Login();
        loginWindow.onSubmit((username, password) -> {
            UserInfo identity = session.authenticate(username, password);
            if (identity == null) {
                loginWindow.showError("Invalid credencials.");
                return;
            }

            System.out.println("authenticated");

            loginWindow.close();

            HashMap<Integer, RoomStatus> roomStatusInfo = session.getStatus();

            openMainWindow(roomStatusInfo, identity);
        });
        loginWindow.onCancel(() -> {
            loginWindow.close();
            session.close();
            openConnectionWindow();
        });

    }

    /**
     * Open main ap window
     *
     * @param roomStatusInfo history for all connected rooms
     * @param identity       Current user info
     */
    private void openMainWindow(HashMap<Integer, RoomStatus> roomStatusInfo, UserInfo identity) {
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

        // request listener
        new Thread(new GUIRequestListener(mainWindow, session)).start();
    }
}
