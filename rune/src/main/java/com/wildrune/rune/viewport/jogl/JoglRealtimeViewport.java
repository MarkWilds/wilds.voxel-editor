package com.wildrune.rune.viewport.jogl;

import com.wildrune.rune.renderer.jogl.JoglRenderer;
import com.wildrune.rune.viewport.IViewport;
import com.wildrune.rune.viewport.camera.BaseCamera;
import com.wildrune.rune.viewport.handler.IViewportInputHandler;
import com.wildrune.rune.viewport.handler.IViewportLifeycleHandler;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.Animator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.event.MouseInputListener;
import java.awt.event.*;

/**
 * @author Mark "Wilds" van der Wal
 * @since 13/08/17
 */
public final class JoglRealtimeViewport extends GLJPanel implements IViewport {

    private static final Logger LOGGER = LogManager.getLogger(JoglRealtimeViewport.class);

    private Animator viewportAnimator;
    private JoglRenderer renderer;
    private BaseCamera camera;
    private IViewportLifeycleHandler lifeycleHandler;
    private IViewportInputHandler inputHandler;

    JoglRealtimeViewport(GLAutoDrawable sharedDrawable, JoglRenderer viewportRenderer) {
        super(sharedDrawable.getChosenGLCapabilities());
        setSharedAutoDrawable(sharedDrawable);

        viewportAnimator = new Animator(this);
        viewportAnimator.setRunAsFastAsPossible(true);

        RealTimeViewportHandler realTimeViewportHandler = new RealTimeViewportHandler();
        addGLEventListener(realTimeViewportHandler);
        addMouseListener(realTimeViewportHandler);
        addMouseMotionListener(realTimeViewportHandler);
        addMouseWheelListener(realTimeViewportHandler);
        addKeyListener(realTimeViewportHandler);

        renderer = viewportRenderer;
    }

    @Override
    public void setLifecycleHandler(IViewportLifeycleHandler handler) {
        lifeycleHandler = handler;
    }

    @Override
    public void setInputHandler(IViewportInputHandler handler) {
        inputHandler = handler;
    }

    @Override
    public GLJPanel getRoot() {
        return this;
    }

    @Override
    public JoglRenderer getRenderer() {
        return renderer;
    }

    @Override
    public BaseCamera getCamera() {
        return camera;
    }

    @Override
    public void setCamera(BaseCamera _camera) {
        camera = _camera;
        camera.setWindowDimensions(getSurfaceWidth(), getSurfaceHeight());
    }

    @Override
    public int getViewportWidth() {
        return getSurfaceWidth();
    }

    @Override
    public int getViewportHeight() {
        return getSurfaceHeight();
    }

    /**
     * Handles the viewport lifecycle and input delegation
     */
    private class RealTimeViewportHandler extends KeyAdapter implements GLEventListener, MouseInputListener, MouseWheelListener {

        private final int TARGET_FRAMES = 60;
        private final long NANO_SEC = 1_000_000_000;
        private final long NANO_FRAME = NANO_SEC / TARGET_FRAMES;
        private final long MAX_FRAME_TIME = (long) (NANO_FRAME * 1.5f);
        private final float DELTA_TIME = 1.0f / TARGET_FRAMES;

        private long lastTime = System.nanoTime();
        private long nanoTimer = 0;

        @Override
        public void init(GLAutoDrawable glAutoDrawable) {
            renderer.setGL(glAutoDrawable.getGL().getGL2ES2());
            renderer.initState();
            lifeycleHandler.create();

            viewportAnimator.start();
        }

        @Override
        public void dispose(GLAutoDrawable glAutoDrawable) {
            renderer.setGL(glAutoDrawable.getGL().getGL2ES2());
            lifeycleHandler.dispose();

            viewportAnimator.stop();
        }

        @Override
        public void display(GLAutoDrawable glAutoDrawable) {
            long currentTime = System.nanoTime();
            long passedTime = currentTime - lastTime;
            lastTime = currentTime;
            nanoTimer += passedTime;

            // avoid spiral of death
            if (nanoTimer > MAX_FRAME_TIME) {
                nanoTimer = MAX_FRAME_TIME;
            }

            if (nanoTimer > NANO_FRAME) {
                do {
                    nanoTimer -= NANO_FRAME;
                    lifeycleHandler.update(DELTA_TIME);
                } while (nanoTimer > NANO_FRAME);

                renderer.setGL(glAutoDrawable.getGL().getGL2ES2());
                lifeycleHandler.render();
            }
        }

        @Override
        public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
            renderer.setGL(glAutoDrawable.getGL().getGL2ES2());
            renderer.setViewport(0, 0, width, height);
            if (camera != null) {
                camera.setWindowDimensions(width, height);
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            inputHandler.mousePressed(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            inputHandler.mouseReleased(e);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            inputHandler.mouseEntered(e);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            inputHandler.mouseEntered(e);
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            inputHandler.mouseWheelMoved(e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            inputHandler.mouseDragged(e);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            inputHandler.mouseMoved(e);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            inputHandler.keyDown(e);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            inputHandler.keyUp(e);
        }
    }
}
