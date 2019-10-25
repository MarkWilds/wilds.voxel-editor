package pao.core.logic.commands;

import pao.core.models.Map;
import pao.core.models.MapCell;
import pao.core.models.mapobjects.MapObject;
import pao.core.services.MapService;

/**
 * @author Mark van der Wal
 * @since 05/04/18
 */
public class EraseTilesCommand implements ICommand {

    private MapService mapService;
    private int[] tileIndices;
    private MapObject[] oldMapObjects;

    public EraseTilesCommand(int[] indices, MapService mapService) {
        this.mapService = mapService;
        this.tileIndices = indices;

        oldMapObjects = new MapObject[indices.length];
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
            cell.setCellObject(null);
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

