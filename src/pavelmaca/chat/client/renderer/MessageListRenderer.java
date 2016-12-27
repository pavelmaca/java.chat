package pavelmaca.chat.client.renderer;

import pavelmaca.chat.client.model.Message;
import pavelmaca.chat.client.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class MessageListRenderer extends JTextArea implements ListCellRenderer<Message> {

    protected User currentUser;

    public MessageListRenderer(User currentUser) {
        this.currentUser = currentUser;
        setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Message> list, Message message, int index,
                                                  boolean isSelected, boolean cellHasFocus) {

       /* String code = roomgetCode();
        ImageIcon imageIcon = new ImageIcon(getClass().getResource("/images/" + code + ".png"));

        setIcon(imageIcon);*/
        setText(message.getAuthor().getName() + ": " + message.getContent());
        setBorder(new EmptyBorder(5, 10, 5, 10)); // padding

        setLineWrap(true);
        setWrapStyleWord(true);

        if (message.getAuthor().equals(currentUser)) {
            Font bold = new Font(getFont().getFontName(), Font.BOLD, getFont().getSize());
            setFont(bold);
           // setHorizontalAlignment(LEFT);
        } /*else {
            setHorizontalAlignment(RIGHT);
        }*/


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