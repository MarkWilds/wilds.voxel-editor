package wilds.voxeleditor.core.logic.camera;

import wilds.voxeleditor.core.models.Map;

import wilds.rune.geometry.Plane;
import wilds.rune.util.Maths;
import wilds.rune.viewport.camera.BaseCamera;
import wilds.rune.viewport.camera.OrthoCamera;

import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import javax.swing.SwingUtilities;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * @author Mark "Wilds" van der Wal
 * @since 13-3-2018
 */
public class TopDownCameraController implements CameraController {

    private final Vector2i currentMousePosition = new Vector2i();
    private final Vector2i previousMousePosition = new Vector2i();

    private final Plane upPlane = new Plane(Maths.UNIT_Y, 0);

    private final Vector3f viewportPos = new Vector3f();
    private final Vector3f lastViewportPos = new Vector3f();

    private OrthoCamera orthographicCamera;

    public TopDownCameraController() {
        orthographicCamera = new OrthoCamera(0.1f, 1024f);
    }

    @Override
    public void setup(BaseCamera oldCamera) {
        orthographicCamera.getWorldMatrix().identity();
        float camHeight = Map.MAX_MAP_DEPTH * 32 + 64;

        if (oldCamera == null) {
            orthographicCamera.setPosition(0, camHeight, 0);
        } else {
            Matrix4f oldWorld = oldCamera.getWorldMatrix();
            orthographicCamera.setPosition(oldWorld.m30(), camHeight, oldWorld.m32());
        }

        orthographicCamera.rotate(90.0f, Maths.UNIT_X);
    }

    @Override
    public BaseCamera getCamera() {
        return orthographicCamera;
    }

    @Override
    public void update(float deltaTime) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        currentMousePosition.set(e.getPoint().x, e.getPoint().y);
        previousMousePosition.set(currentMousePosition);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        currentMousePosition.set(e.getPoint().x, e.getPoint().y);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (SwingUtilities.isRightMouseButton(e)
                || SwingUtilities.isLeftMouseButton(e)
                || SwingUtilities.isMiddleMouseButton(e))
            return;

        double zoomFactor = orthographicCamera.getZoom();
        double wheelRotation = 1.0 + -e.getWheelRotation() * 0.14;

        zoomFactor = zoomFactor / wheelRotation;

        if (zoomFactor > 0.98f && zoomFactor < 1.02f)
            zoomFactor = 1.0f;

        if (zoomFactor < 0.5f)
            zoomFactor = 0.5f;
        else if (zoomFactor > 4.0f)
            zoomFactor = 4.0f;

        orthographicCamera.setZoom((float) zoomFactor);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        currentMousePosition.set(e.getPoint().x, e.getPoint().y);

        if (SwingUtilities.isMiddleMouseButton(e)) {
            orthographicCamera.setScreenToWorldOnPlane(currentMousePosition, upPlane, viewportPos);
            orthographicCamera.setScreenToWorldOnPlane(previousMousePosition, upPlane, lastViewportPos);
            orthographicCamera.move(lastViewportPos.sub(viewportPos));
        }

        previousMousePosition.set(currentMousePosition);
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void keyDown(KeyEvent keyEvent) {

    }

    @Override
    public void keyUp(KeyEvent keyEvent) {

    }
}
