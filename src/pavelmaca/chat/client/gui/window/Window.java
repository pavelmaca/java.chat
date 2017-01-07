package pavelmaca.chat.client.gui.window;

import pavelmaca.chat.share.Lambdas;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Basic window
 *
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

    /**
     * @param callback Called after the window event is triggered
     */
    public void addWindowsCloseListener(Lambdas.Function0 callback) {
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                callback.apply();
            }
        });
    }

    /**
     * Close current windows by triggering close event
     */
    public void close() {
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }


    /**
     * Create GUI
     */
    protected abstract void setupComponents();
}
