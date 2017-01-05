package pavelmaca.chat.client.gui.renderer;

import pavelmaca.chat.share.model.RoomStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class RoomListRenderer extends JLabel implements ListCellRenderer<RoomStatus>{

    public RoomListRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends RoomStatus> list, RoomStatus room, int index,
                                                  boolean isSelected, boolean cellHasFocus) {

       /* String code = roomgetCode();
        ImageIcon imageIcon = new ImageIcon(getClass().getResource("/images/" + code + ".png"));

        setIcon(imageIcon);*/
        setText(room.getRoomInfo().getName());
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