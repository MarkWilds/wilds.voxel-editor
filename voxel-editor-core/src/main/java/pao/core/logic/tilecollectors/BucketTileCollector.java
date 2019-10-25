package pao.core.logic.tilecollectors;

import pao.core.models.Map;
import pao.core.models.MapCell;

import com.wildrune.rune.geometry.Plane;
import com.wildrune.rune.geometry.Ray;
import com.wildrune.rune.util.Maths;

import org.joml.Vector3f;
import org.joml.Vector3i;

/**
 * @author Mark "Wilds" van der Wal
 * @since 23-3-2018
 */
public class BucketTileCollector extends BaseMapTileCollector {

    private Vector3f worldCoordinate;
    private Vector3i tileCoords;

    private int[] floodFillStack;
    private int stackPointer;
    private FloodFillRule floodFillRule;
    private FloodFillRule nullRule;

    public BucketTileCollector(Map map) {
        super(map.getWidth() * map.getHeight());
        floodFillStack = new int[256];

        worldCoordinate = new Vector3f();
        tileCoords = new Vector3i();
        nullRule = new NullFloodFillRule();
        floodFillRule = nullRule;
    }

    @Override
    public void start(Map map, Ray ray) {
    }

    @Override
    public void move(Map map, Ray ray) {
        Maths.rayIntersectsPlane(ray, workingPlane, worldCoordinate);
        map.worldToTile(worldCoordinate, tileCoords);

        if (map.isTileCoordWithinBounds(tileCoords.x, tileCoords.y, tileCoords.z)) {
            Maths.rayIntersectsPlane(ray, workingPlane, worldCoordinate);
            map.worldToTile(worldCoordinate, tileCoords);
            floodFill(map, workingPlane);
        }
    }

    @Override
    public void stop(Map map, Ray ray) {
    }

    public void setFloodFillRule(FloodFillRule rule) {
        if (rule == null && rule != nullRule) {
            floodFillRule = nullRule;
        } else {
            floodFillRule = rule;
        }
    }

    private void floodFill(Map map, Plane workPlane) {
        MapCell[] mapCells = map.getMapData();
        int index = map.tileToIndex(tileCoords.x, tileCoords.y, tileCoords.z);
        if (!tileIndices.contains(index)) {
            teardown();
        }

        if (floodFillRule.preCondition(mapCells[index])) {
            return;
        }

        stackPointer = 0;

        // get the axis for the workingplane
        Vector3f xAxis = Maths.getAbsHorizontalAxisAligned(workPlane.getNormal());
        Vector3f yAxis = Maths.getAbsVerticalAxisAligned(workPlane.getNormal());

        int width = (int) xAxis.dot(map.getWidth(), Map.MAX_MAP_DEPTH, map.getHeight());
        int height = (int) yAxis.dot(map.getWidth(), Map.MAX_MAP_DEPTH, map.getHeight());

        floodFillStack[stackPointer++] = index;
        while (stackPointer > 0) {
            index = floodFillStack[--stackPointer];
            map.indexToTile(index, tileCoords);

            // find last tile that has the target value
            while (getAxisValue(yAxis, tileCoords) >= 0 &&
                    !tileIndices.contains(index)
                    && floodFillRule.condition(mapCells[index])) {

                // get new tile for [x, y - 1]
                tileCoords.sub((int) yAxis.x, (int) yAxis.z, (int) yAxis.y);
                if (!map.isTileCoordWithinBounds(tileCoords.x, tileCoords.y, tileCoords.z)) {
                    break;
                }

                index = map.tileToIndex(tileCoords.x, tileCoords.y, tileCoords.z);
            }

            // get new tile for [x, y + 1]
            tileCoords.add((int) yAxis.x, (int) yAxis.z, (int) yAxis.y);
            index = map.tileToIndex(tileCoords.x, tileCoords.y, tileCoords.z);

            boolean spanLeft = false;
            boolean spanRight = false;
            while (getAxisValue(yAxis, tileCoords) < height
                    && !tileIndices.contains(index)
                    && floodFillRule.condition(mapCells[index])) {

                tileIndices.add(index);

                spanLeft = checkLeftCell(map, tileCoords, xAxis, mapCells, spanLeft);
                spanRight = checkRightCell(map, tileCoords, xAxis, mapCells, spanRight, width);

                // get new tile for tile coordinates [x, y + 1]
                tileCoords.add((int) yAxis.x, (int) yAxis.z, (int) yAxis.y);
                if (map.isTileCoordWithinBounds(tileCoords.x, tileCoords.y, tileCoords.z)) {
                    index = map.tileToIndex(tileCoords.x, tileCoords.y, tileCoords.z);
                }
            }
        }
    }

    // check tile coordinates for [x - 1, y]
    private boolean checkLeftCell(Map map, Vector3i tileCoords, Vector3f xAxis, MapCell[] mapCells, boolean span) {
        int spanX = tileCoords.x - (int) xAxis.x;
        int spanY = tileCoords.y - (int) xAxis.z;
        int spanZ = tileCoords.z - (int) xAxis.y;
        int tileIndex = map.tileToIndex(spanX, spanY, spanZ);
        return spawnSeed(mapCells, span, tileIndex,
                getAxisValue(xAxis, tileCoords) > 0);
    }

    // check tile coordinates for [x + 1, y]
    private boolean checkRightCell(Map map, Vector3i tileCoords, Vector3f xAxis, MapCell[] mapCells, boolean span, int width) {
        int spanX = tileCoords.x + (int) xAxis.x;
        int spanY = tileCoords.y + (int) xAxis.z;
        int spanZ = tileCoords.z + (int) xAxis.y;
        int tileIndex = map.tileToIndex(spanX, spanY, spanZ);
        return spawnSeed(mapCells, span, tileIndex,
                getAxisValue(xAxis, tileCoords) < width - 1);
    }

    private boolean spawnSeed(MapCell[] mapCells, boolean span, int tileIndex, boolean withinBounds) {
        if (withinBounds) {
            if (!span && !tileIndices.contains(tileIndex) && floodFillRule.condition(mapCells[tileIndex])) {
                floodFillStack[stackPointer++] = tileIndex;
                return true;
            } else if (span && !floodFillRule.condition(mapCells[tileIndex])) {
                return false;
            }
        }

        return span;
    }

    /**
     * Axis in world coordinates, coordinates in tile coordinates
     */
    private int getAxisValue(Vector3f axis, Vector3i coordinates) {
        return (int) axis.dot(coordinates.x, coordinates.z, coordinates.y);
    }

    @Override
    public void teardown() {
        tileIndices.clear();
    }

    private class NullFloodFillRule implements FloodFillRule {

        @Override
        public boolean preCondition(MapCell cell) {
            return cell.getCellObject() != null;
        }

        @Override
        public boolean condition(MapCell cell) {
            return cell.getCellObject() == null;
        }
    }

    public interface FloodFillRule {

        boolean preCondition(MapCell cell);

        boolean condition(MapCell cell);
    }
}
