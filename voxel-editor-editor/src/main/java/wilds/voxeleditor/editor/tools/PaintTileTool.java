package wilds.voxeleditor.editor.tools;

import wilds.voxeleditor.core.logic.camera.CameraController;
import wilds.voxeleditor.core.logic.camera.FreeCameraController;
import wilds.voxeleditor.core.logic.camera.GameViewCameraController;
import wilds.voxeleditor.core.logic.commands.EraseTilesCommand;
import wilds.voxeleditor.core.logic.commands.PaintTilesCommand;
import wilds.voxeleditor.core.logic.commands.PlaceTilesCommand;
import wilds.voxeleditor.core.logic.tilecollectors.*;
import wilds.voxeleditor.core.models.Map;
import wilds.voxeleditor.core.models.MapCell;
import wilds.voxeleditor.core.models.mapobjects.tiles.CubeTileSide;
import wilds.voxeleditor.core.services.CommandService;
import wilds.voxeleditor.core.services.MapService;
import wilds.voxeleditor.editor.models.ToolBarElement;
import wilds.voxeleditor.editor.models.ToolBarIcon;
import wilds.voxeleditor.editor.views.ToolBarView;

import wilds.rune.geometry.Box;
import wilds.rune.geometry.Plane;
import wilds.rune.geometry.Ray;
import wilds.rune.renderer.Color;
import wilds.rune.renderer.IRenderer;
import wilds.rune.renderer.batch.PrimitiveBatch;
import wilds.rune.renderer.gl.states.BlendState;
import wilds.rune.renderer.gl.states.RasterizerState;
import wilds.rune.util.Maths;
import wilds.rune.viewport.IViewport;
import wilds.rune.viewport.camera.BaseCamera;

import com.alee.laf.button.WebToggleButton;
import com.alee.managers.style.StyleId;
import com.jogamp.opengl.GL2ES2;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * @author Mark "Wilds" van der Wal
 * @since 19-3-2018
 */
public class PaintTileTool extends MapTool {

    private final Color selectionHoverColor = Color.fromRGBA(132, 179, 255, 125);

    private enum PaintMode {
        Cube,
        Quad,
        Erase
    }

    private CameraController currentCameraController;
    private CameraController gameViewCameraController;
    private CameraController freeCameraController;

    private TileCollector currentTileCollector;
    private BaseMapTileCollector pencilTileCollector;
    private BaseMapTileCollector rectangleTileCollector;
    private BucketTileCollector bucketTileCollector;

    private PaintMode currentPaintMode;
    private PrimitiveBatch primitiveBatch;
    private boolean modifierShiftPressed;

    private Plane workingPlane;
    private Ray worldRay;

    // temp variables used only for calculations
    private Vector3i previewTileCoordinates;
    private Vector3f startHit;
    private Vector3f endHit;

    // below variables are only valid if a solid tile is hit
    private Box tileHitBounds;
    private Vector2f tileHitExtends;
    private Vector3f tileHitPosition;
    private Vector3f tileHitFaceNormal;

    public PaintTileTool(IViewport viewport, MapService mapService, CommandService commandService) {
        super(viewport, mapService, commandService);
        worldRay = new Ray();
        workingPlane = new Plane();
        workingPlane.setNormal(Maths.UNIT_Y);
        workingPlane.setDistance(Maths.BIG_EPSILON);
        previewTileCoordinates = new Vector3i();

        tileHitBounds = new Box();
        tileHitBounds.regenerate(0, 0, 0);
        tileHitExtends = new Vector2f();
        tileHitPosition = new Vector3f();
        tileHitFaceNormal = new Vector3f();
        tileHitFaceNormal.set(Maths.UNIT_Z);
        startHit = new Vector3f();
        endHit = new Vector3f();

        currentPaintMode = PaintMode.Cube;
        primitiveBatch = new PrimitiveBatch(viewport.getRenderer());
    }

    @Override
    public void create() {
        Map map = mapService.getMap();
        primitiveBatch.create();

        gameViewCameraController = new GameViewCameraController();
        freeCameraController = new FreeCameraController();

        pencilTileCollector = new PencilTileCollector(map);
        rectangleTileCollector = new RectangleTileCollector(map);
        bucketTileCollector = new BucketTileCollector(map);
    }

    @Override
    public void dispose() {
        primitiveBatch.dispose();
    }

