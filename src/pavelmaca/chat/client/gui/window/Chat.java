package pavelmaca.chat.client.gui.window;

import pavelmaca.chat.client.Client;
import pavelmaca.chat.client.model.Message;
import pavelmaca.chat.client.model.Room;
import pavelmaca.chat.client.model.User;
import pavelmaca.chat.client.renderer.MessageListRenderer;
import pavelmaca.chat.client.renderer.RoomListRenderer;
import pavelmaca.chat.client.renderer.UserListRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.Function;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Chat extends Window {

    protected ArrayList<User> userList;
    protected User currentUser;
    protected ArrayList<Room> roomList;

    protected void setupDemo() {
        currentUser = new User("Assassik", "123");

        Random random = new Random(123456);

        userList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            userList.add(new User("User " + i, "123"));
        }

        roomList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Room room = new Room("room " + i, currentUser);
            roomList.add(room);
            int numOfUser = random.nextInt(userList.size()) + 1;
            for (int j = 0; j < numOfUser; j++) {
                room.addUser(userList.get(random.nextInt(userList.size())));
            }
        }


    }

    public Chat(User currentUser) {
        super("chat room name");
        this.currentUser = currentUser;
        System.out.println("my identity is:"+currentUser.getName());
    }

    @Override
    protected void setupComponents() {
        setupDemo();

        frame.setResizable(true);
        frame.setMinimumSize(new Dimension(450, 300));
        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(setupRoomList(), BorderLayout.LINE_START);
        contentPane.add(setupUserList(), BorderLayout.LINE_END);
        contentPane.add(setupChat(), BorderLayout.CENTER);
    }

    protected JPanel setupRoomList() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));

        //create the model and add elements
        final DefaultListModel<Room> listModel = new DefaultListModel<>();
        roomList.forEach(listModel::addElement);

        //create the list
        JList<Room> roomListModel = new JList<>(listModel);
        panel.add(new JScrollPane(roomListModel), BorderLayout.CENTER);

        roomListModel.setFixedCellHeight(25);
        roomListModel.setFixedCellWidth(100);
        roomListModel.setCellRenderer(new RoomListRenderer());

        JButton joinRoom = new JButton("Join room");
        panel.add(joinRoom, BorderLayout.PAGE_END);

        return panel;
    }

    protected JPanel setupUserList() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));


        //create the model and add elements
        //create the model and add elements
        final DefaultListModel<User> listModel = new DefaultListModel<>();
        roomList.get(0).getUserList().forEach(listModel::addElement);

        //create the list
        JList<User> userListModel = new JList<>(listModel);
        panel.add(new JScrollPane(userListModel), BorderLayout.CENTER);

        userListModel.setFixedCellHeight(25);
        userListModel.setFixedCellWidth(100);
        userListModel.setMinimumSize(new Dimension(100, 25));
        userListModel.setCellRenderer(new UserListRenderer());

        return panel;
    }

    protected JPanel setupChat() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        panel.setPreferredSize(new Dimension(400, 300));

        //create the model and add elements
        DefaultListModel<Message> listModel = new DefaultListModel<>();
        createDemoMessages(listModel);

        //create the list
        JList<Message> roomList = new JList<>(listModel);
        roomList.setEnabled(false);

        roomList.setFixedCellHeight(25);
        roomList.setFixedCellWidth(100);
        roomList.setMinimumSize(new Dimension(100, 25));
        roomList.setCellRenderer(new MessageListRenderer(currentUser));
        roomList.addComponentListener(new ComponentAdapter() {
            /**
             * https://stackoverflow.com/questions/7306295/swing-jlist-with-multiline-text-and-dynamic-height
             * @param e
             */
            @Override
            public void componentResized(ComponentEvent e) {
                // next line possible if list is of type JXList
                //roomList.invalidateCellSizeCache();
                // for core: force cache invalidation by temporarily setting fixed height
                roomList.setFixedCellHeight(10);
                roomList.setFixedCellHeight(-1);
            }
        });


        JTextField message = new JTextField();
        JButton sendBtn = new JButton("Send");

        //Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(message);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(sendBtn);

        panel.add(new JScrollPane(roomList), BorderLayout.CENTER);
        panel.add(buttonPane, BorderLayout.PAGE_END);


        return panel;
    }

    private DefaultListModel<Message> createDemoMessages(DefaultListModel<Message> listModel) {
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
    }
}
