package pavelmaca.chat.client.gui.window;

import javax.swing.*;
import java.awt.*;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Login extends Window {
    public Login() {
        super("Login");
    }

    @Override
    protected void setupComponents() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        JTextField userName = new JTextField();
        JTextField password = new JTextField();
        JLabel userNameLabel = new JLabel("Username");
        JLabel passwordLabel = new JLabel("Password");

        JButton connectBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");
        JCheckBox saveCheckBox = new JCheckBox("Remember me");

        panel.add(userNameLabel);
        panel.add(userName);
        panel.add(passwordLabel);
        panel.add(password);
        panel.add(saveCheckBox);

        //Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(cancelBtn);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(connectBtn);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.getContentPane().add(buttonPane, BorderLayout.PAGE_END);
    }
}
