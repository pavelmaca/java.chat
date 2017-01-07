package pavelmaca.chat.client.gui.components;

import pavelmaca.chat.client.gui.renderer.UserListRenderer;
import pavelmaca.chat.share.Lambdas;
import pavelmaca.chat.share.model.UserInfo;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.TreeSet;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class UserList implements IComponent<JPanel> {
    private JList<UserInfo> userJList;
    private JPopupMenu popupMenu;
    private JMenuItem banItem;
    private JMenuItem removeBanItem;

    private UserInfo currentUserInRoom;

    private JPanel panel;

    public UserList() {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));

        userJList = new JList<>();
        userJList.setFocusable(false);
        panel.add(new JScrollPane(userJList), BorderLayout.CENTER);

        userJList.setFixedCellHeight(25);
        userJList.setFixedCellWidth(100);
        userJList.setCellRenderer(new UserListRenderer());

        TitledBorder title = BorderFactory.createTitledBorder("Users");
        panel.setBorder(title);

        Dimension minimumSize = new Dimension(120, 25);
        userJList.setMinimumSize(minimumSize);
        panel.setMinimumSize(minimumSize);

        popupMenu = new JPopupMenu();
        banItem = new JMenuItem("Ban");
        popupMenu.add(banItem);
        removeBanItem = new JMenuItem("Remove ban");
        popupMenu.add(removeBanItem);

        // right click listener
        Lambdas.Function1<MouseEvent> event = (e) -> {
            if (e.isPopupTrigger() && currentUserInRoom.getRank().equals(UserInfo.Rank.OWNER)) { //if the event shows the menu
                int index = userJList.locationToIndex(e.getPoint());

                // check if event is triggered on cell
                if (index < 0 || !userJList.getCellBounds(index, index).intersects(e.getX(), e.getY(), 1, 1)) {
                    return;
                }

                userJList.setSelectedIndex(index); //select the item
                UserInfo selectedUser = userJList.getSelectedValue();
                if (selectedUser.equals(currentUserInRoom)) {
                    return;
                }
                banItem.setVisible(selectedUser.getStatus() != UserInfo.Status.BANNED);
                removeBanItem.setVisible(selectedUser.getStatus() == UserInfo.Status.BANNED);

                popupMenu.show(userJList, e.getX(), e.getY()); //and show the menu
            }
        };
        userJList.addMouseListener(new MouseAdapter() {
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
    }

    @Override
    public JPanel getComponent() {
        return panel;
    }


    /**
     * @param userInfo Set of users in room
     * @param identity Current logged user
     */
    public void show(TreeSet<UserInfo> userInfo, UserInfo identity) {
        this.currentUserInRoom = identity;
        DefaultListModel<UserInfo> userListModel = new DefaultListModel<>();
        userInfo.forEach(userListModel::addElement);
        userJList.setModel(userListModel);
    }

    /**
     * @param listener Ban button clicked listener
     */
    public void addBanActionListener(Lambdas.Function1<UserInfo> listener) {
        banItem.addActionListener(e -> {
            listener.apply(userJList.getSelectedValue());
        });
    }


    /**
     * @param listener Remove ban button clicked listener
     */
    public void addRemoveBanActionListener(Lambdas.Function1<UserInfo> listener) {
        removeBanItem.addActionListener(e -> {
            listener.apply(userJList.getSelectedValue());
        });
    }


}
