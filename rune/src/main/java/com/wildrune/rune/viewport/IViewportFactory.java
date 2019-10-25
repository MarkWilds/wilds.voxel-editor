package com.wildrune.rune.viewport;

import com.wildrune.rune.viewport.exceptions.ViewportException;

/**
 * @author Mark van der Wal
 * @since 20/01/18
 * <p>
 * Manages OpenGL context and functions as a viewport factory
 * <p>
 * The context manager uses a factory method to create viewports.
 * It can be used to create one GLContext that will be shared among all other viewports
 * to allow resource sharing.
 */
public interface IViewportFactory {

    /**
     * Initializes the context manager
     */
    void initialize() throws ViewportException;

    /**
     * Returns an implementation of IViewport
     *
     * @return an implementation of the IViewport
     * @throws ViewportException when viewport creation goes wrong, throw a ViewportException
     */
    IViewport createViewport() throws ViewportException;
}
