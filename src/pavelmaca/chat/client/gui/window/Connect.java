package pavelmaca.chat.client.gui.window;

import pavelmaca.chat.share.Lambdas;

import javax.swing.*;
import java.awt.*;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Connect extends Window {


    private JTextField serverIP;
    private JTextField serverPort;
    private JLabel serverIPLabel;
    private JLabel serverPortLabel;
    private JButton connectBtn;
    private JCheckBox saveCheckBox;
    private JLabel errorLabel;

    public Connect() {
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

    /**
     * @param callback Called after configuration is submitted
     */
    public void onSubmit(Lambdas.Function3<String, Integer, Boolean> callback) {
        connectBtn.addActionListener(e -> callback.apply(serverIP.getText(), Integer.parseInt(serverPort.getText()), saveCheckBox.isSelected()));
    }

    /**
     * Setup default values
     *
     * @param host
     * @param port
     * @param save
     */
    public void setDefaults(String host, String port, boolean save) {
        serverIP.setText(host);
        serverPort.setText(port);
        saveCheckBox.setSelected(save);
    }

    /**
     * @param text Error message
     */
    public void showError(String text) {
        errorLabel.setText(text);
        errorLabel.setVisible(true);
        frame.pack();
    }
}
