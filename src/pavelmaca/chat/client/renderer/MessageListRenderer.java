package pavelmaca.chat.client.renderer;

import pavelmaca.chat.server.entity.Message;
import pavelmaca.chat.server.entity.User;
import pavelmaca.chat.share.model.MessageInfo;

import javax.swing.*;
import java.awt.*;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class MessageListRenderer implements ListCellRenderer<MessageInfo> {

    protected User currentUser;

    private JPanel panel;
    private JLabel l;
    private JTextArea message;
    private JLabel author;


    public MessageListRenderer(User currentUser) {
        this.currentUser = currentUser;

        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // text
        message = new JTextArea();
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        panel.add(message, BorderLayout.CENTER);

        JPanel topPane = new JPanel();
        topPane.setLayout(new BoxLayout(topPane, BoxLayout.Y_AXIS));

        author = new JLabel("");
        author.setOpaque(true);
        topPane.add(author);
        topPane.add(Box.createVerticalGlue());

        panel.add(topPane, BorderLayout.LINE_START);

    }

    @Override
    public Component getListCellRendererComponent(JList<? extends MessageInfo> list, MessageInfo message, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        this.message.setText(message.getText());
        int width = list.getWidth();
        // this is just to lure the ta's internal sizing mechanism into action
        if (width > 0)
            this.message.setSize(width, Short.MAX_VALUE);


        this.author.setText(message.getAuthorName()+":");
        return panel;

    }
}