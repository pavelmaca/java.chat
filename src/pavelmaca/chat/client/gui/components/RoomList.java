package pavelmaca.chat.client.gui.components;

import pavelmaca.chat.client.gui.renderer.RoomListRenderer;
import pavelmaca.chat.share.Factory;
import pavelmaca.chat.share.Lambdas;
import pavelmaca.chat.share.model.RoomStatus;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by Assassik on 05.01.2017.
 */
public class RoomList implements Factory<JPanel> {

    final private DefaultListModel<RoomStatus> roomListModel = new DefaultListModel<>();
    private JList<RoomStatus> roomJList = new JList<>();
    private JButton joinRoomBtn = new JButton("Join room");

    @Override
    public JPanel create() {
        JPanel panel = new JPanel();
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
        JMenuItem disconnectRoom = new JMenuItem("Disconnect");
        popupMenu.add(disconnectRoom);
        JMenuItem renameRoom = new JMenuItem("Rename");
        popupMenu.add(renameRoom);
        JMenuItem changePasswordRoom = new JMenuItem("Change password");
        popupMenu.add(changePasswordRoom);
        JMenuItem deleteRoom = new JMenuItem("Delete");
        popupMenu.add(deleteRoom);

        //   roomJList.setComponentPopupMenu(popupMenu);
        Lambdas.Function1<MouseEvent> event = (e) -> {
            if (e.isPopupTrigger()) { //if the event shows the menu
                int index = roomJList.locationToIndex(e.getPoint());

                // check if event is triggered on cell
                if (index < 0 || !roomJList.getCellBounds(index, index).intersects(e.getX(), e.getY(), 1, 1)) {
                    return;
                }

                roomJList.setSelectedIndex(index); //select the item
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

     /*   roomJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onRoomSelected();
            }
        });*/

        panel.add(joinRoomBtn, BorderLayout.PAGE_END);

        TitledBorder title = BorderFactory.createTitledBorder("Room list");
        panel.setBorder(title);

        Dimension minimumSize = new Dimension(150, 25);
        roomJList.setMinimumSize(minimumSize);
        panel.setMinimumSize(minimumSize);

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


}
