package pavelmaca.chat.server.entity;

import pavelmaca.chat.share.model.UserInfo;

/**
 * Represent table row for user
 *
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class User {
    protected String name;
    protected String password;
    private int id;

    public User(int id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public boolean comparePasswords(String password) {
        return this.password.equals(password);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public int getId() {
        return id;
    }

    public UserInfo getInfoModel() {
        return new UserInfo(id, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return id == user.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
