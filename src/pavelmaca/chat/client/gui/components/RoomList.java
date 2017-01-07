package pavelmaca.chat.client.gui.components;

import pavelmaca.chat.client.gui.renderer.RoomListRenderer;
import pavelmaca.chat.share.Factory;
import pavelmaca.chat.share.Lambdas;
import pavelmaca.chat.share.model.RoomStatus;
import pavelmaca.chat.share.model.UserInfo;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by Assassik on 05.01.2017.
 */
public class RoomList implements IComponent<JPanel> {

    final private DefaultListModel<RoomStatus> roomListModel = new DefaultListModel<>();

    private JList<RoomStatus> roomJList = new JList<>();
    private JButton joinRoomBtn = new JButton("Join room");

    private JMenuItem leaveRoomItem;
    private JMenuItem renameRoomItem;
    private JMenuItem removePasswordRoomItem;
    private JMenuItem setPasswordRoomItem;
    private JMenuItem changePasswordRoomItem;
    private JMenuItem deleteRoomItem;

    private JPanel panel;

    private UserInfo currentUser = null;

    public RoomList() {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));

        //create the list
        roomJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomJList.setFocusable(false);
        roomJList.setModel(roomListModel);
        panel.add(new JScrollPane(roomJList), BorderLayout.CENTER);

        //roomJList.setFixedCellHeight(25);
        roomJList.setFixedCellWidth(100);
        roomJList.setCellRenderer(new RoomListRenderer());

        JPopupMenu popupMenu = new JPopupMenu();
        leaveRoomItem = new JMenuItem("Leave");
        popupMenu.add(leaveRoomItem);
        renameRoomItem = new JMenuItem("Rename");
        popupMenu.add(renameRoomItem);

        changePasswordRoomItem = new JMenuItem("Change password");
        popupMenu.add(changePasswordRoomItem);

        removePasswordRoomItem = new JMenuItem("Remove password");
        popupMenu.add(removePasswordRoomItem);

        setPasswordRoomItem = new JMenuItem("Set password");
        popupMenu.add(setPasswordRoomItem);

        deleteRoomItem = new JMenuItem("Delete");
        popupMenu.add(deleteRoomItem);

        //   roomJList.setComponentPopupMenu(popupMenu);
        Lambdas.Function1<MouseEvent> event = (e) -> {
            if (e.isPopupTrigger()) { //if the event shows the menu
                int index = roomJList.locationToIndex(e.getPoint());

                // check if event is triggered on cell
                if (index < 0 || !roomJList.getCellBounds(index, index).intersects(e.getX(), e.getY(), 1, 1)) {
                    return;
                }

                roomJList.setSelectedIndex(index); //select the item

                RoomStatus selectedRoom = roomJList.getSelectedValue();
                boolean isOwner = selectedRoom.getUserInfo(currentUser).getRank() == UserInfo.Rank.OWNER;
                renameRoomItem.setVisible(isOwner);
                if(isOwner){
                    changePasswordRoomItem.setVisible(selectedRoom.getRoomInfo().hasPassword());
                    removePasswordRoomItem.setVisible(selectedRoom.getRoomInfo().hasPassword());

                    setPasswordRoomItem.setVisible(!selectedRoom.getRoomInfo().hasPassword());
                }else{
                    changePasswordRoomItem.setVisible(false);
                    removePasswordRoomItem.setVisible(false);
                    setPasswordRoomItem.setVisible(false);
                }

                deleteRoomItem.setVisible(isOwner);

                popupMenu.show(roomJList, e.getX(), e.getY()); //and show the menu
            }
        };
        roomJList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                event.apply(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                event.apply(e);
            }
        });


        panel.add(joinRoomBtn, BorderLayout.PAGE_END);

        TitledBorder title = BorderFactory.createTitledBorder("Room list");
        panel.setBorder(title);

        Dimension minimumSize = new Dimension(150, 25);
        roomJList.setMinimumSize(minimumSize);
        panel.setMinimumSize(minimumSize);
    }

    public JPanel getComponent() {
        return panel;
    }

    public RoomStatus getSelected() {
        return roomJList.getSelectedValue();
    }

    public boolean isSelected() {
        return !roomJList.isSelectionEmpty();
    }

    public void setSelected(RoomStatus roomStatus) {
        roomJList.setSelectedValue(roomStatus, true);
    }

    public void addRoom(RoomStatus room) {
        roomListModel.addElement(room);
    }

    public void removeRoom(RoomStatus room) {
        roomListModel.removeElement(room);
    }

    public void refresh() {
        roomJList.repaint();
        roomJList.updateUI();
    }


    public void setCurrentUser(UserInfo currentUser) {
        this.currentUser = currentUser;
    }

    // ------ Listeners

    public void addJoinActionListener(ActionListener listener) {
        joinRoomBtn.addActionListener(listener);
    }

    public void addRoomSelectedListener(Lambdas.Function1<RoomStatus> listener) {
        roomJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                listener.apply(getSelected());
            }
        });
    }

    public void addLeaveActionListener(Lambdas.Function1<RoomStatus> listener) {
        leaveRoomItem.addActionListener(e -> {
            listener.apply(getSelected());
        });
    }

    public void addRenameActionListener(Lambdas.Function1<RoomStatus> listener) {
        renameRoomItem.addActionListener(e -> {
            listener.apply(getSelected());
        });
    }

    public void addChangePasswordActionListener(Lambdas.Function1<RoomStatus> listener) {
        changePasswordRoomItem.addActionListener(e -> {
            listener.apply(getSelected());
        });
    }

    public void addRemovePasswordActionListener(Lambdas.Function1<RoomStatus> listener) {
        removePasswordRoomItem.addActionListener(e -> {
            listener.apply(getSelected());
        });
    }

    public void addSetPasswordActionListener(Lambdas.Function1<RoomStatus> listener) {
        setPasswordRoomItem.addActionListener(e -> {
            listener.apply(getSelected());
        });
    }

    public void addDeleteActionListener(Lambdas.Function1<RoomStatus> listener) {
        deleteRoomItem.addActionListener(e -> {
            listener.apply(getSelected());
        });
    }

}
