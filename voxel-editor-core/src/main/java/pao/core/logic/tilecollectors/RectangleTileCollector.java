package pao.core.logic.tilecollectors;

import pao.core.models.Map;

import com.wildrune.rune.geometry.Box;
import com.wildrune.rune.geometry.Ray;
import com.wildrune.rune.util.Maths;

import org.joml.Vector3f;
import org.joml.Vector3i;

/**
 * @author Mark "Wilds" van der Wal
 * @since 23-3-2018
 */
public class RectangleTileCollector extends BaseMapTileCollector {
    private boolean isDragging;

    private Vector3f worldCoordinate;
    private Vector3i startTileCoordinates;
    private Vector3i endTileCoordinates;
    private Vector3i worldAreaDimensions;

    private Box area;

    public RectangleTileCollector(Map map) {
        super(map.getWidth() * map.getHeight());

        worldCoordinate = new Vector3f();
        startTileCoordinates = new Vector3i();
        endTileCoordinates = new Vector3i();
        worldAreaDimensions = new Vector3i();
        area = new Box();
    }

    @Override
    public void start(Map map, Ray ray) {
        Maths.rayIntersectsPlane(ray, workingPlane, worldCoordinate);
        map.worldToTile(worldCoordinate, startTileCoordinates);

        if (map.isTileCoordWithinBounds(startTileCoordinates.x, startTileCoordinates.y, startTileCoordinates.z)) {
            udpateArea(map);

            isDragging = true;
        }
    }

    @Override
    public void move(Map map, Ray ray) {
        Maths.rayIntersectsPlane(ray, workingPlane, worldCoordinate);
        map.worldToTile(worldCoordinate, endTileCoordinates);

        if (map.isTileCoordWithinBounds(endTileCoordinates.x, endTileCoordinates.y, endTileCoordinates.z)) {
            udpateArea(map);
        }
    }

    @Override
    public void stop(Map map, Ray ray) {
        Maths.rayIntersectsPlane(ray, workingPlane, worldCoordinate);
        map.worldToTile(worldCoordinate, endTileCoordinates);

        if (map.isTileCoordWithinBounds(endTileCoordinates.x, endTileCoordinates.y, endTileCoordinates.z)) {
            udpateArea(map);
        }

        isDragging = false;
    }

    private void udpateArea(Map map) {
        if (isDragging) {
            area.regenerate(startTileCoordinates.x, startTileCoordinates.z, startTileCoordinates.y,
                    endTileCoordinates.x, endTileCoordinates.z, endTileCoordinates.y);
        } else {
            area.regenerate(endTileCoordinates.x, endTileCoordinates.z, endTileCoordinates.y);
        }

        updateTileIndices(map);
    }

    private void updateTileIndices(Map map) {
        Vector3f min = area.getMin();
        Vector3f max = area.getMax();

        teardown();

        worldAreaDimensions.set((int) max.x, (int) max.y, (int) max.z)
                .sub((int) min.x, (int) min.y, (int) min.z)
                .add(1, 1, 1);

        int flatSize = worldAreaDimensions.x * worldAreaDimensions.z;
        int totalTiles = flatSize * worldAreaDimensions.y;

        for (int i = 0; i < totalTiles; i++) {
            int layerIndex = i % flatSize;
            int z = i / flatSize;
            int y = layerIndex / worldAreaDimensions.x;
            int x = layerIndex % worldAreaDimensions.x;

            int worldTileIndex = map.tileToIndex((int) min.x + x, (int) min.z + y, (int) min.y + z);
            tileIndices.add(worldTileIndex);
        }
    }

    @Override
    public void teardown() {
        tileIndices.clear();
    }
}
