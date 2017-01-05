package pavelmaca.chat.client.gui.renderer;

import pavelmaca.chat.server.entity.User;
import pavelmaca.chat.share.model.MessageInfo;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.SimpleDateFormat;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class MessageListRenderer implements ListCellRenderer<MessageInfo> {

    protected User currentUser;

    private JPanel panel;
    private JTextArea message;
    private JLabel author;
    private JLabel timestamp;
    SimpleDateFormat dateFormat;


    public MessageListRenderer(User currentUser) {
        this.currentUser = currentUser;

        dateFormat = new SimpleDateFormat("dd.MM. HH:mm:ss");

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


     /*   JPanel topPane = new JPanel();
        topPane.setLayout(new BoxLayout(topPane, BoxLayout.Y_AXIS));

        topPane.add(author);
        topPane.add(Box.createVerticalGlue());

        panel.add(topPane, BorderLayout.PAGE_START);*/

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