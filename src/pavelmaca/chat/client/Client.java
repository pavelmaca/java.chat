package pavelmaca.chat.client;

import javax.swing.*;
import pavelmaca.chat.client.gui.window.*;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Client {

    public static void main(String[] args) {
        try {
            SwingUtilities.invokeLater(() -> {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
              //  new Connection();
              //  new Login();
                new Chat("Test");
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
