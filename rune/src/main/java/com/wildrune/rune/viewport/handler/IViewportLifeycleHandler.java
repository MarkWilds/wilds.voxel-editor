package com.wildrune.rune.viewport.handler;

/**
 * @author Mark "Wilds" van der Wal
 * @since 23-1-2018
 */
public interface IViewportLifeycleHandler {
    void create();

    void dispose();

    void update(float deltaTime);

    void render();
}
