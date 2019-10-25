package com.wildrune.rune.renderer.jogl;

import com.jogamp.opengl.GL2ES2;

/**
 * @author Mark "Wilds" van der Wal
 * @since 28-1-2018
 */
public abstract class JoglEs2Context {

    protected GL2ES2 gl;

    public void setGL(GL2ES2 gl) {
        this.gl = gl;
    }

    public GL2ES2 getGl() {
        return gl;
    }
}
