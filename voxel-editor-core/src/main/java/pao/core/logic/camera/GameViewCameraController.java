package pao.core.logic.camera;

import com.wildrune.rune.geometry.Plane;
import com.wildrune.rune.util.Maths;
import com.wildrune.rune.viewport.camera.BaseCamera;
import com.wildrune.rune.viewport.camera.PerspCamera;

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
public class GameViewCameraController implements CameraController {

    private final Vector2i currentMousePosition = new Vector2i();
    private final Vector2i previousMousePosition = new Vector2i();

    private final Vector3f viewportPos = new Vector3f();
    private final Vector3f lastViewportPos = new Vector3f();
    private final Plane upPlane = new Plane(Maths.UNIT_Y, 0);

    private PerspCamera perspectiveCamera;

    public GameViewCameraController() {
        perspectiveCamera = new PerspCamera(20, 512f, 2048);
    }

    @Override
    public void setup(BaseCamera oldCamera) {
        perspectiveCamera.getWorldMatrix().identity();
        float camHeight = 26 * 32;
        float camDepth = -22 * 32;

        if (oldCamera == null) {
            perspectiveCamera.setPosition(0, camHeight, camDepth);
        } else {
            Matrix4f oldWorld = oldCamera.getWorldMatrix();
            perspectiveCamera.setPosition(oldWorld.m30(), camHeight, oldWorld.m32());
        }

        perspectiveCamera.rotate(50, Maths.UNIT_X);
    }

    @Override
    public BaseCamera getCamera() {
        return perspectiveCamera;
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
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        currentMousePosition.set(e.getPoint().x, e.getPoint().y);

        if (SwingUtilities.isMiddleMouseButton(e)) {
            perspectiveCamera.setScreenToWorldOnPlane(currentMousePosition, upPlane, viewportPos);
            perspectiveCamera.setScreenToWorldOnPlane(previousMousePosition, upPlane, lastViewportPos);
            perspectiveCamera.move(lastViewportPos.sub(viewportPos));
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
