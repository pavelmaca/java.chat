package pavelmaca.chat.client.model;

import java.util.Date;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Message {
    protected Date timestamp;
    protected String content;
    protected User author;

    public Message(String content, User author) {
        this.content = content;
        this.author = author;
        timestamp = new Date();
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getContent() {
        return content;
    }

    public User getAuthor() {
        return author;
    }
}
