package pavelmaca.chat.share.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class RoomStatus implements Serializable {

    private RoomInfo roomInfo;
    private List<UserInfo> activeUsers;
    private List<UserInfo> joinedUsers;
    private List<MessageInfo> messages = new ArrayList<>();
    private int ownerId;

    public RoomStatus(RoomInfo roomInfo, List<UserInfo> joinedUsers, List<UserInfo> activeUsers, int ownerId) {
        this.roomInfo = roomInfo;
        this.joinedUsers = joinedUsers;
        this.activeUsers = activeUsers;
        this.ownerId = ownerId;
    }

    public void addMessage(MessageInfo messageInfo) {
        messages.add(messageInfo);
    }

    public RoomInfo getRoomInfo() {
        return roomInfo;
    }

    public List<UserInfo> getActiveUsers() {
        return activeUsers;
    }

    public List<MessageInfo> getMessages() {
        return messages;
    }

    public List<UserInfo> getJoinedUsers() {
        return joinedUsers;
    }

    // ---- Modify status

    public void userDisconnected(int userId) {
        // iterator to safely remove from array
        Iterator<UserInfo> i = getActiveUsers().iterator();
        while (i.hasNext()) {
            UserInfo userInfo = i.next();
            if (userInfo.getId() == userId) {
                i.remove();
                break;
            }
        }
    }

    public void userConnected(UserInfo userInfo) {
        getActiveUsers().add(userInfo);
    }

    public int getOwnerId() {
        return ownerId;
    }
}
