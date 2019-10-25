package pao.core.models.mapobjects.operations;

import pao.core.models.mapobjects.tiles.CubeTile;

/**
 * @author Mark "Wilds" van der Wal
 * @since 8-3-2018
 */
public interface IMapObjectOperation {

    public void visit(CubeTile tile);
}
