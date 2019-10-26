package wilds.voxeleditor.core.logic.commands;

import wilds.voxeleditor.core.models.Map;
import wilds.voxeleditor.core.models.MapCell;
import wilds.voxeleditor.core.models.mapobjects.MapObject;
import wilds.voxeleditor.core.models.mapobjects.operations.MapObjectIdentifierOperation;
import wilds.voxeleditor.core.models.mapobjects.tiles.CubeTile;
import wilds.voxeleditor.core.services.MapService;

/**
 * @author Mark van der Wal
 * @since 09/03/18
 */
public class PlaceTilesCommand implements ICommand {
    private MapService mapService;
    private MapObjectIdentifierOperation mapObjectIdentifier;
    private int[] tileIndices;
    private MapObject[] oldMapObjects;
    private int tileId;

    public PlaceTilesCommand(int[] indices, int tileId, MapService mapService) {
        this.mapService = mapService;
        this.tileId = tileId;
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

            CubeTile tile = new CubeTile();

            oldMapObjects[i] = cell.getCellObject();
            if (!cell.isEmpty()) {
                oldMapObjects[i].accept(mapObjectIdentifier);

                if (mapObjectIdentifier.isCubeTile()) {
                    tile.setData(mapObjectIdentifier.getCubeTile());
                }
            }

            tile.fillCube(null, (short) tileId);

            cell.setCellObject(tile);
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
