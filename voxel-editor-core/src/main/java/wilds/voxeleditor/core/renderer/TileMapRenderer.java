package wilds.voxeleditor.core.renderer;

import wilds.voxeleditor.core.models.Map;
import wilds.voxeleditor.core.models.MapCell;
import wilds.voxeleditor.core.models.mapobjects.MapObject;
import wilds.voxeleditor.core.models.mapobjects.operations.MapObjectOperationAdapter;
import wilds.voxeleditor.core.models.mapobjects.tiles.CubeTile;
import wilds.voxeleditor.core.models.mapobjects.tiles.CubeTileSide;
import wilds.voxeleditor.core.services.MapService;

import wilds.rune.renderer.Color;
import wilds.rune.renderer.IRenderer;
import wilds.rune.renderer.batch.PrimitiveBatch;
import wilds.rune.renderer.batch.TileBatch;
import wilds.rune.renderer.gl.Texture2D;
import wilds.rune.renderer.gl.states.SamplerState;
import wilds.rune.util.Maths;
import wilds.rune.util.Textures;
import wilds.rune.viewport.IViewport;
import wilds.rune.viewport.camera.BaseCamera;

import com.jogamp.opengl.GL2ES2;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author Mark "Wilds" van der Wal
 * @since 10-3-2018
 */
public class TileMapRenderer {

    private TileBatch tileBatch;
    private PrimitiveBatch primitiveBatch;

    private Texture2D texture;
    private IViewport viewport;
    private MapService mapService;

    private TileRenderOperation tileRenderOperation;
    private Color baseGridColor = Color.GRAY;
    private boolean drawGrid;
    private int tilesDrawn = 0;

    public TileMapRenderer(IViewport viewport, MapService mapService) {
        this.viewport = viewport;
        this.mapService = mapService;
        tileRenderOperation = new TileRenderOperation();
    }