    @Override
    public void setup(ToolBarView toolBar) {
        int defaultIconSize = ToolBarView.DEFAULT_ICON_SIZE;

        // view modes
        JToggleButton gameViewButton = new WebToggleButton(StyleId.buttonIconHover, ToolBarIcon.GameView.getIcon(defaultIconSize));
        JToggleButton freeViewButton = new WebToggleButton(StyleId.buttonIconHover, ToolBarIcon.FreeView.getIcon(defaultIconSize));

        gameViewButton.addActionListener(actionEvent -> onToolbarAction(ToolBarElement.GameView));
        freeViewButton.addActionListener(actionEvent -> onToolbarAction(ToolBarElement.FreeView));

        ButtonGroup cameraGroup = new ButtonGroup();
        cameraGroup.add(freeViewButton);
        cameraGroup.add(gameViewButton);

        // draw modes
        JToggleButton cubeButton = new WebToggleButton(StyleId.buttonIconHover, ToolBarIcon.Cube.getIcon(defaultIconSize));
        JToggleButton quadButton = new WebToggleButton(StyleId.buttonIconHover, ToolBarIcon.Quad.getIcon(defaultIconSize));
        JToggleButton eraserButton = new WebToggleButton(StyleId.buttonIconHover, ToolBarIcon.Eraser.getIcon(defaultIconSize));
        cubeButton.addActionListener(actionEvent -> onToolbarAction(ToolBarElement.Cube));
        quadButton.addActionListener(actionEvent -> onToolbarAction(ToolBarElement.Quad));
        eraserButton.addActionListener(actionEvent -> onToolbarAction(ToolBarElement.Eraser));

        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(cubeButton);
        modeGroup.add(quadButton);
        modeGroup.add(eraserButton);

        // Pencil, Rect and Bucket
        JToggleButton pencilButton = new WebToggleButton(StyleId.buttonIconHover, ToolBarIcon.Pencil.getIcon(defaultIconSize));
        JToggleButton rectButton = new WebToggleButton(StyleId.buttonIconHover, ToolBarIcon.Rect.getIcon(defaultIconSize));
        JToggleButton bucketButton = new WebToggleButton(StyleId.buttonIconHover, ToolBarIcon.Bucket.getIcon(defaultIconSize));

        pencilButton.addActionListener(actionEvent -> onToolbarAction(ToolBarElement.Pencil));
        rectButton.addActionListener(actionEvent -> onToolbarAction(ToolBarElement.Rect));
        bucketButton.addActionListener(actionEvent -> onToolbarAction(ToolBarElement.Bucket));

        // set default
        freeViewButton.setSelected(true);
        cubeButton.setSelected(true);
        pencilButton.setSelected(true);

        ButtonGroup toolGroup = new ButtonGroup();
        toolGroup.add(pencilButton);
        toolGroup.add(rectButton);
        toolGroup.add(bucketButton);

        toolBar.add(freeViewButton);
        toolBar.add(gameViewButton);
        toolBar.addSeparator();
        toolBar.add(cubeButton);
        toolBar.add(quadButton);
        toolBar.add(eraserButton);
        toolBar.addSeparator();
        toolBar.add(pencilButton);
        toolBar.add(rectButton);
        toolBar.add(bucketButton);

        // set default state
        changeCameraController(freeCameraController);
        changeTileCollector(pencilTileCollector);
    }

    @Override
    public void onToolbarAction(ToolBarElement element) {
        switch (element) {
            case GameView:
                changeCameraController(gameViewCameraController);
                break;
            case FreeView:
                changeCameraController(freeCameraController);
                break;
            case Cube:
                currentPaintMode = PaintMode.Cube;
                break;
            case Quad:
                currentPaintMode = PaintMode.Quad;
                break;
            case Eraser:
                currentPaintMode = PaintMode.Erase;
                break;
            case Pencil:
                changeTileCollector(pencilTileCollector);
                break;
            case Rect:
                changeTileCollector(rectangleTileCollector);
                break;
            case Bucket:
                changeTileCollector(bucketTileCollector);
                break;
        }
    }

    private void changeTileCollector(BaseMapTileCollector tileCollector) {
        if (currentTileCollector == tileCollector) {
            return;
        }

        tileCollector.setup(workingPlane);
        currentTileCollector = tileCollector;
    }

    private void changeCameraController(CameraController controller) {
        if (currentCameraController == controller) {
            return;
        }

        BaseCamera oldCamera = currentCameraController == null ? null : currentCameraController.getCamera();
        currentCameraController = controller;
        currentCameraController.setup(oldCamera);
        viewport.setCamera(currentCameraController.getCamera());
    }

    @Override
    public void update(float deltaTime) {
        currentCameraController.update(deltaTime);
    }

