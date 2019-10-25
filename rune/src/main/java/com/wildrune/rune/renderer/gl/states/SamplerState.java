package com.wildrune.rune.renderer.gl.states;

import com.jogamp.opengl.GL2ES2;

/**
 * @author Mark "Wilds" van der Wal
 * @since 5-3-2018
 */
public class SamplerState {

    public final static SamplerState PointWrap;
    public final static SamplerState PointClamp;
    public final static SamplerState LinearWrap;
    public final static SamplerState LinearClamp;

    public final static int ENABLE_HANDLE = GL2ES2.GL_TEXTURE_2D;

    static {
        PointWrap = new SamplerState(GL2ES2.GL_NEAREST, GL2ES2.GL_NEAREST, GL2ES2.GL_REPEAT, GL2ES2.GL_REPEAT);
        PointClamp = new SamplerState(GL2ES2.GL_NEAREST, GL2ES2.GL_NEAREST, GL2ES2.GL_CLAMP_TO_EDGE, GL2ES2.GL_CLAMP_TO_EDGE);
        LinearWrap = new SamplerState(GL2ES2.GL_LINEAR, GL2ES2.GL_LINEAR, GL2ES2.GL_REPEAT, GL2ES2.GL_REPEAT);
        LinearClamp = new SamplerState(GL2ES2.GL_LINEAR, GL2ES2.GL_LINEAR, GL2ES2.GL_CLAMP_TO_EDGE, GL2ES2.GL_CLAMP_TO_EDGE);
    }

    // data members
    public int minFilter;
    public int maxFilter;
    public int wrapS;
    public int wrapT;

    public SamplerState(int min, int max, int s, int t) {
        minFilter = min;
        maxFilter = max;
        wrapS = s;
        wrapT = t;
    }


    public static void activateState(GL2ES2 gl, SamplerState state) {
        gl.glTexParameterf(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MIN_FILTER, state.minFilter);
        gl.glTexParameterf(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MAG_FILTER, state.maxFilter);
        gl.glTexParameterf(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_S, state.wrapS);
        gl.glTexParameterf(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_T, state.wrapT);
    }
}
