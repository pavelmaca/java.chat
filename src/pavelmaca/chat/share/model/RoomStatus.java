package pavelmaca.chat.share.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class RoomStatus implements Serializable {

    private RoomInfo roomInfo;
    private ArrayList<UserInfo> activeUsers;
    private ArrayList<MessageInfo> messages = new ArrayList<>();

    public RoomStatus(RoomInfo roomInfo, ArrayList<UserInfo> activeUsers) {
        this.roomInfo = roomInfo;
        this.activeUsers = activeUsers;
    }

    public void addMessage(MessageInfo messageInfo) {
        messages.add(messageInfo);
    }

    public RoomInfo getRoomInfo() {
        return roomInfo;
    }

    public ArrayList<UserInfo> getActiveUsers() {
        return activeUsers;
    }

    public ArrayList<MessageInfo> getMessages() {
        return messages;
    }
}
