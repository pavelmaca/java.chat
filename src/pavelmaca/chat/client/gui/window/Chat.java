package pavelmaca.chat.client.gui.window;

import pavelmaca.chat.client.Lambdas;
import pavelmaca.chat.client.Lambdas.Function2;
import pavelmaca.chat.client.renderer.MessageListRenderer;
import pavelmaca.chat.client.renderer.RoomListRenderer;
import pavelmaca.chat.client.renderer.UserListRenderer;
import pavelmaca.chat.server.entity.User;
import pavelmaca.chat.share.model.MessageInfo;
import pavelmaca.chat.share.model.RoomStatus;
import pavelmaca.chat.share.model.UserInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Chat extends Window {

    private User currentUser;

    private ArrayList<RoomStatus> roomStatuses = new ArrayList<>();
    final private DefaultListModel<RoomStatus> roomListModel = new DefaultListModel<>();

    // GUI elements
    private JList<RoomStatus> roomJList;
    private JList<UserInfo> userJList;
    private JList<MessageInfo> chatJList;
    private JTextField message;
    private JButton sendBtn;
    private JButton joinRoom;

    public Chat(ArrayList<RoomStatus> roomStatus, User currentUser) {
        super("chat room name");
        this.currentUser = currentUser;

        roomStatuses = roomStatus;
        roomStatuses.forEach(statusUpdate -> addRoom(statusUpdate, false));
        roomJList.setModel(roomListModel);

        if (!roomStatuses.isEmpty()) {
            roomJList.setSelectedValue(roomStatuses.get(0), true);
        }

        System.out.println("my identity is:" + currentUser.getName());
    }

    @Override
    protected void setupComponents() {
        frame.setResizable(true);
        frame.setMinimumSize(new Dimension(450, 300));
        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(setupRoomList(), BorderLayout.LINE_START);
        contentPane.add(setupUserList(), BorderLayout.LINE_END);
        contentPane.add(setupChat(), BorderLayout.CENTER);
    }

    public void onMessageSubmit(Function2<String, Integer> callback) {
        sendBtn.addActionListener(e -> {
            RoomStatus selectedRoom = roomJList.getSelectedValue();
            String text = message.getText();
            message.setText("");
            if (!text.isEmpty()) {
                int selectedRoomId = selectedRoom.getRoomInfo().getId();
                callback.apply(text, selectedRoomId);
                MessageInfo messageInfo = new MessageInfo(text, currentUser.getId(), new Date(), currentUser.getName(), selectedRoomId);
                messageRecieved(messageInfo);
            }
        });
    }

    public void onRoomCreated(Lambdas.Function0 callback) {
        joinRoom.addActionListener(e -> callback.apply());
    }

    private void onRoomSelected() {
        if (roomJList.isSelectionEmpty()) {
            return;
        }

        RoomStatus room = roomJList.getSelectedValue();

        System.out.println("selected room " + room.getRoomInfo().getId());

        roomJList.setSelectedValue(room, true);

        updateUserList(room);

        updateChatList(room);
    }

    public void addRoom(RoomStatus room, boolean setFocus) {
        roomListModel.addElement(room);
        if (setFocus) {
            roomJList.setSelectedValue(room, true);
        }

    }

    private JPanel setupRoomList() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));

        //create the list
        roomJList = new JList<>();
        roomJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(roomJList), BorderLayout.CENTER);

        roomJList.setFixedCellHeight(25);
        roomJList.setFixedCellWidth(100);
        roomJList.setCellRenderer(new RoomListRenderer());

        roomJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onRoomSelected();
            }
        });

        joinRoom = new JButton("Join room");
        panel.add(joinRoom, BorderLayout.PAGE_END);

        return panel;
    }

    private JPanel setupUserList() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));

        userJList = new JList<>();
        panel.add(new JScrollPane(userJList), BorderLayout.CENTER);

        userJList.setFixedCellHeight(25);
        userJList.setFixedCellWidth(100);
        userJList.setMinimumSize(new Dimension(100, 25));
        userJList.setCellRenderer(new UserListRenderer());

        return panel;
    }

    private JPanel setupChat() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        panel.setPreferredSize(new Dimension(400, 300));

        chatJList = new JList<>();
        chatJList.setEnabled(false);

        chatJList.setFixedCellHeight(25);
        chatJList.setFixedCellWidth(100);
        chatJList.setMinimumSize(new Dimension(100, 25));
        chatJList.setCellRenderer(new MessageListRenderer(currentUser));
        chatJList.addComponentListener(new ComponentAdapter() {
            /**
             * https://stackoverflow.com/questions/7306295/swing-jlist-with-multiline-text-and-dynamic-height
             * @param e
             */
            @Override
            public void componentResized(ComponentEvent e) {
                // for core: force cache invalidation by temporarily setting fixed height
                chatJList.setFixedCellHeight(10);
                chatJList.setFixedCellHeight(-1);
            }
        });


        message = new JTextField();
        sendBtn = new JButton("Send");

        //Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(message);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(sendBtn);

        panel.add(new JScrollPane(chatJList), BorderLayout.CENTER);
        panel.add(buttonPane, BorderLayout.PAGE_END);

        return panel;
    }

    public void userConnected(int roomId, UserInfo userInfo) {
        for (RoomStatus roomStatus : roomStatuses) {
            if (roomStatus.getRoomInfo().getId() == roomId) {
                roomStatus.getActiveUsers().add(userInfo);
                if (roomJList.getSelectedValue().equals(roomStatus)) {
                    updateUserList(roomStatus);
                }
            }
        }
        ;
    }

    public void userDisconnected(int roomId, int userId) {
        for (RoomStatus roomStatus : roomStatuses) {
            if (roomStatus.getRoomInfo().getId() == roomId) {
                // iterator to safely remove from array
                Iterator<UserInfo> i = roomStatus.getActiveUsers().iterator();
                while (i.hasNext()) {
                    UserInfo userInfo = i.next();
                    if (userInfo.getId() == userId) {
                        i.remove();
                        if (roomJList.getSelectedValue().equals(roomStatus)) {
                            updateUserList(roomStatus);
                        }
                        break;
                    }
                }
                break;
            }
        }
    }

    private void updateUserList(RoomStatus room) {
        DefaultListModel<UserInfo> userListModel = new DefaultListModel<>();
        room.getActiveUsers().forEach(userListModel::addElement);
        userJList.setModel(userListModel);
    }

    private void updateChatList(RoomStatus room) {
        DefaultListModel<MessageInfo> chatListModel = new DefaultListModel<>();
        room.getMessages().forEach(chatListModel::addElement);
        chatJList.setModel(chatListModel);
    }

    public void messageRecieved(MessageInfo message) {
        for (RoomStatus roomStatus : roomStatuses) {
            if (roomStatus.getRoomInfo().getId() == message.getRoomId()) {
                roomStatus.addMessage(message);
                if (roomJList.getSelectedValue().equals(roomStatus)) {
                    updateChatList(roomStatus);
                }
                break;
            }
        }
    }

}
