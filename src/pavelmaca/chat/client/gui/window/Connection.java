package pavelmaca.chat.client.gui.window;

import javax.swing.*;
import java.awt.*;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Connection extends Window {

    public Connection() {
        super("Connection setting");
    }

    @Override
    protected void setupComponents() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        JTextField serverIP = new JTextField();
        JTextField serverPort = new JTextField();
        JLabel serverIPLabel = new JLabel("Server");
        JLabel serverPortLabel = new JLabel("Port");

        JButton connectBtn = new JButton("Connect");
        JCheckBox saveCheckBox = new JCheckBox("Remember server");

        panel.add(serverIPLabel);
        panel.add(serverIP);
        panel.add(serverPortLabel);
        panel.add(serverPort);
        panel.add(saveCheckBox);

        //Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(connectBtn);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.getContentPane().add(buttonPane, BorderLayout.PAGE_END);
    }
}