    public void create() {
        IRenderer<GL2ES2> renderer = viewport.getRenderer();
        primitiveBatch = new PrimitiveBatch(renderer);
        primitiveBatch.create();

        tileBatch = new TileBatch(renderer);
        tileBatch.create();

        try {
            BufferedImage image = Textures.loadImage("/images/tileseta.png");
            texture = Texture2D.create2D(renderer.getGL(), image, SamplerState.PointWrap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dispose() {
        primitiveBatch.dispose();
        tileBatch.dispose();
        texture.dispose();
    }

    /**
     * todo: implement a broad phase for faster rendering!
     */
    public void render() {
        Map map = mapService.getMap();
        BaseCamera camera = viewport.getCamera();
        tilesDrawn = 0;

        if (drawGrid) {
            drawBaseGrid();
        }

        MapCell[] mapCells = map.getMapData();
        if (mapService.hasMap() && mapCells.length > 0) {
            texture.bind();
            tileBatch.begin(camera.getViewMatrix(), camera.getViewportProjectionMatrix(),
                    map.getTileSize(), 1f, drawGrid);
            tileRenderOperation.setCamera(camera);

            for (int i = 0; i < mapCells.length; i++) {
                if (mapCells[i].isEmpty()) {
                    continue;
                }

                MapObject tile = mapCells[i].getCellObject();
                tileRenderOperation.setTileIndex(i);
                tile.accept(tileRenderOperation);
            }

            tileBatch.end();
        }
    }

    private void drawBaseGrid() {
        Map map = mapService.getMap();
        BaseCamera camera = viewport.getCamera();
        int mapWidth = map.getWidth();
        int mapHeight = map.getHeight();
        int ts = map.getTileSize();

        primitiveBatch.begin(camera.getViewMatrix(), camera.getViewportProjectionMatrix());
        primitiveBatch.color(baseGridColor);

        for (int x = 0; x <= mapWidth; x++) {
            primitiveBatch.line(x * ts, 0, 0, x * ts, 0, mapHeight * ts);
        }

        for (int y = 0; y <= mapHeight; y++) {
            primitiveBatch.line(0, 0, y * ts, mapWidth * ts, 0, y * ts);
        }

        primitiveBatch.end();
    }

    public void toggleGrid() {
        drawGrid = !drawGrid;
    }

    private class TileRenderOperation extends MapObjectOperationAdapter {
        private BaseCamera camera;
        private int tileIndex;

        private Vector3i tempTileCoordinates;
        private Vector3f tempCamDir;

        private CubeTileSide visibleSides[];
        private int visibleSidesCount;

        TileRenderOperation() {
            tempTileCoordinates = new Vector3i();
            tempCamDir = new Vector3f();
            visibleSides = new CubeTileSide[6];
        }

        void setTileIndex(int index) {
            tileIndex = index;
        }

        void setCamera(BaseCamera camera) {
            this.camera = camera;
        }

        /**
         * todo: optimize for single tiles as tile cubes aren't just binary empty or solid
         * These aren't voxel (they are close)
         */
        private void setVisibleSide(CubeTile tile, CubeTileSide side, Vector3f cameraDirection, Vector3i tileCoords) {
            short tileId = tile.getTileId(side);
            if (tileId == CubeTile.EMPTY_TILE_ID) {
                return;
            }

            Map map = mapService.getMap();
            MapCell[] cells = map.getMapData();
            if (cameraDirection.dot(side.getAxis()) <= Maths.EPSILON) {
                Vector3f axis = side.getAxis();

                int x = (int) (tileCoords.x + axis.x);
                int y = (int) (tileCoords.y + axis.z);
                int z = (int) (tileCoords.z + axis.y);

                // if this side's neighbor is outside map bounds or its empty
                // we can check the next side because its visible.
                int index = map.tileToIndex(x, y, z);
                if (!map.isTileCoordWithinBounds(x, y, z) || cells[index].isEmpty()) {
                    visibleSides[visibleSidesCount++] = side;
                }
            }
        }

        /**
         * Check which tilesides we can see so that we only draw the visible tiles
         */
        private void cullOccludedAndBackfaceSides(CubeTile tile, Vector3i tileCoordinates) {
            Matrix4f camWorld = camera.getWorldMatrix();
            int tileSize = mapService.getMap().getTileSize();
            int halfTileSize = tileSize / 2;
            visibleSidesCount = 0;

            tempCamDir.set(tileCoordinates.x * tileSize + halfTileSize - camWorld.m30(),
                    tileCoordinates.z * tileSize + halfTileSize - camWorld.m31(),
                    tileCoordinates.y * tileSize + halfTileSize - camWorld.m32());

            setVisibleSide(tile, CubeTileSide.Front, tempCamDir, tileCoordinates);
            setVisibleSide(tile, CubeTileSide.Back, tempCamDir, tileCoordinates);
            setVisibleSide(tile, CubeTileSide.Left, tempCamDir, tileCoordinates);
            setVisibleSide(tile, CubeTileSide.Right, tempCamDir, tileCoordinates);
            setVisibleSide(tile, CubeTileSide.Bottom, tempCamDir, tileCoordinates);
            setVisibleSide(tile, CubeTileSide.Top, tempCamDir, tileCoordinates);
        }

        @Override
        public void visit(CubeTile tile) {
            Map map = mapService.getMap();
            int tileSize = map.getTileSize();
            map.indexToTile(tileIndex, tempTileCoordinates);
            cullOccludedAndBackfaceSides(tile, tempTileCoordinates);

            // iterate visible tile cube sides
            for (int i = 0; i < visibleSidesCount; i++) {
                CubeTileSide side = visibleSides[i];
                short tileId = tile.getTileId(side);

                tilesDrawn++;

                // todo: replace with correct tileset data
                int tilesetWidth = texture.getWidth() / tileSize;
                int tilesetX = tileId % tilesetWidth;
                int tilesetY = tileId / tilesetWidth;

                tileBatch.tileAxisAligned(tempTileCoordinates.x, tempTileCoordinates.z, tempTileCoordinates.y,
                        tilesetX, tilesetY, tileSize, side.ordinal(), texture);
            }
        }
    }
}
