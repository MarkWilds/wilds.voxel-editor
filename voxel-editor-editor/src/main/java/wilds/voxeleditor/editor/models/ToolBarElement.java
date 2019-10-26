package wilds.voxeleditor.editor.models;

import com.alee.managers.hotkey.HotkeyData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.event.KeyEvent;

/**
 * @author Mark "Wilds" van der Wal
 * @since 12-1-2018
 */
public enum ToolBarElement {
    Separator("Separator"),

    New("New map", ToolBarIcon.New, new HotkeyData(true, false, false, KeyEvent.VK_N)),
    Open("Open map", ToolBarIcon.Open, new HotkeyData(true, false, false, KeyEvent.VK_O)),
    Save("Save map", ToolBarIcon.Save, new HotkeyData(true, false, false, KeyEvent.VK_S)),
    SaveAs("Save map as...", ToolBarIcon.SaveAs, new HotkeyData(true, false, true, KeyEvent.VK_S)),

    Undo("Undo action", ToolBarIcon.Undo, new HotkeyData(true, false, false, KeyEvent.VK_Z)),
    Redo("Redo action", ToolBarIcon.Redo, new HotkeyData(true, false, true, KeyEvent.VK_Z)),

    Copy("Copy selection"), // currently not used!
    Cut("Cut selection"),  // currently not used!
    Paste("Paste selection"),  // currently not used!

    Edit("Edit map properties", ToolBarIcon.Edit),
    Settings("Open settings window", ToolBarIcon.Settings, ToolBarElementPosition.End),
    ToolWindow("Tool windows", ToolBarIcon.ToolWindow, ToolBarElementPosition.End),

    TopDownView("Top down camera", ToolBarIcon.TopDownView),
    GameView("Game camera", ToolBarIcon.GameView),
    FreeView("Free camera", ToolBarIcon.FreeView),

    Cube("Cube paint tool", ToolBarIcon.Cube),
    Quad("Quad paint tool", ToolBarIcon.Quad),
    Pencil("Cube paint tool", ToolBarIcon.Pencil),
    Rect("Quad paint tool", ToolBarIcon.Rect),
    Bucket("Quad paint tool", ToolBarIcon.Bucket),
    Eraser("Eraser tool", ToolBarIcon.Eraser),
    Grid("Map grid", ToolBarIcon.Grid),

    ClearLog("Clear log", ToolBarIcon.Bin),

    NewTileset("New tileset", ToolBarIcon.New, ToolBarElementPosition.End),
    EditTileset("Edit tileset", ToolBarIcon.Edit, ToolBarElementPosition.End),
    RemoveTileset("Remove tileset", ToolBarIcon.Bin, ToolBarElementPosition.End);

    private static final Logger LOGGER = LogManager.getLogger(ToolBarElement.class);

    private String description;
    private ToolBarIcon icon;
    private ToolBarElementPosition toolBarElementPosition;
    private HotkeyData hotkeyData;

    ToolBarElement(String description) {
        this(description, null);
    }

    ToolBarElement(String description, ToolBarIcon toolBarIcon) {
        this(description, toolBarIcon, ToolBarElementPosition.Start);
    }

    ToolBarElement(String description, ToolBarIcon toolBarIcon,
                   ToolBarElementPosition toolBarElementPosition) {
        this(description, toolBarIcon, toolBarElementPosition, null);
    }

    ToolBarElement(String description, ToolBarIcon toolBarIcon,
                   HotkeyData hotkey) {
        this(description, toolBarIcon, ToolBarElementPosition.Start, hotkey);
    }

    ToolBarElement(String description, ToolBarIcon icon,
                   ToolBarElementPosition position, HotkeyData hotkey) {
        this.description = description;
        this.icon = icon;
        this.toolBarElementPosition = position;
        this.hotkeyData = hotkey;
    }

    public String getDescription() {
        return description;
    }

    public ToolBarIcon getToolBarIcon() {
        return icon;
    }

    public ToolBarElementPosition getToolBarElementPosition() {
        return toolBarElementPosition;
    }

    public HotkeyData getHotkeyData() {
        return hotkeyData;
    }

}
