package pavelmaca.chat.server.entity;

import pavelmaca.chat.share.model.MessageInfo;

import java.util.Date;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class Message {
    protected Date timestamp;
    protected String content;
    protected User author;
    protected int roomId;

    public Message(int id, String content, User author, int roomId) {
        this.content = content;
        this.author = author;
        this.roomId = roomId;
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

    public MessageInfo getInfoModel(){
        return new MessageInfo(content, author.getId(), timestamp, author.getName(), roomId);
    }
}
