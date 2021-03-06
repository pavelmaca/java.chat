package pavelmaca.chat.client.gui.window;

import pavelmaca.chat.share.Lambdas;

import javax.swing.*;
import java.awt.*;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Login extends Window {
    private JTextField userName;
    private JTextField password;
    private JLabel userNameLabel;
    private JLabel passwordLabel;
    private JButton connectBtn;
    private JButton cancelBtn;

    private JLabel errorLabel;

    public Login() {
        super("Login");
    }

    @Override
    protected void setupComponents() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        userNameLabel = new JLabel("Username");
        panel.add(userNameLabel);

        userName = new JTextField();
        panel.add(userName);

        passwordLabel = new JLabel("Password");
        panel.add(passwordLabel);

        password = new JTextField();
        panel.add(password);

        errorLabel = new JLabel("");
        errorLabel.setVisible(false);
        errorLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.add(errorLabel);

        //Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());

        cancelBtn = new JButton("Cancel");
        buttonPane.add(cancelBtn);

        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));

        connectBtn = new JButton("OK");
        buttonPane.add(connectBtn);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.getContentPane().add(buttonPane, BorderLayout.PAGE_END);
    }

    /**
     * @param callback Called after valid user information are filled
     */
    public void onSubmit(Lambdas.Function2<String, String> callback) {
        connectBtn.addActionListener(e -> {
            String userNameText = userName.getText();
            if (!userNameText.isEmpty() && !password.getText().isEmpty()) {
                callback.apply(userName.getText(), password.getText());
            } else {
                showError("Fill username and password.");
            }
        });
    }

    /**
     * @param callback Called after close button clicked
     */
    public void onCancel(Lambdas.Function0 callback) {
        cancelBtn.addActionListener(e -> {
            callback.apply();
        });
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
