package pavelmaca.chat.client;

import pavelmaca.chat.client.gui.window.Chat;
import pavelmaca.chat.client.gui.window.Connection;
import pavelmaca.chat.client.gui.window.Login;
import pavelmaca.chat.client.model.User;

import javax.swing.*;
import java.util.Properties;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Client implements Runnable {

    Session session;
    Properties properties;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.invokeLater(new Client());
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
            return null;
        });
    }

    private void openAuthenticationWindow() {
        Login loginWindow = new Login();
        loginWindow.onSubmit((username, password) -> {
            if (session.authenticate(username, password)) {
                System.out.println("authenticated");

                loginWindow.close();

                openChatWindow(session.getIdentity());
            } else {
                loginWindow.showError("Invalid credencials.");
            }
            return null;
        });
        loginWindow.onCancel((e) -> {
            loginWindow.close();
            session.close();
            openConnectionWindow();
            return null;
        });
    }

    private void openChatWindow(User identity) {
        if (identity == null) {
            openAuthenticationWindow();
            return;
        }

        System.out.println("recived identity: " + identity.getName());

        Chat chatWindow = new Chat(identity);
        chatWindow.onWindowClose((e) -> {
            session.close();
            return null;
        });
       /* chatWindow.onNewMessage((text, room) -> {
            if (session.sendMessage(text, room)) {
                // TODO
            } else {
                System.out.println("Error during sending your message");
            }
        });*/
    }
}
