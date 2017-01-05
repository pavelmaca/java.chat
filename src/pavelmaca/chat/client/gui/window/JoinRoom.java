package pavelmaca.chat.client.gui.window;

import pavelmaca.chat.share.Lambdas;
import pavelmaca.chat.share.model.RoomInfo;

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

    private JButton joinBtn;

    private RoomInfo newRoom;

    public JoinRoom(ArrayList<RoomInfo> roomList) {
        super("Join room");

        newRoom = new RoomInfo(-1, "<add new>");
        roomSelectBox.addItem(newRoom);

        roomList.forEach(r -> roomSelectBox.addItem(r));

        roomSelectBox.addActionListener(e -> {
            boolean visible = roomSelectBox.getSelectedItem().equals(newRoom);
            createRoom.setVisible(visible);
            createRoomLabel.setVisible(visible);
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

    public void onJoinSubmit(Lambdas.Function1<Integer> callback) {
        joinBtn.addActionListener(e -> {
            RoomInfo selected = (RoomInfo) roomSelectBox.getSelectedItem();
            if (selected.getId() != newRoom.getId()) {
                callback.apply(selected.getId());
            }
        });
    }

    public void onNewRoomSubmit(Lambdas.Function1<String> callback) {
        joinBtn.addActionListener(e -> {
            RoomInfo selected = (RoomInfo) roomSelectBox.getSelectedItem();
            if (selected.getId() == newRoom.getId()) {
                callback.apply(createRoom.getText());
            }
        });
    }

}
