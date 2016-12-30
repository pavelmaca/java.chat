package pavelmaca.chat.server.repository;

import java.sql.Connection;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
abstract public class Repository {
    Connection connection;

    public Repository(Connection connection) {
        this.connection = connection;
    }
}
