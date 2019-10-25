package pao.core.models.mapobjects;

import pao.core.models.mapobjects.operations.IMapObjectOperation;

import com.wildrune.rune.geometry.Box;
import com.wildrune.rune.geometry.Ray;
import com.wildrune.rune.util.Maths;

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
