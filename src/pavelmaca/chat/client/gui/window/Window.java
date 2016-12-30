package pavelmaca.chat.client.gui.window;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Function;

/**
 * @author Pavel Máca <maca.pavel@gmail.com>
 */
public abstract class Window {
    /**
     * Window frame
     */
    protected JFrame frame;


    public Window(String title) {
        frame = new JFrame(title);

        // terminate application after closing this window
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.PAGE_AXIS));
        frame.setResizable(false);

        setupComponents();


        frame.pack();
        frame.setLocationRelativeTo(null); // run window in middle of screen
        frame.setVisible(true);
    }

    public void onWindowClose(Function<Void, Void> callback){
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                callback.apply(null);
            }
        });
    }

    public void close() {
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }


    protected abstract void setupComponents();

    @FunctionalInterface
    public interface Function2<One, Two, Return> {
        Return apply(One one, Two two);
    }

    @FunctionalInterface
    public interface Function3<One, Two, Three, Return> {
        Return apply(One one, Two two, Three three);
    }
}
