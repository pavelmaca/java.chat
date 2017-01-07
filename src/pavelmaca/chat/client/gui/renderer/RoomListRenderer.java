package pavelmaca.chat.client.gui.renderer;

import pavelmaca.chat.share.model.RoomStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class RoomListRenderer implements ListCellRenderer<RoomStatus> {

    JLabel nameLabel;
    JLabel countLabel;
    JPanel panel;

    public RoomListRenderer() {

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBorder(new EmptyBorder(5, 10, 5, 10)); // padding

        JPanel leftCom = new JPanel();
        leftCom.setLayout(new BoxLayout(leftCom, BoxLayout.X_AXIS));
        leftCom.setOpaque(false);
        nameLabel = new JLabel("");
        nameLabel.setOpaque(false);
        leftCom.add(nameLabel);
        leftCom.add(Box.createHorizontalGlue());
        panel.add(leftCom);

        Font nameFont = nameLabel.getFont();

        //panel.add(Box.createHorizontalGlue());
        JPanel rightComp = new JPanel();
        rightComp.setLayout(new BoxLayout(rightComp, BoxLayout.X_AXIS));
        rightComp.setOpaque(false);
        countLabel = new JLabel("");
        countLabel.setFont(new Font(nameFont.getFontName(), Font.PLAIN, nameFont.getSize() - 2));
        countLabel.setOpaque(false);
        rightComp.add(Box.createHorizontalGlue());
        rightComp.add(countLabel);

        panel.add(rightComp);

        // panel.add(rightComp);

    }

    @Override
    public Component getListCellRendererComponent(JList<? extends RoomStatus> list, RoomStatus room, int index,
                                                  boolean isSelected, boolean cellHasFocus) {

        nameLabel.setText(room.getRoomInfo().getName());

        countLabel.setText(room.countConnectedUsers() + "/" + room.countUsers());

        if (isSelected) {
            panel.setBackground(list.getSelectionBackground());
            panel.setForeground(list.getSelectionForeground());
        } else {
            panel.setBackground(list.getBackground());
            panel.setForeground(list.getForeground());
        }

        return panel;
    }
}