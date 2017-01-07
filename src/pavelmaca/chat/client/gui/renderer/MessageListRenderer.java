package pavelmaca.chat.client.gui.renderer;

import pavelmaca.chat.server.entity.User;
import pavelmaca.chat.share.model.MessageInfo;
import pavelmaca.chat.share.model.UserInfo;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.SimpleDateFormat;

/**
 * Render message list items
 *
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class MessageListRenderer implements ListCellRenderer<MessageInfo> {

    private UserInfo currentUser;

    private JPanel panel;
    private JTextArea message;
    private JLabel author;
    private JLabel timestamp;
    private final SimpleDateFormat dateFormat;


    public MessageListRenderer(UserInfo currentUser) {
        this.currentUser = currentUser;

        // date format for the message timestamp
        dateFormat = new SimpleDateFormat("dd.MM. HH:mm:ss");

        // setup GUI
        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        author = new JLabel("");
        author.setBackground(Color.WHITE);
        author.setOpaque(true);


        timestamp = new JLabel("");
        timestamp.setBackground(Color.WHITE);
        timestamp.setOpaque(true);

        Font smallFont = new Font(author.getFont().getFontName(), Font.PLAIN, 10);
        timestamp.setFont(smallFont);
        author.setFont(smallFont);

        Component horizontalGlue = Box.createHorizontalGlue();
        horizontalGlue.setBackground(Color.WHITE);

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
        header.add(author);
        header.add(horizontalGlue);
        header.add(timestamp);
        header.setOpaque(true);
        header.setBackground(Color.WHITE);
        panel.add(header, BorderLayout.PAGE_START);

        // text
        message = new JTextArea();
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        message.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        panel.add(message, BorderLayout.CENTER);

        panel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(0, 5, 0, 5)
        ));
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends MessageInfo> list, MessageInfo message, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        this.message.setText(message.getText());
        this.timestamp.setText(dateFormat.format(message.getTimestamp()));
        int width = list.getWidth();

        // this is just to lure the ta's internal sizing mechanism into action
        if (width > 0)
            this.message.setSize(width, Short.MAX_VALUE);


        this.author.setText(message.getAuthorName());
        return panel;
    }
}