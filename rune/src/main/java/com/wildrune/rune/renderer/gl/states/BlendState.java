package com.wildrune.rune.renderer.gl.states;

import com.jogamp.opengl.GL2ES2;

/**
 * @author Mark "Wilds" van der Wal
 * @since 28-2-2018
 */
public class BlendState {

    public static final BlendState Opaque;
    public static final BlendState AlphaBlend;
    public static final BlendState Additive;
    public static final BlendState Multiplicative;
    public static final BlendState NonPreMultiplied;

    public static final int ENABLE_HANDLE = GL2ES2.GL_BLEND;

    static {
        Opaque = new BlendState(GL2ES2.GL_ONE, GL2ES2.GL_ZERO);
        AlphaBlend = new BlendState(GL2ES2.GL_ONE, GL2ES2.GL_SRC_ALPHA);
        Additive = new BlendState(GL2ES2.GL_SRC_ALPHA, GL2ES2.GL_ONE);
        Multiplicative = new BlendState(GL2ES2.GL_DST_COLOR, GL2ES2.GL_ZERO);
        NonPreMultiplied = new BlendState(GL2ES2.GL_SRC_ALPHA, GL2ES2.GL_ONE_MINUS_SRC_ALPHA);
    }

    private int srcBlend;
    private int dstBlend;

    private BlendState(int src, int dst) {
        srcBlend = src;
        dstBlend = dst;
    }

    public static BlendState getCurrentState(GL2ES2 gl) {
        int[] tempSrc = {0};
        int[] tempDst = {0};

        gl.glGetIntegerv(gl.GL_BLEND_SRC_ALPHA, tempSrc, 0);
        gl.glGetIntegerv(gl.GL_BLEND_DST_ALPHA, tempDst, 0);

        return new BlendState(tempSrc[0], tempDst[0]);
    }

    public static void activateState(GL2ES2 gl, BlendState state) {
        gl.glBlendFunc(state.srcBlend, state.dstBlend);
    }
}
