package pavelmaca.chat.client.model;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public class User {
    protected String name;

    public User(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
