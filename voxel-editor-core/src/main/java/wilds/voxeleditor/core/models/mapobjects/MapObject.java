package wilds.voxeleditor.core.models.mapobjects;

import wilds.voxeleditor.core.models.mapobjects.operations.IMapObjectOperation;

import wilds.rune.geometry.Box;
import wilds.rune.geometry.Ray;
import wilds.rune.util.Maths;

/**
 * @author Mark "Wilds" van der Wal
 * @since 19-2-2018
 */
public abstract class MapObject {

    protected final Box bounds;

    public MapObject() {
        bounds = new Box();
    }

    public boolean isHit(final Ray ray) {
        return Maths.rayIntersectsBox(ray, bounds);
    }

    public Box getBounds() {
        return bounds;
    }

    public abstract void accept(IMapObjectOperation operation);
}
