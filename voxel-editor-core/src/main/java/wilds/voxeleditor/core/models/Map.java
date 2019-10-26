package wilds.voxeleditor.core.models;

import wilds.rune.geometry.Box;

import org.joml.Vector3f;
import org.joml.Vector3i;

/**
 * @author Mark "Wilds" van der Wal
 * @since 31-1-2018
 */
public class Map {

    public final static String DEFAULT_MAP_NAME = "untitled";
    public final static int MAP_STEP = 8;
    public final static int MAX_MAP_DIMENSION = 128;
    public final static int MAX_MAP_DEPTH = 8;

    private final static int VERSION = 1;

    private String name;
    private int version;
    private int width;
    private int height;
    private int tileSize;
    private byte collisionData[];
    private MapCell mapData[];
    private Box bounds;

    public Map(String mapName, int mapVersion, int w, int h) {
        version = mapVersion;
        name = mapName;
        width = w;
        height = h;
        tileSize = 32;
        collisionData = new byte[width * height];
        mapData = new MapCell[width * height * MAX_MAP_DEPTH];
        bounds = new Box();
        bounds.grow(0, 0, 0);
        bounds.grow(width * tileSize, MAX_MAP_DEPTH * tileSize, height * tileSize);

        for (int i = 0; i < width * height * MAX_MAP_DEPTH; i++) {
            mapData[i] = new MapCell();
        }
    }

    public MapCell getMapCell(int x, int y, int z) {
        int index = tileToIndex(x, y, z);
        if (index < 0 || index >= mapData.length) {
            return null;
        }

        return mapData[index];
    }

    public int tileToIndex(int x, int y, int z) {
        return z * width * height + y * width + x;
    }

    public void indexToTile(int index, Vector3i tileCoordinates) {
        int flatSize = width * height;
        int layerIndex = index % flatSize;

        tileCoordinates.set(layerIndex % width, layerIndex / width, index / flatSize);
    }

    public boolean isTileCoordWithinBounds(int x, int y, int z) {
        return x >= 0 && x < width &&
                y >= 0 && y < height &&
                z >= 0 && z < Map.MAX_MAP_DEPTH;
    }

    public void worldToTile(Vector3f world, Vector3i tile) {
        int x = (int) Math.floor(world.x / tileSize);
        int y = (int) Math.floor(world.y / tileSize);
        int z = (int) Math.floor(world.z / tileSize);
        tile.set(x, z, y);
    }

    public void tileToWorld(Vector3i tile, Vector3f world) {
        float x = tile.x * tileSize;
        float y = tile.y * tileSize;
        float z = tile.z * tileSize;
        world.set(x, z, y);
    }

    /**
     * Expects tile coordinates
     *
     * @param x      positive goes right
     * @param y      positive goes into the screen
     * @param z      positive goes up
     * @param bounds to get for the tile
     * @return true if succeeded
     */
    public boolean getTileBounds(int x, int y, int z, Box bounds) {
        if (!isTileCoordWithinBounds(x, y, z)) {
            return false;
        }

        float worldCoordsX = x * tileSize;
        float worldCoordsY = z * tileSize;
        float worldCoordsZ = y * tileSize;

        bounds.regenerate(worldCoordsX, worldCoordsY, worldCoordsZ,
                worldCoordsX + tileSize, worldCoordsY + tileSize, worldCoordsZ + tileSize);

        return true;
    }

    public MapCell[] getMapData() {
        return mapData;
    }

    public void setMapData(MapCell[] mapData) {
        this.mapData = mapData;
    }

    public byte[] getCollisionData() {
        return collisionData;
    }

    public void setCollisionData(byte[] collisionData) {
        this.collisionData = collisionData;
    }

    public int getTileSize() {
        return tileSize;
    }

    public void setTileSize(int tileSize) {
        this.tileSize = tileSize;
    }

    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Box getBounds() {
        return bounds;
    }
}
