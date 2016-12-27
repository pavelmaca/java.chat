package pavelmaca.chat.client.renderer;

import pavelmaca.chat.client.model.Room;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class RoomListRenderer extends JLabel implements ListCellRenderer<Room>{

    public RoomListRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Room> list, Room room, int index,
                                                  boolean isSelected, boolean cellHasFocus) {

       /* String code = roomgetCode();
        ImageIcon imageIcon = new ImageIcon(getClass().getResource("/images/" + code + ".png"));

        setIcon(imageIcon);*/
        setText(room.getName());
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