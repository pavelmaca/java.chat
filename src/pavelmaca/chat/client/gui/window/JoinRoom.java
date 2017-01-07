package pavelmaca.chat.client.gui.window;

import pavelmaca.chat.share.Lambdas;
import pavelmaca.chat.share.model.RoomInfo;
import pavelmaca.chat.share.model.RoomStatus;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class JoinRoom extends Window {
    private JLabel errorLabel;

    private JComboBox<RoomInfo> roomSelectBox;
    private JLabel createRoomLabel;
    private JTextField createRoom;

    private JLabel roomPasswordLabel;
    private JTextField roomPassword;

    private JButton joinBtn;

    private RoomInfo newRoom;

    public JoinRoom(ArrayList<RoomInfo> roomList) {
        super("Join room");

        newRoom = new RoomInfo(-1, "<add new>", true);
        roomSelectBox.addItem(newRoom);

        roomList.forEach(r -> roomSelectBox.addItem(r));

        roomSelectBox.addActionListener(e -> {
            RoomInfo selectedRoom = (RoomInfo) roomSelectBox.getSelectedItem();
            boolean isNewRoom = selectedRoom.equals(newRoom);
            createRoom.setVisible(isNewRoom);
            createRoomLabel.setVisible(isNewRoom);

            if (isNewRoom) {
                roomPasswordLabel.setText("Password (optional)");
                roomPasswordLabel.setVisible(true);
                roomPassword.setVisible(true);
            } else if (selectedRoom.hasPassword()) {
                roomPasswordLabel.setText("Password (required)");
                roomPasswordLabel.setVisible(true);
                roomPassword.setVisible(true);
            } else {
                roomPasswordLabel.setVisible(false);
                roomPassword.setVisible(false);
            }

            errorLabel.setVisible(false);

            frame.pack();
        });
    }

    @Override
    protected void setupComponents() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        JLabel roomSelectLabel = new JLabel("Select existing room");
        panel.add(roomSelectLabel);

        roomSelectBox = new JComboBox<>();
        panel.add(roomSelectBox);

        createRoomLabel = new JLabel("Create new room");
        panel.add(createRoomLabel);

        createRoom = new JTextField();
        panel.add(createRoom);

        roomPasswordLabel = new JLabel("Password (optional)");
        panel.add(roomPasswordLabel);

        roomPassword = new JTextField();
        panel.add(roomPassword);

        errorLabel = new JLabel("");
        errorLabel.setVisible(false);
        errorLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.add(errorLabel);

        //Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());

        joinBtn = new JButton("Join");
        buttonPane.add(joinBtn);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.getContentPane().add(buttonPane, BorderLayout.PAGE_END);
        frame.pack();
    }

    public void showError(String text) {
        errorLabel.setText(text);
        errorLabel.setVisible(true);
        frame.pack();
    }

    public void onJoinSubmit(Lambdas.Function2<Integer, String> callback) {
        joinBtn.addActionListener(e -> {
            RoomInfo selected = (RoomInfo) roomSelectBox.getSelectedItem();
            if (selected.getId() != newRoom.getId()) {
                if (selected.hasPassword() && roomPassword.getText().isEmpty()) {
                    // need password to continue
                    return;
                }
                callback.apply(selected.getId(), roomPassword.getText());
            }
        });
    }

    public void onNewRoomSubmit(Lambdas.Function2<String, String> callback) {
        joinBtn.addActionListener(e -> {
            RoomInfo selected = (RoomInfo) roomSelectBox.getSelectedItem();
            if (selected.getId() == newRoom.getId()) {
                callback.apply(createRoom.getText(), roomPassword.getText());
            }
        });
    }

}
