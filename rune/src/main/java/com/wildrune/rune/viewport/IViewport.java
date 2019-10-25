package com.wildrune.rune.viewport;

import com.wildrune.rune.renderer.IRenderer;
import com.wildrune.rune.viewport.camera.BaseCamera;
import com.wildrune.rune.viewport.handler.IViewportInputHandler;
import com.wildrune.rune.viewport.handler.IViewportLifeycleHandler;

import java.awt.Component;

/**
 * @author Mark van der Wal
 * @since 20/01/18
 * <p>
 * Interface for viewport subclasses that use AWT/Swing and OpenGL functionality
 * <p>
 * By having a getRoot method the contract specifies that the implementation
 * must be able to give a AWT/SWING component that implements this interface.
 */
public interface IViewport {

    /**
     * Get the root AWT/SWING component.
     *
     * @return the root component that the subclass will use to draw on.
     */
    Component getRoot();

    /**
     * Gets the renderer that holds the OpenGL state and drawing methods.
     *
     * @return IRenderer.
     */
    IRenderer getRenderer();

    /**
     * Gets the camera that is used by the viewport to show it's OpenGL image.
     *
     * @return BaseCamera implementation.
     */
    BaseCamera getCamera();

    /**
     * Changes the camera for the viewport.
     *
     * @param camera the camera to use for the viewport.
     */
    void setCamera(BaseCamera camera);

    /**
     * gets the viewport surface width
     *
     * @return width in whole numbers
     */
    int getViewportWidth();

    /**
     * gets the viewport surface height
     *
     * @return height in whole numbers
     */
    int getViewportHeight();

    /**
     * sets the viewport handler classes.
     *
     * @param handler the lifecycle handler.
     */
    void setLifecycleHandler(IViewportLifeycleHandler handler);

    /**
     * sets the viewport handler classes.
     *
     * @param handler the input handler.
     */
    void setInputHandler(IViewportInputHandler handler);
}
