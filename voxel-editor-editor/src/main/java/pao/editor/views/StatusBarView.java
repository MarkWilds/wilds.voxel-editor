package pao.editor.views;

import com.alee.extended.statusbar.WebMemoryBar;
import com.alee.extended.statusbar.WebStatusBar;
import com.alee.laf.label.WebLabel;

/**
 * @author Mark "Wilds" van der Wal
 * @since 5-1-2018
 */
public class StatusBarView extends WebStatusBar {

    private WebLabel messageLabel;

    public void setMessage(final String message) {
        messageLabel.setText(message);
    }

    public StatusBarView() {
        messageLabel = new WebLabel();
        add(messageLabel);

        addToEnd(new WebMemoryBar());
    }
}
