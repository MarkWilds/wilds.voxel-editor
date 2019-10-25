package com.wildrune.rune.renderer.gl;

import com.jogamp.opengl.*;

/**
 * @author Mark "Wilds" van der Wal
 * @since 28-1-2018
 */
public enum ShaderType {
    Vertex(GL2.GL_VERTEX_SHADER),
    Fragment(GL2.GL_FRAGMENT_SHADER);

    private int identifier;

    ShaderType(int openglId) {
        identifier = openglId;
    }

    public int getIdentifier() {
        return identifier;
    }
}
