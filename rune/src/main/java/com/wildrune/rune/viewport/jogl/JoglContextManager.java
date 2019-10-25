package com.wildrune.rune.viewport.jogl;

import com.wildrune.rune.renderer.jogl.JoglRenderer;
import com.wildrune.rune.viewport.IViewportFactory;
import com.wildrune.rune.viewport.exceptions.ViewportException;

import com.jogamp.opengl.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mark van der Wal
 * @since 19/01/18
 */
public final class JoglContextManager implements IViewportFactory {

    private static final Logger LOGGER = LogManager.getLogger(JoglContextManager.class);

    private final JoglRenderer renderer;
    private GLAutoDrawable sharedAutoDrawable;
    private boolean isInitialized;

    public JoglContextManager() {
        renderer = new JoglRenderer();
    }

    @Override
    public void initialize() throws ViewportException {
        if (!GLProfile.isAvailable(GLProfile.GL2ES2)) {
            throw new ViewportException("GL2ES2 profile could not be found!");
        }

        // create viewport
        GLProfile glProfile = GLProfile.getDefault();
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);
        glCapabilities.setDepthBits(24);
        glCapabilities.setStencilBits(8);
//        glCapabilities.setAlphaBits(8);
//        glCapabilities.setNumSamples(8);
//        glCapabilities.setSampleBuffers(true);

        sharedAutoDrawable = GLDrawableFactory
                .getFactory(glProfile)
                .createDummyAutoDrawable(null, true, glCapabilities, null);

        sharedAutoDrawable.addGLEventListener(new JoglLifecycleHandler());
        sharedAutoDrawable.display();

        isInitialized = true;
    }

    @Override
    public JoglRealtimeViewport createViewport() throws ViewportException {
        if (!isInitialized) {
            throw new ViewportException("JoglContextManager is not initialized!");
        }
        return new JoglRealtimeViewport(sharedAutoDrawable, renderer);
    }

    private class JoglLifecycleHandler implements GLEventListener {

        @Override
        public void init(GLAutoDrawable drawable) {
            GL2ES2 gl = drawable.getGL().getGL2ES2();

            gl.setSwapInterval(0);

            String openGLFormat = "OpenGL Canvas initialized\n%s\nGPU: %s\nGL: %s\nGLSL: %s";
            LOGGER.info(String.format(openGLFormat,
                    gl.glGetString(gl.GL_VENDOR),
                    gl.glGetString(gl.GL_RENDERER),
                    gl.glGetString(gl.GL_VERSION),
                    gl.glGetString(gl.GL_SHADING_LANGUAGE_VERSION)));

            GLCapabilitiesImmutable cap = drawable.getChosenGLCapabilities();
            String openGLCapabilities = "OpenGL capabilities\nHardware Accelerated: %s\nDepthBits: %s\nStencilBits: %s\nRGBA %s/%s/%s/%s\nSamples: %s";
            LOGGER.debug(String.format(openGLCapabilities,
                    cap.getHardwareAccelerated(),
                    cap.getDepthBits(),
                    cap.getStencilBits(),
                    cap.getRedBits(),
                    cap.getGreenBits(),
                    cap.getBlueBits(),
                    cap.getAlphaBits(),
                    cap.getNumSamples()));

            renderer.setGL(gl);
        }

        @Override
        public void dispose(GLAutoDrawable drawable) {
            GL2ES2 gl = drawable.getGL().getGL2ES2();
            renderer.setGL(gl);
            sharedAutoDrawable.destroy();
        }

        @Override
        public void display(GLAutoDrawable drawable) {
        }

        @Override
        public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        }
    }
}
