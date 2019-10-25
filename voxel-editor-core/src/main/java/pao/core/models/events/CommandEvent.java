package pao.core.models.events;

/**
 * @author Mark van der Wal
 * @since 15/02/18
 */
public class CommandEvent {

    public enum Type {
        Execute,
        Undo,
        Redo,
        Cleared
    }

    private Type type;
    private String description;
    private int undoCommands;
    private int redoCommands;

    public CommandEvent(Type type, String description, int undoCommands, int redoCommands) {
        this.type = type;
        this.description = description;
        this.undoCommands = undoCommands;
        this.redoCommands = redoCommands;
    }

    /**
     * Gets the type of event
     *
     * @return type of event
     */
    public Type getType() {
        return type;
    }

    public String getCommandDescription() {
        return description;
    }

    public boolean hasUndoCommands() {
        return undoCommands > 0;
    }

    public boolean hasRedoCommands() {
        return redoCommands > 0;
    }
}
