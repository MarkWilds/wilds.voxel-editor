package pao.core.logic.tilecollectors;

import wilds.rune.geometry.Plane;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * @author Mark "Wilds" van der Wal
 * @since 23-3-2018
 */
public abstract class BaseMapTileCollector implements TileCollector {

    protected IntSet tileIndices;
    protected Plane workingPlane;

    public BaseMapTileCollector(int cacheSize) {
        this.tileIndices = new IntOpenHashSet(cacheSize);
    }

    @Override
    public void setup(Plane plane) {
        this.workingPlane = plane;
    }

    @Override
    public IntSet getTileIndices() {
        return tileIndices;
    }
}
