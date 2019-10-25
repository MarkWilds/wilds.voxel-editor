package pao.core.models;

import pao.core.models.mapobjects.MapObject;

/**
 * @author Mark "Wilds" van der Wal
 * @since 20-2-2018
 */
public class MapCell {

    private MapObject cellObject;

    public MapObject getCellObject() {
        return cellObject;
    }

    public void setCellObject(MapObject mapObject) {
        cellObject = mapObject;
    }

    public boolean isEmpty() {
        return cellObject == null;
    }
}
