package wilds.voxeleditor.core.logic.camera;

import wilds.voxeleditor.core.models.Map;

import wilds.rune.geometry.Plane;
import wilds.rune.util.Maths;
import wilds.rune.viewport.camera.BaseCamera;
import wilds.rune.viewport.camera.PerspCamera;

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
public class FreeCameraController implements CameraController {

    private final Vector2i currentMousePosition = new Vector2i();
    private final Vector2i previousMousePosition = new Vector2i();

    private final Plane fpsDragPlane = new Plane(Maths.UNIT_Y, 0);

    private final Vector3f viewportPos = new Vector3f();
    private final Vector3f lastViewportPos = new Vector3f();
    private final Vector3f tempNormal = new Vector3f();
    private final Vector3f pointInfrontOfCamera = new Vector3f();

    private PerspCamera perspectiveCamera;

    private final float cameraSpeed = 768;
    private final Vector3f direction = new Vector3f();

    public FreeCameraController() {
        perspectiveCamera = new PerspCamera(70, 0.1f, 8192);
    }

    @Override
    public void setup(BaseCamera oldCamera) {
        perspectiveCamera.getWorldMatrix().identity();
        float camHeight = Map.MAX_MAP_DEPTH * 32 + 256;

        if (oldCamera == null) {
            perspectiveCamera.setPosition(0, camHeight, 0);
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
        perspectiveCamera.moveLocal(direction.x * cameraSpeed * deltaTime, 0, direction.z * cameraSpeed * deltaTime);
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

        perspectiveCamera.moveLocal(0, 0, (float) -e.getWheelRotation() * 32);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        currentMousePosition.set(e.getPoint().x, e.getPoint().y);

        if (SwingUtilities.isMiddleMouseButton(e)) {
            Matrix4f camWorld = perspectiveCamera.getWorldMatrix();

            // set plane normal with camera forward normal
            tempNormal.set(camWorld.m20(), camWorld.m21(), camWorld.m22());
            fpsDragPlane.setNormal(tempNormal);
            fpsDragPlane.getNormal().negate();

            // calculate point infront of the camera
            camWorld.getTranslation(pointInfrontOfCamera);
            pointInfrontOfCamera.add(tempNormal.mul(128));

            fpsDragPlane.setDistance(pointInfrontOfCamera);

            perspectiveCamera.setScreenToWorldOnPlane(currentMousePosition, fpsDragPlane, viewportPos);
            perspectiveCamera.setScreenToWorldOnPlane(previousMousePosition, fpsDragPlane, lastViewportPos);
            perspectiveCamera.move(lastViewportPos.sub(viewportPos));

        } else if (SwingUtilities.isRightMouseButton(e)) {
            float yRotation = currentMousePosition.x - previousMousePosition.x;
            float xRotation = currentMousePosition.y - previousMousePosition.y;

            perspectiveCamera.rotateWorld(yRotation * 0.2f, Maths.UNIT_Y);
            perspectiveCamera.rotate(xRotation * 0.2f, Maths.UNIT_X);
        }

        previousMousePosition.set(currentMousePosition);
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void keyDown(KeyEvent keyEvent) {
        if (keyEvent.getKeyCode() == KeyEvent.VK_W) {
            direction.set(0, 0, 1);
        } else if (keyEvent.getKeyCode() == KeyEvent.VK_S) {
            direction.set(0, 0, -1);
        }

        if (keyEvent.getKeyCode() == KeyEvent.VK_D) {
            direction.set(1, 0, direction.z);
        } else if (keyEvent.getKeyCode() == KeyEvent.VK_A) {
            direction.set(-1, 0, direction.z);
        }

        if (!direction.equals(Maths.ZERO)) {
            direction.normalize();
        }
    }

    @Override
    public void keyUp(KeyEvent keyEvent) {
        if (keyEvent.getKeyCode() == KeyEvent.VK_W
                || keyEvent.getKeyCode() == KeyEvent.VK_S) {
            direction.set(direction.x, 0, 0);
        }

        if (keyEvent.getKeyCode() == KeyEvent.VK_D
                || keyEvent.getKeyCode() == KeyEvent.VK_A) {
            direction.set(0, 0, direction.z);
        }
    }
}
