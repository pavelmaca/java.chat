package pavelmaca.chat.client.gui.window;

import pavelmaca.chat.client.Session;
import pavelmaca.chat.client.gui.components.HeaderMenu;
import pavelmaca.chat.client.gui.components.RoomList;
import pavelmaca.chat.client.gui.components.UserList;
import pavelmaca.chat.share.Lambdas;
import pavelmaca.chat.share.Lambdas.Function2;
import pavelmaca.chat.client.gui.renderer.MessageListRenderer;
import pavelmaca.chat.server.entity.User;
import pavelmaca.chat.share.model.MessageInfo;
import pavelmaca.chat.share.model.RoomStatus;
import pavelmaca.chat.share.model.UserInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Chat extends Window {

    private User currentUser;
    private Session session;

    private ArrayList<RoomStatus> roomStatuses = new ArrayList<>();

    // GUI elements
    private UserList userList;
    private RoomList roomList;
    private HeaderMenu headerMenu;

    private JList<MessageInfo> chatJList;
    private JTextField message;
    private JButton sendBtn;

    private boolean closingForLogout = false;

    private ArrayList<Lambdas.Function0> disconnectListeners = new ArrayList<>();

    public Chat(Session session, ArrayList<RoomStatus> roomStatus, User currentUser) {
        super("Chat");
        this.currentUser = currentUser;
        this.session = session;

        roomStatuses = roomStatus;
        roomStatuses.forEach(statusUpdate -> roomList.addRoom(statusUpdate));
        if (!roomStatuses.isEmpty()) {
            roomList.setSelected(roomStatuses.get(0));
        }
    }

    @Override
    protected void setupComponents() {
        roomList = new RoomList();
        headerMenu = new HeaderMenu();
        userList = new UserList();

        roomList.addRoomSelectedListener(room -> {
            //change window title to room name
            frame.setTitle(room.getRoomInfo().getName());
            System.out.println("selected room " + room.getRoomInfo().getId());
            userList.show(room.getActiveUsers());
            updateChatList(room);
        });

        headerMenu.addChangePasswordActionListener(e -> {
            ChangePassword changePassword = new ChangePassword();
            changePassword.onCancel(changePassword::close);
            changePassword.onSubmit(newPassword -> {
                if (session.changePassword(newPassword)) {
                    changePassword.close();
                } else {
                    changePassword.showError("Error during password change, try again.");
                }
            });
        });

        frame.setJMenuBar(headerMenu.create());


        frame.setResizable(true);
        frame.setMinimumSize(new Dimension(450, 300));

        Container contentPane = frame.getContentPane();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                roomList.create(), setupChat());
        contentPane.add(splitPane, BorderLayout.CENTER);

    }

    public void onMessageSubmit(Function2<String, Integer> callback) {
        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!roomList.isSelected()) {
                    return;
                }
                RoomStatus selectedRoom = roomList.getSelected();
                String text = message.getText();
                message.setText("");
                if (!text.isEmpty()) {
                    int selectedRoomId = selectedRoom.getRoomInfo().getId();
                    callback.apply(text, selectedRoomId);
                    MessageInfo messageInfo = new MessageInfo(text, currentUser.getId(), new Date(), currentUser.getName(), selectedRoomId);
                    messageRecieved(messageInfo);
                }
            }
        };

        message.addActionListener(action);
        sendBtn.addActionListener(action);
    }

    public void onJoinRoomClicked(ActionListener listener) {
        roomList.addJoinActionListener(listener);
    }


    public void addRoom(RoomStatus room, boolean setSelected) {
        roomList.addRoom(room);
        if (setSelected) {
            roomList.setSelected(room);
        }

    }

    private JSplitPane setupChat() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        panel.setPreferredSize(new Dimension(400, 300));

        chatJList = new JList<>();
        chatJList.setEnabled(false);

        chatJList.setFixedCellHeight(25);
        chatJList.setFixedCellWidth(100);
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

        Dimension minimumSize = new Dimension(300, 25);
        chatJList.setMinimumSize(minimumSize);
        panel.setMinimumSize(minimumSize);

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


        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(panel);
        splitPane.setRightComponent(userList.create());
        splitPane.setResizeWeight(1);
        return splitPane;
    }

    public void userConnected(int roomId, UserInfo userInfo) {
        for (RoomStatus roomStatus : roomStatuses) {
            if (roomStatus.getRoomInfo().getId() == roomId) {
                roomStatus.getActiveUsers().add(userInfo);
                if (roomList.getSelected().equals(roomStatus)) {
                    userList.show(roomStatus.getActiveUsers());
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
                        if (roomList.getSelected().equals(roomStatus)) {
                            userList.show(roomStatus.getActiveUsers());
                        }
                        break;
                    }
                }
                break;
            }
        }
    }

    private void updateChatList(RoomStatus room) {
        DefaultListModel<MessageInfo> chatListModel = new DefaultListModel<>();
        room.getMessages().forEach(chatListModel::addElement);
        chatJList.setModel(chatListModel);
        chatJList.ensureIndexIsVisible(chatListModel.size() - 1);
    }

    public void messageRecieved(MessageInfo message) {
        for (RoomStatus roomStatus : roomStatuses) {
            if (roomStatus.getRoomInfo().getId() == message.getRoomId()) {
                roomStatus.addMessage(message);
                if (roomList.getSelected().equals(roomStatus)) {
                    updateChatList(roomStatus);
                }
                break;
            }
        }
    }

    public void addDisconnectListener(Lambdas.Function0 callback) {
       // disconnectListeners.add(callback);
        headerMenu.addDisconnectActionListener(e -> callback.apply());
    }

    public void addLogoutListener(Lambdas.Function0 callback) {
        headerMenu.addLogoutActionListener(e -> {
            closingForLogout = true;
            callback.apply();
        });
    }

    public void addWindowsCloseListener(Lambdas.Function1<Boolean> callback) {
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                callback.apply(closingForLogout);
            }
        });
    }

   /* public void disconect() {
        for (Lambdas.Function0 disconnectListener : disconnectListeners) {
            disconnectListener.apply();
        }
    }*/
}
