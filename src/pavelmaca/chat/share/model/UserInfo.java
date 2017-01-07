package pavelmaca.chat.share.model;

import java.io.Serializable;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class UserInfo implements Serializable, Comparable<UserInfo> {
    private int id;
    private String name;
    private Rank rank = Rank.MEMBER;
    private Status status;

    public UserInfo(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public Rank getRank() {
        return rank;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserInfo userInfo = (UserInfo) o;

        return id == userInfo.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public int compareTo(UserInfo o) {
        int rankCompare = getRank().compareTo(o.getRank());
        if (rankCompare != 0) {
            return rankCompare;
        }

        int statusCompare = getStatus().compareTo(o.getStatus());
        return statusCompare != 0 ? statusCompare : getName().compareTo(o.getName());

    }

    public enum Rank {
        OWNER,
        MEMBER
    }

    public enum Status {
        ONLINE,
        OFFLINE,
        BANNED,
    }
}
