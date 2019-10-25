package pao.core.models.mapobjects.tiles;

import pao.core.models.Tileset;
import pao.core.models.mapobjects.MapObject;
import pao.core.models.mapobjects.operations.IMapObjectOperation;

/**
 * @author Mark "Wilds" van der Wal
 * @since 7-3-2018
 */
public class CubeTile extends MapObject {

    public static final short EMPTY_TILE_ID = -1;

    private Tileset[] tilesets;
    private short[] tileIds;

    public CubeTile() {
        CubeTileSide[] tileSides = CubeTileSide.values();
        int sides = tileSides.length;
        tilesets = new Tileset[sides];
        tileIds = new short[sides];

        for (int i = 0; i < sides; i++) {
            clearTile(tileSides[i]);
        }
    }

    public void setData(CubeTile tile) {
        System.arraycopy(tile.tilesets, 0, tilesets, 0, tilesets.length);
        System.arraycopy(tile.tileIds, 0, tileIds, 0, tileIds.length);
    }

    public void setTile(CubeTileSide side, Tileset tileset, short id) {
        int ordinal = side.ordinal();
        tilesets[ordinal] = tileset;
        tileIds[ordinal] = id;
    }

    public void fillCube(Tileset tileset, short id) {
        setTile(CubeTileSide.Front, tileset, id);
        setTile(CubeTileSide.Back, tileset, id);
        setTile(CubeTileSide.Left, tileset, id);
        setTile(CubeTileSide.Right, tileset, id);
        setTile(CubeTileSide.Bottom, tileset, id);
        setTile(CubeTileSide.Top, tileset, id);
    }

    public short getTileId(CubeTileSide side) {
        return tileIds[side.ordinal()];
    }

    public void clearTile(CubeTileSide side) {
        setTile(side, null, EMPTY_TILE_ID);
    }

    @Override
    public void accept(IMapObjectOperation operation) {
        operation.visit(this);
    }
}
