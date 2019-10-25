package pao.core.services;

import pao.core.exceptions.EditorIOException;
import pao.core.io.MapFileIO;
import pao.core.io.porygon.PorygonMapFileHandler;
import pao.core.models.Map;
import pao.core.models.MapCell;
import pao.core.models.events.MapEvent;
import pao.core.models.mapobjects.MapObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Mark "Wilds" van der Wal
 * @since 31-1-2018
 * <p>
 * Keeps track of the currently opened map.
 * <p>
 * todo: Try to split this service up in MapService and MapIOService
 */
public class MapService {

    private final static Logger LOGGER = LogManager.getLogger(MapService.class);

    private final BehaviorSubject<MapEvent> mapChanged;
    private final MapFileIO defaultMapFileHandler;

    private Path currentMapPath;
    private Map currentMap;

    public MapService() {
        mapChanged = BehaviorSubject.create();
        defaultMapFileHandler = new PorygonMapFileHandler();
    }

    public Subscription onMapChanged(Action1<MapEvent> action) {
        return mapChanged.subscribe(action);
    }

    private void notifyObservers(MapEvent.Type type) {
        LOGGER.debug(String.format("Map %s event", type));

        MapEvent event = new MapEvent(type);
        mapChanged.onNext(event);
    }

    public void closeMap() {
        currentMap = null;
        currentMapPath = null;
        notifyObservers(MapEvent.Type.Closed);
    }

    public void newMap(String name, int width, int height) {
        currentMap = new Map(name, 1, width, height);
        currentMapPath = null;
        notifyObservers(MapEvent.Type.New);
    }

    public void loadMap(Path path) throws EditorIOException {
        Map loadedMap = defaultMapFileHandler.read(path);

        currentMap = loadedMap;
        currentMapPath = path;
        notifyObservers(MapEvent.Type.Opened);
    }

    public void saveMap(Path path) throws EditorIOException {
        defaultMapFileHandler.write(path, currentMap);
        currentMapPath = path;
        notifyObservers(MapEvent.Type.Saved);
    }

    public int getCollisionId(int x, int y) {
        byte[] tiles = currentMap.getCollisionData();
        int index = y * currentMap.getWidth() + x;

        return tiles[index];
    }

    public int setCollisionId(int x, int y, int tile) {
        byte[] tiles = currentMap.getCollisionData();
        int index = y * currentMap.getWidth() + x;
        int oldTile = tiles[index];

        tiles[index] = (byte) tile;

        return oldTile;
    }

    public MapObject setTile(int x, int y, int z, MapObject tile) {
        MapCell cell = currentMap.getMapCell(x, y, z);
        MapObject old = cell.getCellObject();
        cell.setCellObject(tile);

        return old;
    }

    public void notifyChange() {
        notifyObservers(MapEvent.Type.Changed);
    }

    public MapObject getTile(int x, int y, int z) {
        MapCell cell = currentMap.getMapCell(x, y, z);
        return cell == null ? null : cell.getCellObject();
    }

    public MapFileIO getMapIOHandler() {
        return defaultMapFileHandler;
    }

    public boolean hasMapPath() {
        return currentMapPath != null && Files.exists(currentMapPath);
    }

    public Path getMapPath() {
        return currentMapPath;
    }

    public boolean hasMap() {
        return currentMap != null;
    }

    public Map getMap() {
        return currentMap;
    }
}