    @Override
    public void render() {
        final BaseCamera camera = viewport.getCamera();
        final IRenderer<GL2ES2> renderer = viewport.getRenderer();
        final Map map = mapService.getMap();

        renderer.pushBlendState(BlendState.NonPreMultiplied);
        renderer.pushRasterizerState(RasterizerState.DepthAlways);

        primitiveBatch.begin(camera.getViewMatrix(), camera.getViewportProjectionMatrix());
        primitiveBatch.color(selectionHoverColor);

        IntSet indices = currentTileCollector.getTileIndices();
        IntIterator indexIterator = indices.iterator();
        while (indexIterator.hasNext()) {
            map.indexToTile(indexIterator.nextInt(), previewTileCoordinates);
            renderHighlightTile(previewTileCoordinates);
        }

        primitiveBatch.end();

        renderer.popRasterizerState();
        renderer.popBlendState();
    }

    private void renderHighlightTile(Vector3i tileCoordinates) {
        final Map map = mapService.getMap();
        final int tileSize = map.getTileSize();
        final int selectorHeight = 8;
        final int tileSizeHalf = tileSize / 2;
        final int tileSizeWorkingPlane = tileSizeHalf - selectorHeight / 2;

        Vector3f workingPlaneNormal = workingPlane.getNormal();
        int positionOffsetX = (int) (tileSizeHalf - workingPlaneNormal.x * tileSizeWorkingPlane);
        int positionOffsetY = (int) (tileSizeHalf - workingPlaneNormal.y * tileSizeWorkingPlane);
        int positionOffsetZ = (int) (tileSizeHalf - workingPlaneNormal.z * tileSizeWorkingPlane);

        float planeX = Math.abs(workingPlaneNormal.x);
        float planeY = Math.abs(workingPlaneNormal.y);
        float planeZ = Math.abs(workingPlaneNormal.z);

        float sizeX = planeX * selectorHeight + (1 - planeX) * tileSize;
        float sizeY = planeY * selectorHeight + (1 - planeY) * tileSize;
        float sizeZ = planeZ * selectorHeight + (1 - planeZ) * tileSize;

        // draw a cube if shift is pressed
        if (currentPaintMode == PaintMode.Erase
                || currentPaintMode == PaintMode.Cube) {
            sizeX = tileSize;
            sizeY = tileSize;
            sizeZ = tileSize;

            tileCoordinates
                    .mul(tileSize)
                    .add(tileSizeHalf, tileSizeHalf, tileSizeHalf);
        } else {
            tileCoordinates
                    .mul(tileSize)
                    .add(positionOffsetX + (int) (workingPlaneNormal.x * tileSize),
                            positionOffsetZ + (int) (workingPlaneNormal.z * tileSize),
                            positionOffsetY + (int) (workingPlaneNormal.y * tileSize));
        }

        primitiveBatch.fillBox(tileCoordinates.x,
                (float) tileCoordinates.z,
                (float) tileCoordinates.y,
                sizeX, sizeY, sizeZ);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        currentTileCollector.teardown();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        currentCameraController.mousePressed(e);
        BaseCamera camera = viewport.getCamera();
        camera.setScreenToRay(e.getX(), e.getY(), worldRay);

        if (SwingUtilities.isLeftMouseButton(e)) {
            updateWorkingPlane();
            currentTileCollector.start(mapService.getMap(), worldRay);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        currentCameraController.mouseDragged(e);
        BaseCamera camera = viewport.getCamera();
        camera.setScreenToRay(e.getX(), e.getY(), worldRay);

        if (SwingUtilities.isLeftMouseButton(e)) {
            currentTileCollector.move(mapService.getMap(), worldRay);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        currentCameraController.mouseMoved(e);

        BaseCamera camera = viewport.getCamera();
        camera.setScreenToRay(e.getX(), e.getY(), worldRay);

        updateWorkingPlane();
        currentTileCollector.move(mapService.getMap(), worldRay);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        currentCameraController.mouseReleased(e);
        BaseCamera camera = viewport.getCamera();
        camera.setScreenToRay(e.getX(), e.getY(), worldRay);

        if (SwingUtilities.isLeftMouseButton(e)) {
            currentTileCollector.stop(mapService.getMap(), worldRay);

            // commit data to map
            IntSet indiceSet = currentTileCollector.getTileIndices();
            if (indiceSet.size() > 0) {
                int tileIndex = currentPaintMode == PaintMode.Cube ? 65 : 92;

                switch (currentPaintMode) {
                    case Erase:
                        commandService.executeCommand(new EraseTilesCommand(indiceSet.toIntArray(), mapService));
                        break;
                    case Cube:
                        commandService.executeCommand(new PlaceTilesCommand(indiceSet.toIntArray(), tileIndex, mapService));
                        break;
                    case Quad:
                        CubeTileSide side = CubeTileSide.getSideByNormal(workingPlane.getNormal());
                        commandService.executeCommand(new PaintTilesCommand(indiceSet.toIntArray(), tileIndex, side, mapService));
                        break;
                }

                currentTileCollector.teardown();
            }
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        currentCameraController.mouseWheelMoved(e);
    }

    @Override
    public void keyDown(KeyEvent e) {
        currentCameraController.keyDown(e);

        if (e.isShiftDown()) {
            modifierShiftPressed = true;
        }
    }

    @Override
    public void keyUp(KeyEvent e) {
        currentCameraController.keyUp(e);

        if (!e.isShiftDown()) {
            modifierShiftPressed = false;
        }
    }

    private boolean getTileHitInformation() {
        final Map map = mapService.getMap();
        final Box mapBounds = map.getBounds();
        final MapCell[] mapCells = map.getMapData();

        // reset tilebounds
        tileHitBounds.regenerate(0, 0, 0);

        // if we hit the map bounds continue
        if (Maths.rayIntersectsBox(worldRay, mapBounds, startHit, endHit)) {

            // iterate all cells hit by ray and check if solid
            Maths.visitCellsOverlappedBySegment(startHit, endHit, map.getTileSize(), tileCoordinates -> {
                int tileIndex = map.tileToIndex(tileCoordinates.x, tileCoordinates.z, tileCoordinates.y);
                if (mapCells[tileIndex].isEmpty()) {
                    return false;
                }

                // get tile hitbounds
                map.getTileBounds(tileCoordinates.x, tileCoordinates.z, tileCoordinates.y, tileHitBounds);

                // get tile hitposition
                Maths.rayIntersectsBox(worldRay, tileHitBounds, tileHitExtends);
                tileHitPosition.set(worldRay.getDirection()).mul(tileHitExtends.x).add(worldRay.getPosition());

                // get tile hitface normal
                Vector3f faceNormal = Maths.getBoxFaceNormal(tileHitBounds, tileHitPosition);
                tileHitFaceNormal.set(faceNormal);

                return true;
            });
        }

        return tileHitBounds.hasVolume3D();
    }

    private void updateWorkingPlane() {
        switch (currentPaintMode) {
            case Cube:
                handleCubeModeUpdate();
                break;
            case Quad:
                handleQuadModeUpdate();
                break;
            case Erase:
                handleEraseModeUpdate();
                break;
        }
    }

    private void handleCubeModeUpdate() {
        if (getTileHitInformation()) {
            if (modifierShiftPressed) {
                workingPlane.setNormal(tileHitFaceNormal);
                workingPlane.getNormal().negate();
                workingPlane.setDistance(tileHitPosition);
                workingPlane.setDistance(workingPlane.getDistance() + Maths.BIG_EPSILON);
            } else {
                workingPlane.setNormal(tileHitFaceNormal);

                // set offsetted plane distance
                workingPlane.setDistance(tileHitPosition);
                workingPlane.setDistance(workingPlane.getDistance() + Maths.BIG_EPSILON);
            }
        } else {
            workingPlane.setNormal(Maths.UNIT_Y);
            workingPlane.setDistance(Maths.BIG_EPSILON);
        }
    }

    private void handleQuadModeUpdate() {
        if (getTileHitInformation()) {
            workingPlane.setNormal(tileHitFaceNormal);

            // set offsetted plane distance
            workingPlane.setDistance(tileHitPosition);
            workingPlane.setDistance(workingPlane.getDistance() - Maths.BIG_EPSILON);
        } else {
            workingPlane.setNormal(Maths.UNIT_Y);
            workingPlane.setDistance(-Maths.BIG_EPSILON);

            currentTileCollector.teardown();
        }
    }

    private void handleEraseModeUpdate() {
        if (getTileHitInformation()) {
            workingPlane.setNormal(Maths.UNIT_Y);

            // set offsetted plane distance
            workingPlane.setDistance(tileHitPosition);
            workingPlane.setDistance(workingPlane.getDistance() - Maths.BIG_EPSILON);
        } else {
            workingPlane.setNormal(Maths.UNIT_Y);
            workingPlane.setDistance(Maths.BIG_EPSILON);
        }
    }
}
