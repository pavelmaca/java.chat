package pavelmaca.chat.client.gui.renderer;

import pavelmaca.chat.share.model.UserInfo;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class UserListRenderer implements ListCellRenderer<UserInfo> {

    JLabel userNameLabel;
    JPanel panel;

    JLabel adminLabel;
    JLabel onlineLabel;

    ImageIcon onlineIcon;
    ImageIcon offlineIcon;
    ImageIcon admineIcon;
    ImageIcon banIcon;

    public UserListRenderer() {
        userNameLabel = new JLabel();

        adminLabel = new JLabel("");
        adminLabel.setBorder(new EmptyBorder(0, 0, 0, 5));
        onlineLabel = new JLabel("");

        try {
            BufferedImage onlineImage = ImageIO.read(new File("src/pavelmaca/chat/client/resources/online.png"));
            onlineIcon = new ImageIcon(onlineImage);

            BufferedImage offImage = ImageIO.read(new File("src/pavelmaca/chat/client/resources/offline.png"));
            offlineIcon = new ImageIcon(offImage);

            BufferedImage adminImage = ImageIO.read(new File("src/pavelmaca/chat/client/resources/admin.png"));
            admineIcon = new ImageIcon(adminImage.getScaledInstance(16, 16, Image.SCALE_SMOOTH));

            BufferedImage banImage = ImageIO.read(new File("src/pavelmaca/chat/client/resources/ban.png"));
            banIcon = new ImageIcon(banImage.getScaledInstance(26, 26, Image.SCALE_SMOOTH));

            onlineLabel.setIcon(banIcon);
            adminLabel.setIcon(admineIcon);
        } catch (IOException e) {
            e.printStackTrace();
        }

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.setBorder(new EmptyBorder(5, 10, 5, 0)); // padding

        panel.add(userNameLabel);
        panel.add(Box.createHorizontalGlue());
        panel.add(adminLabel);
        panel.add(onlineLabel);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends UserInfo> list, UserInfo user, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        userNameLabel.setText(user.getName());

        userNameLabel.setBackground(list.getBackground());
        userNameLabel.setForeground(list.getForeground());

        return panel;
    }
}