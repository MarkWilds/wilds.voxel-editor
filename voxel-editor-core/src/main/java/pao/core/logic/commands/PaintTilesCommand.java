package pao.core.logic.commands;

import pao.core.models.Map;
import pao.core.models.MapCell;
import pao.core.models.mapobjects.MapObject;
import pao.core.models.mapobjects.operations.MapObjectIdentifierOperation;
import pao.core.models.mapobjects.tiles.CubeTile;
import pao.core.models.mapobjects.tiles.CubeTileSide;
import pao.core.services.MapService;

/**
 * @author Mark van der Wal
 * @since 11/04/18
 */
public class PaintTilesCommand implements ICommand {

    private MapService mapService;
    private MapObjectIdentifierOperation mapObjectIdentifier;
    private int[] tileIndices;
    private MapObject[] oldMapObjects;
    private int tileId;
    private CubeTileSide side;

    public PaintTilesCommand(int[] indices, int tileId, CubeTileSide side, MapService mapService) {
        this.mapService = mapService;
        this.tileId = tileId;
        this.side = side;
        this.tileIndices = indices;

        oldMapObjects = new MapObject[indices.length];
        mapObjectIdentifier = new MapObjectIdentifierOperation();
    }

    @Override
    public String getDescription() {
        return "Set tiles!";
    }

    @Override
    public boolean execute() {
        if (tileIndices.length <= 0) {
            return false;
        }

        Map map = mapService.getMap();
        MapCell[] mapCells = map.getMapData();

        for (int i = 0; i < tileIndices.length; i++) {
            int tileIndex = tileIndices[i];
            MapCell cell = mapCells[tileIndex];

            oldMapObjects[i] = cell.getCellObject();
            if (!cell.isEmpty()) {
                oldMapObjects[i].accept(mapObjectIdentifier);

                if (mapObjectIdentifier.isCubeTile()) {
                    CubeTile tile = mapObjectIdentifier.getCubeTile();

                    CubeTile newTile = new CubeTile();
                    newTile.setData(tile);
                    newTile.setTile(side, null, (short) tileId);

                    cell.setCellObject(newTile);
                }
            }
        }

        return true;
    }

    @Override
    public void undo() {
        Map map = mapService.getMap();
        MapCell[] mapCells = map.getMapData();

        for (int i = 0; i < tileIndices.length; i++) {
            int tileIndex = tileIndices[i];
            MapCell cell = mapCells[tileIndex];

            cell.setCellObject(oldMapObjects[i]);
        }
    }
}
