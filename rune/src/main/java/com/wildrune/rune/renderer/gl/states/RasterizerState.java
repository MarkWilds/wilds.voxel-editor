package com.wildrune.rune.renderer.gl.states;

import com.jogamp.opengl.GL2ES2;

/**
 * @author Mark "Wilds" van der Wal
 * @since 4-3-2018
 */
public class RasterizerState {

    public static final RasterizerState DepthLess;
    public static final RasterizerState DepthAlways;
    public static final RasterizerState DepthNever;

    public static final int ENABLE_HANDLE = GL2ES2.GL_DEPTH_TEST;
    public static final int CLEAR_FLAG = GL2ES2.GL_COLOR_BUFFER_BIT | GL2ES2.GL_DEPTH_BUFFER_BIT;

    static {
        DepthLess = new RasterizerState(GL2ES2.GL_LESS, 0, 1, true);
        DepthAlways = new RasterizerState(GL2ES2.GL_ALWAYS, 0, 1, true);
        DepthNever = new RasterizerState(GL2ES2.GL_NEVER, 0, 1, false);
    }

    private boolean depthMask;
    private int depthFunc;
    private float rangeNear;
    private float rangeFar;

    public RasterizerState(int depth, float near, float far, boolean mask) {
        depthFunc = depth;
        rangeNear = near;
        rangeFar = far;
        depthMask = mask;
    }

    public static RasterizerState getCurrentState(GL2ES2 gl) {
        int[] tempFunc = {0};
        byte[] tempMask = {0};
        int[] tempRange = {0, 0};

        gl.glGetIntegerv(gl.GL_DEPTH_FUNC, tempFunc, 0);
        gl.glGetBooleanv(gl.GL_DEPTH_WRITEMASK, tempMask, 0);
        gl.glGetIntegerv(gl.GL_DEPTH_RANGE, tempRange, 0);


        return new RasterizerState(tempFunc[0], tempRange[0], tempRange[1], tempMask[0] > 0);
    }

    public static void activateState(GL2ES2 gl, RasterizerState state) {
        gl.glDepthFunc(state.depthFunc);
        gl.glDepthMask(state.depthMask);
        gl.glDepthRangef(state.rangeNear, state.rangeFar);
    }
}
