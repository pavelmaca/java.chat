package pavelmaca.chat.client.gui.components;

import pavelmaca.chat.share.Lambdas;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class SimpleInputDialog {

    private String defaultValue = "";
    String title;
    String message = null;
    Component parent;

    private Lambdas.Function1<String> onSuccess = null;
    private Lambdas.Function0 onCancel = null;
    private Function<String, Boolean> validator;
    private String errorMessage = "";
    private String validatorErrorMessage;

    public SimpleInputDialog(Component parent, String title, String message) {
        this.title = title;
        this.message = message;
        this.parent = parent;
        this.defaultValue = defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }


    public void open() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        JLabel messageLabel = new JLabel(message);
        panel.add(messageLabel);

        JTextField inputField = new JTextField();
        panel.add(inputField);

        if(!errorMessage.isEmpty()){
            JLabel errorLabel = new JLabel(errorMessage);
            panel.add(errorLabel);
        }

        String[] options = new String[]{"OK", "Cancel"};
        int option = JOptionPane.showOptionDialog(null, panel, title,
                JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[1]);

        if (option == 0) {
            if (validator != null && !validator.apply(inputField.getText())) {
                errorMessage = validatorErrorMessage;
                open();
                return;
            }
            if (onSuccess != null) {
                onSuccess.apply(inputField.getText());
            }
        } else {
            if (onCancel != null) {
                onCancel.apply();
            }
        }
    }

    public void onSuccess(Lambdas.Function1<String> listener) {
        this.onSuccess = listener;
    }

    public void onCancel(Lambdas.Function0 listener) {
        this.onCancel = listener;
    }

    public void setValidator(Function<String, Boolean> validator, String validatorErrorMessage) {
        this.validatorErrorMessage = validatorErrorMessage;
        this.validator = validator;
    }


    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
