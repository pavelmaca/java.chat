package pavelmaca.chat.client.gui.components;

import pavelmaca.chat.client.gui.renderer.UserListRenderer;
import pavelmaca.chat.share.Factory;
import pavelmaca.chat.share.model.UserInfo;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Assassik on 05.01.2017.
 */
public class UserList implements Factory<JPanel> {
    private JList<UserInfo> userJList;

    public JPanel create(){
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));

        userJList = new JList<>();
        userJList.setFocusable(false);
        panel.add(new JScrollPane(userJList), BorderLayout.CENTER);

        userJList.setFixedCellHeight(25);
        userJList.setFixedCellWidth(100);
        userJList.setCellRenderer(new UserListRenderer());

        TitledBorder title = BorderFactory.createTitledBorder("Active users");
        panel.setBorder(title);

        Dimension minimumSize = new Dimension(120, 25);
        userJList.setMinimumSize(minimumSize);
        panel.setMinimumSize(minimumSize);

        return panel;
    }

   /* public void update(DefaultListModel<UserInfo> userModel){
        userJList.setModel(userModel);
    }*/

    public void show(ArrayList<UserInfo> userInfo){
        DefaultListModel<UserInfo> userListModel = new DefaultListModel<>();
        userInfo.forEach(userListModel::addElement);
        userJList.setModel(userListModel);
    }

}
