package pavelmaca.chat.client.model;

import java.io.Serializable;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class User implements Serializable {
    protected String name;
    protected String password;

    public User(String name, String password) {
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
}
