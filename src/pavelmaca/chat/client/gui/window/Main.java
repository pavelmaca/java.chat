package pavelmaca.chat.client.gui.window;

import pavelmaca.chat.client.Session;
import pavelmaca.chat.client.gui.components.*;
import pavelmaca.chat.share.Lambdas;
import pavelmaca.chat.share.ResponseException;
import pavelmaca.chat.share.model.MessageInfo;
import pavelmaca.chat.share.model.RoomInfo;
import pavelmaca.chat.share.model.RoomStatus;
import pavelmaca.chat.share.model.UserInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Main extends Window {

    private UserInfo identity;
    private Session session;

    private HashMap<Integer, RoomStatus> roomStatuses = new HashMap<>();

    // GUI elements
    private UserList userList;
    private RoomList roomList;
    private ChatList chatList;
    private HeaderMenu headerMenu;

    private boolean closingForLogout = false;

    public Main(Session session, HashMap<Integer, RoomStatus> roomStatus, UserInfo identity) {
        super("Chat");
        this.identity = identity;
        this.session = session;

        roomList.setCurrentUser(identity);

        roomStatuses = roomStatus;
        roomStatuses.forEach((integer, statusUpdate) -> roomList.addRoom(statusUpdate));
        selectFirstRoom();

        chatList.setCurrentUser(identity);
        chatList.addMessageSubmitListener((text) -> {
            if (!roomList.isSelected()) {
                return;
            }

            RoomStatus selectedRoom = roomList.getSelected();
            int selectedRoomId = selectedRoom.getRoomInfo().getId();

            // send message to server
            session.sendMessage(text, selectedRoomId);

            MessageInfo messageInfo = new MessageInfo(text, identity.getId(), new Date(), identity.getName(), selectedRoomId);
            messageReceived(messageInfo); //act as message was received from server
        });

        // windows close listener
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (closingForLogout) {
                    session.logout();
                } else {
                    session.close();
                }
            }
        });
    }

    /// ----- GUI

    @Override
    protected void setupComponents() {
        frame.setResizable(true);
        frame.setMinimumSize(new Dimension(450, 300));

        roomList = new RoomList();
        roomList.addJoinActionListener(e -> openJoinRoomWindow());

        headerMenu = new HeaderMenu();
        frame.setJMenuBar(headerMenu.getComponent());

        chatList = new ChatList();


        userList = new UserList();

        roomList.addRoomSelectedListener(room -> {
            if (room == null) return;

            //change window title to room name
            frame.setTitle(room.getRoomInfo().getName());

            System.out.println("selected room " + room.getRoomInfo().getId());
            userList.show(room.getUserList(), room.getUserInfo(identity));
            chatList.show(room.getMessages());
        });

        roomList.addLeaveActionListener(roomStatus -> {
            session.leaveRoom(roomStatus.getRoomInfo().getId());
            removeRoom(roomStatus);
        });

        roomList.addRenameActionListener(roomStatus -> {
            String newRoomName = (String) JOptionPane.showInputDialog(
                    frame,
                    "Change room name",
                    roomStatus.getRoomInfo().getName(),
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    roomStatus.getRoomInfo().getName());

            if (newRoomName != null && !newRoomName.isEmpty() && !newRoomName.equals(roomStatus.getRoomInfo().getName())) {
                if (session.roomChangeName(roomStatus.getRoomInfo().getId(), newRoomName)) {
                    //TODO check name duplicity on server, also on creation
                    roomStatus.getRoomInfo().setName(newRoomName);
                    roomList.refresh();
                } else {
                    System.out.println("Error during changing room name");
                }
            }
        });

        roomList.addChangePasswordActionListener(roomStatus -> {
            SimpleInputDialog dialog = new SimpleInputDialog(frame.getContentPane(), "Change password", "New room password");
            dialog.setValidator(s -> !s.isEmpty(), "Password is required");
            dialog.onSuccess(newPassword -> {
                try {
                    session.roomChangePassword(roomStatus.getRoomInfo().getId(), newPassword);
                } catch (ResponseException e) {
                    dialog.setErrorMessage(e.getMessage());
                    dialog.open();
                }
            });
            dialog.open();
        });

        roomList.addRemovePasswordActionListener(roomStatus -> {
            try {
                session.roomRemovePassword(roomStatus.getRoomInfo().getId());
                roomStatus.getRoomInfo().removePassword();
            } catch (ResponseException e) {
                showError(e.getMessage());
            }

        });

        roomList.addSetPasswordActionListener(roomStatus -> {
            SimpleInputDialog dialog = new SimpleInputDialog(frame.getContentPane(), "Set password", "Room password");
            dialog.setValidator(s -> !s.isEmpty(), "Password is required");
            dialog.onSuccess(newPassword -> {
                try {
                    session.roomChangePassword(roomStatus.getRoomInfo().getId(), newPassword);
                    roomStatus.getRoomInfo().setPassword();
                } catch (ResponseException e) {
                    dialog.setErrorMessage(e.getMessage());
                    dialog.open();
                }
            });
            dialog.open();
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

        Container contentPane = frame.getContentPane();

        JSplitPane chatSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        chatSplitPane.setLeftComponent(chatList.getComponent());
        chatSplitPane.setRightComponent(userList.getComponent());
        chatSplitPane.setResizeWeight(1);

        JSplitPane roomSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                roomList.getComponent(), chatSplitPane);
        contentPane.add(roomSplitPane, BorderLayout.CENTER);

    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame.getContentPane(), message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void openJoinRoomWindow() {
        ArrayList<RoomInfo> roomList = session.getAvailableRoomList();

        JoinRoom joinRoomWindow = new JoinRoom(roomList);
        joinRoomWindow.onJoinSubmit((roomId, password) -> {
            try {
                RoomStatus room = session.joinRoom(roomId, password);
                if (room != null) {
                    addRoom(room, true);
                    joinRoomWindow.close();
                }
            } catch (ResponseException e) {
                joinRoomWindow.showError(e.getMessage());
            }
        });

        joinRoomWindow.onNewRoomSubmit((roomName, roomPassword) -> {
            RoomStatus room = session.createRoom(roomName, roomPassword);
            if (room != null) {
                addRoom(room, true);
                joinRoomWindow.close();
            }
        });

    }

    private void addRoom(RoomStatus room, boolean setSelected) {
        roomStatuses.put(room.getRoomInfo().getId(), room);
        roomList.addRoom(room);
        if (setSelected) {
            roomList.setSelected(room);
        }

    }

    private void removeRoom(RoomStatus room) {
        roomStatuses.remove(room);
        roomList.removeRoom(room);
        if (roomList.getSelected() == null || roomList.getSelected().equals(room)) {
            selectFirstRoom();
        }

    }

    private void selectFirstRoom() {
        if (!roomStatuses.isEmpty()) {
            RoomStatus firstRoom = roomStatuses.entrySet().iterator().next().getValue();
            roomList.setSelected(firstRoom);
        }
    }

    /// ----- Listeners

    public void addDisconnectListener(Lambdas.Function0 callback) {
        headerMenu.addDisconnectActionListener(e -> callback.apply());
    }

    public void addLogoutListener(Lambdas.Function0 callback) {
        headerMenu.addLogoutActionListener(e -> {
            closingForLogout = true;
            callback.apply();
        });
    }

    /// ----- User/server actions

    public void userConnected(int roomId, UserInfo userInfo) {
        RoomStatus roomStatus = roomStatuses.get(roomId);
        roomStatus.userConnected(userInfo);
        if (roomList.getSelected().equals(roomStatus)) {
            userList.show(roomStatus.getUserList(), roomStatus.getUserInfo(identity));
        }
        roomList.refresh();
    }

    public void userDisconnected(int roomId, int userId) {
        RoomStatus roomStatus = roomStatuses.get(roomId);
        roomStatus.userDisconnected(userId);
        if (roomList.getSelected().equals(roomStatus)) {
            userList.show(roomStatus.getUserList(), roomStatus.getUserInfo(identity));
        }
        roomList.refresh();
    }

    public void messageReceived(MessageInfo message) {
        RoomStatus roomStatus = roomStatuses.get(message.getRoomId());
        roomStatus.addMessage(message);
        if (roomList.getSelected().equals(roomStatus)) {
            chatList.show(roomStatus.getMessages());
        }
    }

    public void userLeft(int roomId, int userId) {
        RoomStatus roomStatus = roomStatuses.get(roomId);
        roomStatus.userLeave(userId);
        if (roomList.getSelected().equals(roomStatus)) {
            userList.show(roomStatus.getUserList(), roomStatus.getUserInfo(identity));
        }
        roomList.refresh();
    }

    public void roomChangeName(int roomId, String newName) {
        RoomStatus roomStatus = roomStatuses.get(roomId);
        roomStatus.getRoomInfo().setName(newName);
        roomList.refresh();
    }
}
