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
 * Render user item in list
 *
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class UserListRenderer implements ListCellRenderer<UserInfo> {

    private JLabel userNameLabel;
    private JPanel panel;

    private JLabel adminLabel;
    private JLabel statusLabel;

    private ImageIcon onlineIcon = null;
    private ImageIcon offlineIcon = null;
    private ImageIcon admineIcon = null;
    private ImageIcon banIcon = null;

    public UserListRenderer() {
        userNameLabel = new JLabel();

        adminLabel = new JLabel("");
        adminLabel.setBorder(new EmptyBorder(0, 0, 0, 5));
        statusLabel = new JLabel("");

        // load image icons
        try {
            BufferedImage onlineImage = ImageIO.read(new File("src/pavelmaca/chat/client/resources/online.png"));
            onlineIcon = new ImageIcon(onlineImage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedImage offImage = ImageIO.read(new File("src/pavelmaca/chat/client/resources/offline.png"));
            offlineIcon = new ImageIcon(offImage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedImage adminImage = ImageIO.read(new File("src/pavelmaca/chat/client/resources/admin.png"));
            admineIcon = new ImageIcon(adminImage.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedImage banImage = ImageIO.read(new File("src/pavelmaca/chat/client/resources/ban.png"));
            banIcon = new ImageIcon(banImage.getScaledInstance(26, 26, Image.SCALE_SMOOTH));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // setup GUI
        adminLabel.setIcon(admineIcon);
        adminLabel.setVisible(false);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.setBorder(new EmptyBorder(5, 10, 5, 0)); // padding

        panel.add(userNameLabel);
        panel.add(Box.createHorizontalGlue());
        panel.add(adminLabel);
        panel.add(statusLabel);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends UserInfo> list, UserInfo user, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        userNameLabel.setText(user.getName());
        userNameLabel.setBackground(list.getBackground());
        userNameLabel.setForeground(list.getForeground());

        adminLabel.setVisible(user.getRank().equals(UserInfo.Rank.OWNER));

        switch (user.getStatus()) {
            case ONLINE:
                statusLabel.setIcon(onlineIcon);
                break;
            case OFFLINE:
                statusLabel.setIcon(offlineIcon);
                break;
            case BANNED:
                statusLabel.setIcon(banIcon);
                break;
        }

        return panel;
    }
}