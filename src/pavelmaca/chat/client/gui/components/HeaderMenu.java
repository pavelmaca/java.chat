package pavelmaca.chat.client.gui.components;

import pavelmaca.chat.share.Factory;

import javax.swing.*;
import java.awt.event.ActionListener;

/**
 * Main header menu
 *
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class HeaderMenu implements IComponent<JMenuBar> {

    private JMenuItem disconnectItem = new JMenuItem("Disconnect");
    private JMenuItem logoutItem = new JMenuItem("Logout");
    private JMenuItem changePasswordItem = new JMenuItem("Change password");

    private JMenuBar menuBar;

    public HeaderMenu() {
        menuBar = new JMenuBar();

        JMenu server = new JMenu("Server");
        server.add(disconnectItem);
        menuBar.add(server);

        JMenu user = new JMenu("User");
        user.add(changePasswordItem);
        user.add(logoutItem);
        menuBar.add(user);
    }

    @Override
    public JMenuBar getComponent() {
        return menuBar;
    }

    /**
     * @param listener Disconnect button listener
     */
    public void addDisconnectActionListener(ActionListener listener) {
        disconnectItem.addActionListener(listener);
    }

    /**
     * @param listener Logout button listener
     */
    public void addLogoutActionListener(ActionListener listener) {
        logoutItem.addActionListener(listener);
    }

    /**
     * @param listener Change user password listener
     */
    public void addChangePasswordActionListener(ActionListener listener) {
        changePasswordItem.addActionListener(listener);
    }
}
