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
 * @author Pavel Máca <maca.pavel@gmail.com>
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


        // Right click event
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
                if (isOwner) {
                    changePasswordRoomItem.setVisible(selectedRoom.getRoomInfo().hasPassword());
                    removePasswordRoomItem.setVisible(selectedRoom.getRoomInfo().hasPassword());

                    setPasswordRoomItem.setVisible(!selectedRoom.getRoomInfo().hasPassword());
                } else {
                    changePasswordRoomItem.setVisible(false);
                    removePasswordRoomItem.setVisible(false);
                    setPasswordRoomItem.setVisible(false);
                }

                deleteRoomItem.setVisible(isOwner);

                popupMenu.show(roomJList, e.getX(), e.getY()); //show the menu
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

    /**
     * @return Selected room
     */
    public RoomStatus getSelected() {
        return roomJList.getSelectedValue();
    }

    public boolean isSelected() {
        return !roomJList.isSelectionEmpty();
    }

    /**
     * @param roomStatus Select this room
     */
    public void setSelected(RoomStatus roomStatus) {
        roomJList.setSelectedValue(roomStatus, true);
    }

    /**
     * @param room Add room to the list
     */
    public void addRoom(RoomStatus room) {
        roomListModel.addElement(room);
    }

    /**
     * @param room Remove room from the list
     */
    public void removeRoom(RoomStatus room) {
        roomListModel.removeElement(room);
    }

    /**
     * Refresh component to update online status counter
     */
    public void refresh() {
        roomJList.repaint();
    }


    /**
     * @param currentUser Current logged user
     */
    public void setCurrentUser(UserInfo currentUser) {
        this.currentUser = currentUser;
    }

    // ------ Listeners

    /**
     * @param listener Join room button clicked
     */
    public void addJoinActionListener(ActionListener listener) {
        joinRoomBtn.addActionListener(listener);
    }

    /**
     * @param listener Room selected
     */
    public void addRoomSelectedListener(Lambdas.Function1<RoomStatus> listener) {
        roomJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                listener.apply(getSelected());
            }
        });
    }

    /**
     * @param listener Leave button clicked
     */
    public void addLeaveActionListener(Lambdas.Function1<RoomStatus> listener) {
        leaveRoomItem.addActionListener(e -> {
            listener.apply(getSelected());
        });
    }

    /**
     * @param listener Rename button clicked
     */
    public void addRenameActionListener(Lambdas.Function1<RoomStatus> listener) {
        renameRoomItem.addActionListener(e -> {
            listener.apply(getSelected());
        });
    }

    /**
     * @param listener Change  password button clicked
     */
    public void addChangePasswordActionListener(Lambdas.Function1<RoomStatus> listener) {
        changePasswordRoomItem.addActionListener(e -> {
            listener.apply(getSelected());
        });
    }

    /**
     * @param listener Remove password button clicked
     */
    public void addRemovePasswordActionListener(Lambdas.Function1<RoomStatus> listener) {
        removePasswordRoomItem.addActionListener(e -> {
            listener.apply(getSelected());
        });
    }

    /**
     * @param listener Set password button clicked
     */
    public void addSetPasswordActionListener(Lambdas.Function1<RoomStatus> listener) {
        setPasswordRoomItem.addActionListener(e -> {
            listener.apply(getSelected());
        });
    }

    /**
     * @param listener Delete room button clicked
     */
    public void addDeleteActionListener(Lambdas.Function1<RoomStatus> listener) {
        deleteRoomItem.addActionListener(e -> {
            listener.apply(getSelected());
        });
    }

}
