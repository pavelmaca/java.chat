package pavelmaca.chat.client.gui.window;

import pavelmaca.chat.client.Session;
import pavelmaca.chat.client.gui.components.ChatList;
import pavelmaca.chat.client.gui.components.HeaderMenu;
import pavelmaca.chat.client.gui.components.RoomList;
import pavelmaca.chat.client.gui.components.UserList;
import pavelmaca.chat.share.Lambdas;
import pavelmaca.chat.client.gui.renderer.MessageListRenderer;
import pavelmaca.chat.server.entity.User;
import pavelmaca.chat.share.model.MessageInfo;
import pavelmaca.chat.share.model.RoomInfo;
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
public class Main extends Window {

    //private User currentUser;
    private Session session;

    private ArrayList<RoomStatus> roomStatuses = new ArrayList<>();

    // GUI elements
    private UserList userList;
    private RoomList roomList;
    private ChatList chatList;
    private HeaderMenu headerMenu;

    private boolean closingForLogout = false;

    public Main(Session session, ArrayList<RoomStatus> roomStatus, User currentUser) {
        super("Chat");
        //  this.currentUser = currentUser;
        this.session = session;

        roomStatuses = roomStatus;
        roomStatuses.forEach(statusUpdate -> roomList.addRoom(statusUpdate));
        if (!roomStatuses.isEmpty()) {
            roomList.setSelected(roomStatuses.get(0));
        }

        chatList.setCurrentUser(currentUser);
        chatList.addMessageSubmitListener((text) -> {
            if (!roomList.isSelected()) {
                return;
            }

            RoomStatus selectedRoom = roomList.getSelected();
            int selectedRoomId = selectedRoom.getRoomInfo().getId();

            // send message to server
            session.sendMessage(text, selectedRoomId);

            MessageInfo messageInfo = new MessageInfo(text, currentUser.getId(), new Date(), currentUser.getName(), selectedRoomId);
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
        frame.setJMenuBar(headerMenu.create());

        chatList = new ChatList();


        userList = new UserList();

        roomList.addRoomSelectedListener(room -> {
            //change window title to room name
            frame.setTitle(room.getRoomInfo().getName());
            System.out.println("selected room " + room.getRoomInfo().getId());
            userList.show(room.getActiveUsers());
            chatList.show(room.getMessages());
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
        chatSplitPane.setLeftComponent(chatList.create());
        chatSplitPane.setRightComponent(userList.create());
        chatSplitPane.setResizeWeight(1);

        JSplitPane roomSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                roomList.create(), chatSplitPane);
        contentPane.add(roomSplitPane, BorderLayout.CENTER);

    }

    private void openJoinRoomWindow() {
        ArrayList<RoomInfo> roomList = session.getAvailableRoomList();

        JoinRoom joinRoomWindow = new JoinRoom(roomList);
        joinRoomWindow.onJoinSubmit(roomId -> {
            RoomStatus room = session.joinRoom(roomId);
            if (room != null) {
                addRoom(room, true);
                joinRoomWindow.close();
            }
            // TODO room authentication
        });

        joinRoomWindow.onNewRoomSubmit(roomName -> {
            RoomStatus room = session.createRoom(roomName);
            if (room != null) {
                addRoom(room, true);
                joinRoomWindow.close();
            }
        });

    }

    private void addRoom(RoomStatus room, boolean setSelected) {
        roomStatuses.add(room);
        roomList.addRoom(room);
        if (setSelected) {
            roomList.setSelected(room);
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
        for (RoomStatus roomStatus : roomStatuses) {
            if (roomStatus.getRoomInfo().getId() == roomId) {
                roomStatus.getActiveUsers().add(userInfo);
                if (roomList.getSelected().equals(roomStatus)) {
                    userList.show(roomStatus.getActiveUsers());
                }
            }
        }
        roomList.refresh();
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
        roomList.refresh();
    }

    public void messageReceived(MessageInfo message) {
        for (RoomStatus roomStatus : roomStatuses) {
            if (roomStatus.getRoomInfo().getId() == message.getRoomId()) {
                roomStatus.addMessage(message);
                if (roomList.getSelected().equals(roomStatus)) {
                    chatList.show(roomStatus.getMessages());
                }
                break;
            }
        }
    }


}
