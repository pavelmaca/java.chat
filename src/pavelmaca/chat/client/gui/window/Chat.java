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

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Chat extends Window {

    protected User currentUser;

    final protected DefaultListModel<RoomStatus> roomListModel = new DefaultListModel<>();
    JList<RoomStatus> roomJList;

    //final protected DefaultListModel<UserInfo> userListModel = new DefaultListModel<>();
    JList<UserInfo> userJList;

    //final protected DefaultListModel<MessageInfo> chatListModel = new DefaultListModel<>();
    JList<MessageInfo> chatJList;


    // GUI elements
    JTextField message;
    JButton sendBtn;

    JButton joinRoom;

  /*  protected void setupDemo() {
        currentUser = new User(0, "Assassik", "123");

        Random random = new Random(123456);

        userList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            userList.add(new User(0, "User " + i, "123"));
        }

        roomList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Room room = new Room(0, "room " + i, currentUser);
            roomList.add(room);
            int numOfUser = random.nextInt(userList.size()) + 1;
            for (int j = 0; j < numOfUser; j++) {
                room.addUser(userList.get(random.nextInt(userList.size())));
            }
        }


    }*/

    public Chat(ArrayList<RoomStatus> roomStatus, User currentUser) {
        super("chat room name");
        this.currentUser = currentUser;

        roomStatus.forEach(statusUpdate -> {
            addRoom(statusUpdate, false);
        });
        roomJList.setModel(roomListModel);

        if (!roomStatus.isEmpty()) {
            roomJList.setSelectedValue(roomStatus.get(0), true);
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
                callback.apply(text, selectedRoom.getRoomInfo().getId());
            }
        });
    }

    public void onRoomCreated(Lambdas.Function0 callback) {
        joinRoom.addActionListener(e -> {
            callback.apply();
        });
    }

    public void onRoomSelected() {
        if (roomJList.isSelectionEmpty()) {
            return;
        }

        RoomStatus room = roomJList.getSelectedValue();

        System.out.println("selected room " + room.getRoomInfo().getId());

        roomJList.setSelectedValue(room, true);

        DefaultListModel<UserInfo> userListModel = new DefaultListModel<>();
        room.getActiveUsers().stream().forEach(userListModel::addElement);
        userJList.setModel(userListModel);

        DefaultListModel<MessageInfo> chatListModel = new DefaultListModel<>();
        room.getMessages().stream().forEach(chatListModel::addElement);
        chatJList.setModel(chatListModel);
    }

    public void addRoom(RoomStatus room, boolean setFocus) {
        roomListModel.addElement(room);
        if (setFocus) {
            roomJList.setSelectedValue(room, true);
        }

    }


    protected JPanel setupRoomList() {
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

    protected JPanel setupUserList() {
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

    protected JPanel setupChat() {
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
                // next line possible if list is of type JXList
                //roomList.invalidateCellSizeCache();
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

   /* private DefaultListModel<Message> createDemoMessages(DefaultListModel<Message> listModel) {
        for (int i = 0; i < 20; i++) {
            User author;
            switch (i % 3) {
                case 0:
                    author = currentUser;
                    break;
                default:
                    author = userList.get(i % 3 + 2);
                    break;
            }
            listModel.addElement(new Message("Test message assssssssss sa sa sas as asasa s as safgdfgdf gfdgdg dg gdfg dfgdf gdfg gdg  " + i, author));
        }

        return listModel;
    }*/
}
