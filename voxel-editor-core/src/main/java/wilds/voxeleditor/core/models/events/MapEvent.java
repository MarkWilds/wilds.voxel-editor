package wilds.voxeleditor.core.models.events;

/**
 * @author Mark van der Wal
 * @since 15/02/18
 */
public final class MapEvent {

    private Type type;

    public MapEvent(Type type) {
        this.type = type;
    }

    /**
     * Gets the type of event
     *
     * @return type of event
     */
    public Type getType() {
        return type;
    }

    public boolean isMapNew() {
        return type == Type.New;
    }

    public boolean isMapSaved() {
        return type == Type.Saved;
    }

    public boolean isMapOpened() {
        return type == Type.Opened;
    }

    public boolean isMapChanged() {
        return type == Type.Changed;
    }

    public boolean isMapClosed() {
        return type == Type.Closed;
    }

    /**
     * If the map is created or opened from disk it is fresh!
     *
     * @return true if map is fresh
     */
    public boolean isMapFresh() {
        return type == Type.New || type == Type.Opened;
    }

    /**
     * The map is only dirty when a new map is created or
     * when a change has happened to the map data
     *
     * @return true if map was changed or newly created
     */
    public boolean isMapDirty() {
        return type == Type.New || type == Type.Changed;
    }

    public enum Type {
        New,
        Saved,
        Opened,
        Closed,
        Changed
    }
}