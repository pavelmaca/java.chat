package pavelmaca.chat.client.gui.components;

import pavelmaca.chat.client.gui.renderer.UserListRenderer;
import pavelmaca.chat.share.Factory;
import pavelmaca.chat.share.Lambdas;
import pavelmaca.chat.share.model.UserInfo;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by Assassik on 05.01.2017.
 */
public class UserList implements Factory<JPanel> {
    private JList<UserInfo> userJList;
    JPopupMenu popupMenu;
    JMenuItem banPopupItem;
    JMenuItem removeBanPopupItem;

    private UserInfo currentUserInRoom;

    public JPanel create() {
        JPanel panel = new JPanel();
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
        banPopupItem = new JMenuItem("Ban");
        popupMenu.add(banPopupItem);
        removeBanPopupItem = new JMenuItem("Remove ban");
        popupMenu.add(removeBanPopupItem);

        //   roomJList.setComponentPopupMenu(popupMenu);
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
                banPopupItem.setVisible(selectedUser.getStatus() != UserInfo.Status.BANNED);
                removeBanPopupItem.setVisible(selectedUser.getStatus() == UserInfo.Status.BANNED);

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

        return panel;
    }

   /* public void update(DefaultListModel<UserInfo> userModel){
        userJList.setModel(userModel);
    }*/

    public void show(TreeSet<UserInfo> userInfo, UserInfo identity) {
        this.currentUserInRoom = identity;
        DefaultListModel<UserInfo> userListModel = new DefaultListModel<>();
        userInfo.forEach(userListModel::addElement);
        userJList.setModel(userListModel);
    }

}
