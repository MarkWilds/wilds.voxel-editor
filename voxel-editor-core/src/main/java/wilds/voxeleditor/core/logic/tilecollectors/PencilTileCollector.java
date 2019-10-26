package wilds.voxeleditor.core.logic.tilecollectors;

import wilds.voxeleditor.core.models.Map;

import wilds.rune.geometry.Ray;
import wilds.rune.util.Maths;

import org.joml.Vector3f;
import org.joml.Vector3i;

/**
 * @author Mark "Wilds" van der Wal
 * @since 21-3-2018
 */
public class PencilTileCollector extends BaseMapTileCollector {
    private boolean isDragging;

    private Vector3f worldCoordinate;
    private Vector3i tileCoordinates;

    public PencilTileCollector(Map map) {
        super(map.getWidth() * map.getHeight());

        worldCoordinate = new Vector3f();
        tileCoordinates = new Vector3i();
    }

    @Override
    public void start(Map map, Ray ray) {
        Maths.rayIntersectsPlane(ray, workingPlane, worldCoordinate);
        map.worldToTile(worldCoordinate, tileCoordinates);

        if (map.isTileCoordWithinBounds(tileCoordinates.x, tileCoordinates.y, tileCoordinates.z)) {
            isDragging = true;

            int index = map.tileToIndex(tileCoordinates.x, tileCoordinates.y, tileCoordinates.z);
            tileIndices.add(index);
        }
    }

    @Override
    public void move(Map map, Ray ray) {
        Maths.rayIntersectsPlane(ray, workingPlane, worldCoordinate);
        map.worldToTile(worldCoordinate, tileCoordinates);

        if (map.isTileCoordWithinBounds(tileCoordinates.x, tileCoordinates.y, tileCoordinates.z)) {
            int index = map.tileToIndex(tileCoordinates.x, tileCoordinates.y, tileCoordinates.z);
            if (!isDragging) {
                teardown();
            }
            tileIndices.add(index);
        }
    }

    @Override
    public void stop(Map map, Ray ray) {
        isDragging = false;

        if (map.isTileCoordWithinBounds(tileCoordinates.x, tileCoordinates.y, tileCoordinates.z)) {
            int index = map.tileToIndex(tileCoordinates.x, tileCoordinates.y, tileCoordinates.z);
            tileIndices.add(index);
        }
    }

    @Override
    public void teardown() {
        tileIndices.clear();
    }
}
