package pao.core.models.mapobjects.operations;

import pao.core.models.mapobjects.tiles.CubeTile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mark "Wilds" van der Wal
 * @since 8-3-2018
 */
public abstract class MapObjectOperationAdapter implements IMapObjectOperation {

    private static final Logger LOGGER = LogManager.getLogger(MapObjectOperationAdapter.class);

    @Override
    public void visit(CubeTile tile) {
        LOGGER.warn("Not implemented visit for CubeTile");
    }
}
