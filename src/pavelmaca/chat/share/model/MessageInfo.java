package pavelmaca.chat.share.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class MessageInfo implements Serializable {
    private String text;
    private int authorId;
    private String authorName;
    private Date timestamp;

    public MessageInfo(String text, int authorId, Date timestamp, String authorName) {
        this.text = text;
        this.authorId = authorId;
        this.timestamp = timestamp;
        this.authorName = authorName;
    }

    public String getText() {
        return text;
    }

    public int getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
