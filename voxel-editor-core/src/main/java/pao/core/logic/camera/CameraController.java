package pao.core.logic.camera;

import wilds.rune.viewport.camera.BaseCamera;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * @author Mark "Wilds" van der Wal
 * @since 13-3-2018
 */
public interface CameraController {
    public void setup(BaseCamera oldCamera);

    public BaseCamera getCamera();

    public void update(float deltaTime);

    public void mousePressed(MouseEvent e);

    public void mouseReleased(MouseEvent e);

    public void mouseWheelMoved(MouseWheelEvent e);

    public void mouseDragged(MouseEvent e);

    public void mouseMoved(MouseEvent e);

    public void keyDown(KeyEvent e);

    public void keyUp(KeyEvent e);
}
