package com.wildrune.rune.renderer;

import com.wildrune.rune.renderer.gl.states.BlendState;
import com.wildrune.rune.renderer.gl.states.CullingState;
import com.wildrune.rune.renderer.gl.states.RasterizerState;

import com.jogamp.opengl.GL;

/**
 * @author Mark "Wilds" van der Wal
 * @since 23-1-2018
 */
public interface IRenderer<T extends GL> {

    T getGL();

    void setGL(T gl);

    void initState();

    void setClearColor(Color color);

    void clear();

    void setViewport(int x, int y, int width, int height);

    void pushRasterizerState(RasterizerState state);

    void popRasterizerState();

    void pushCullingState(CullingState state);

    void popCullingState();

    void pushBlendState(BlendState state);

    void popBlendState();
}
