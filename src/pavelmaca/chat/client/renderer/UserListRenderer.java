package pavelmaca.chat.client.renderer;

import pavelmaca.chat.client.model.Room;
import pavelmaca.chat.client.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class UserListRenderer extends JLabel implements ListCellRenderer<User>{

    public UserListRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends User> list, User user, int index,
                                                  boolean isSelected, boolean cellHasFocus) {

       /* String code = roomgetCode();
        ImageIcon imageIcon = new ImageIcon(getClass().getResource("/images/" + code + ".png"));

        setIcon(imageIcon);*/
        setText(user.getName());
        setBorder(new EmptyBorder(5,10, 5, 10)); // padding

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        return this;
    }
}