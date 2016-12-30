package pavelmaca.chat;

import pavelmaca.chat.commands.Command;
import pavelmaca.chat.commands.Status;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public abstract class Protocol {
    private States state = null;

    protected ObjectOutputStream outputStream = null;
    protected ObjectInputStream inputStream = null;


    enum States {
        CON_NEW,
        CON_OPEN,
        CON_AUTHORIZATION,
        CON_CLOSE,
        ROOM_CREATE,
        ROOM_JOIN,
        ROOM_LEAVE,
    }

}
