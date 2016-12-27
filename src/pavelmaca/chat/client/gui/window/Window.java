package pavelmaca.chat.client.gui.window;

import javax.swing.*;

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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.PAGE_AXIS));
        frame.setResizable(false);

        setupComponents();


        frame.pack();
        frame.setLocationRelativeTo(null); // run window in middle of screen
        frame.setVisible(true);
    }

    protected abstract void setupComponents();
}
