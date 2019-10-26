package wilds.voxeleditor.core.models.mapobjects.operations;

import wilds.voxeleditor.core.models.mapobjects.tiles.CubeTile;

/**
 * @author Mark "Wilds" van der Wal
 * @since 22-3-2018
 */
public class MapObjectIdentifierOperation implements IMapObjectOperation {

    private CubeTile tile;

    public boolean isCubeTile() {
        return tile != null;
    }

    public CubeTile getCubeTile() {
        return tile;
    }

    @Override
    public void visit(CubeTile tile) {
        this.tile = tile;
    }
}
