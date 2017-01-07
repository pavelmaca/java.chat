package pavelmaca.chat.share.model;

import pavelmaca.chat.server.entity.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class RoomStatus implements Serializable {

    private RoomInfo roomInfo;
    private TreeSet<UserInfo> userList;
    private List<MessageInfo> messages = new ArrayList<>();
    private int ownerId;

    public RoomStatus(RoomInfo roomInfo, TreeSet<UserInfo> userList) {
        this.roomInfo = roomInfo;
        this.userList = userList;
    }

    public void addMessage(MessageInfo messageInfo) {
        messages.add(messageInfo);
    }

    public RoomInfo getRoomInfo() {
        return roomInfo;
    }

    public TreeSet<UserInfo> getUserList() {
        return userList;
    }

    public int countConnectedUsers() {
        return (int) userList.stream().filter(userInfo -> userInfo.getStatus().equals(UserInfo.Status.ONLINE)).count();
    }

    public List<MessageInfo> getMessages() {
        return messages;
    }

    // ---- Modify status

    public void userLeave(int userId) {
        // iterator to safely remove from array
        Iterator<UserInfo> i = userList.iterator();
        while (i.hasNext()) {
            UserInfo userInfo = i.next();
            if (userInfo.getId() == userId) {
                i.remove();
                break;
            }
        }
    }

    public void userJoined(UserInfo userInfo) {
        userInfo.setStatus(UserInfo.Status.ONLINE);
        userList.add(userInfo);
    }

    public void userConnected(UserInfo userInfo) {
        for (UserInfo info : userList) {
            if (info.equals(userInfo)) {
                info.setStatus(UserInfo.Status.ONLINE);
                return;
            }
        }

        // new user
        userJoined(userInfo);
    }

    public void userDisconnected(int userId) {
        for (UserInfo info : userList) {
            if (info.getId() == userId) {
                info.setStatus(UserInfo.Status.OFFLINE);
                break;
            }
        }
    }

    public UserInfo getUserInfo(UserInfo currentUser) {
        for (UserInfo userInfo : userList) {
            if (userInfo.equals(currentUser)) {
                return userInfo;
            }
        }
        return null;
    }

    public void userBan(UserInfo userInfo) {
        userInfo.setStatus(UserInfo.Status.BANNED);
        userList.add(userInfo);
    }

    public void userRemoveBan(int userId) {
        userLeave(userId); // just remove from list
    }

    /**
     * Count all users connected to room, except banned ones
     *
     * @return
     */
    public int countUsers() {
        return (int) userList.stream().filter(userInfo -> userInfo.getStatus() != UserInfo.Status.BANNED).count();
    }
}
