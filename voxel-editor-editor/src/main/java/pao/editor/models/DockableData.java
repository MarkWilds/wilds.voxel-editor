package pao.editor.models;

import com.alee.api.data.*;

import javax.swing.*;

/**
 * @author Mark van der Wal
 * @since 14/01/18
 *
 * POJO that holds dockable information needed by WebLaf WebDockableFrames
 */
public final class DockableData {

    private String id;
    private String title;
    private CompassDirection direction;
    private JComponent component;

    public DockableData(String _id, JComponent component) {
        this(_id, _id, CompassDirection.west, component);
    }

    public DockableData(String _id, String _title, CompassDirection _direction, JComponent _component) {
        id = _id;
        title = _title;
        direction = _direction;
        component = _component;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public CompassDirection getDirection() {
        return direction;
    }

    public JComponent getComponent() {
        return component;
    }
}
