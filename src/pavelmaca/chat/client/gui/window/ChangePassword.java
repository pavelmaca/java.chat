package pavelmaca.chat.client.gui.window;

import pavelmaca.chat.share.Lambdas;

import javax.swing.*;
import java.awt.*;

/**
 * Change password window
 *
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class ChangePassword extends Window {

    private JLabel newPasswordLabel;
    private JTextField newPassword;
    private JButton saveBtn;
    private JButton cancelBtn;

    private JLabel errorLabel;

    public ChangePassword() {
        super("Change password");
    }

    @Override
    protected void setupComponents() {
        //Setup GUI
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));


        newPasswordLabel = new JLabel("New password");
        panel.add(newPasswordLabel);

        newPassword = new JTextField();
        panel.add(newPassword);


        //Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());

        cancelBtn = new JButton("Cancel");
        buttonPane.add(cancelBtn);

        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));

        saveBtn = new JButton("Save");
        buttonPane.add(saveBtn);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        errorLabel = new JLabel("");
        errorLabel.setVisible(false);
        errorLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.add(errorLabel);

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.getContentPane().add(buttonPane, BorderLayout.PAGE_END);
    }

    /**
     * @param callback Called after valid new password is submitted
     */
    public void onSubmit(Lambdas.Function1<String> callback) {
        saveBtn.addActionListener(e -> {
            if (!newPassword.getText().isEmpty()) {
                callback.apply(newPassword.getText());
            } else {
                showError("password can't be empty.");
            }
        });
    }

    /**
     * @param callback Called after cancel button clicked
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
