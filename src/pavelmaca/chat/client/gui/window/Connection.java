package pavelmaca.chat.client.gui.window;

import pavelmaca.chat.client.Lambdas;

import javax.swing.*;
import java.awt.*;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Connection extends Window {


    private JTextField serverIP;
    private JTextField serverPort;
    private JLabel serverIPLabel;
    private JLabel serverPortLabel;
    private JButton connectBtn;
    private JCheckBox saveCheckBox;
    private JLabel errorLabel;

    public Connection() {
        super("Connection setting");
    }

    @Override
    protected void setupComponents() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        serverIP = new JTextField();
        serverPort = new JTextField();
        serverIPLabel = new JLabel("Server");
        serverPortLabel = new JLabel("Port");

        errorLabel = new JLabel("");

        errorLabel.setVisible(false);
        errorLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        connectBtn = new JButton("Connect");
        saveCheckBox = new JCheckBox("Remember server");

        panel.add(serverIPLabel);
        panel.add(serverIP);
        panel.add(serverPortLabel);
        panel.add(serverPort);
        panel.add(saveCheckBox);
        panel.add(errorLabel);

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

    public void onSubmit(Lambdas.Function3<String, Integer, Boolean> callback) {
        connectBtn.addActionListener(e -> {
            callback.apply(serverIP.getText(), Integer.parseInt(serverPort.getText()), saveCheckBox.isSelected());
        });
    }

    public void setDefaults(String host, String port, boolean save) {
        serverIP.setText(host);
        serverPort.setText(port);
        saveCheckBox.setSelected(save);
    }

    public void showError(String text) {
        errorLabel.setText(text);
        errorLabel.setVisible(true);
        frame.pack();
    }

   /* protected void setupConnection(String serverIp, int serverPort, boolean saveConfig) {
        boolean connected = client.connect(serverIp, serverPort);
        if (connected) {
            if (saveConfig) {
                saveConfig(serverIp, serverPort);
            } else {
                try {
                    Files.delete(Paths.get(configFile));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        } else {
            errorLabel.setText("Can't connect to server.");
            errorLabel.setVisible(true);
            frame.pack();
        }
    }*/

}
