package wilds.voxeleditor.editor.views;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;

/**
 * @author Mark van der Wal
 * @since 16/01/18
 */
public abstract class AbstractWindowView extends WindowAdapter {

    private Consumer<WindowEvent> windowListener;

    @Override
    public void windowOpened(WindowEvent e) {
        dispatchEvent(e);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        dispatchEvent(e);
    }

    @Override
    public void windowClosed(WindowEvent e) {
        dispatchEvent(e);
    }

    @Override
    public void windowIconified(WindowEvent e) {
        dispatchEvent(e);
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        dispatchEvent(e);
    }

    @Override
    public void windowActivated(WindowEvent e) {
        dispatchEvent(e);
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        dispatchEvent(e);
    }

    @Override
    public void windowStateChanged(WindowEvent e) {
        dispatchEvent(e);
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {
        dispatchEvent(e);
    }

    @Override
    public void windowLostFocus(WindowEvent e) {
        dispatchEvent(e);
    }

    public void setWindowListener(Consumer<WindowEvent> listener) {
        windowListener = listener;
    }

    private void dispatchEvent(WindowEvent event) {
        if (windowListener != null) {
            windowListener.accept(event);
        }
    }
}
