package com.wildrune.rune.renderer.gl.states;

import com.jogamp.opengl.GL2ES2;

/**
 * @author Mark "Wilds" van der Wal
 * @since 4-3-2018
 */
public class CullingState {

    public static final CullingState CullNone;
    public static final CullingState CullClockwise;
    public static final CullingState CullCounterClockwise;

    public static final int ENABLE_HANDLE = GL2ES2.GL_CULL_FACE;

    static {
        CullNone = new CullingState(GL2ES2.GL_CW, GL2ES2.GL_FRONT_AND_BACK);
        CullClockwise = new CullingState(GL2ES2.GL_CW, GL2ES2.GL_BACK);
        CullCounterClockwise = new CullingState(GL2ES2.GL_CCW, GL2ES2.GL_BACK);
    }

    private int cullWinding;
    private int cullSide;

    private CullingState(int winding, int side) {
        cullWinding = winding;
        cullSide = side;
    }

    public static CullingState getCurrentState(GL2ES2 gl) {
        int[] tempWinding = {0};
        int[] tempSide = {0};

        gl.glGetIntegerv(gl.GL_FRONT_FACE, tempWinding, 0);
        gl.glGetIntegerv(gl.GL_CULL_FACE_MODE, tempSide, 0);

        return new CullingState(tempWinding[0], tempSide[0]);
    }

    public static void activateState(GL2ES2 gl, CullingState state) {
        gl.glFrontFace(state.cullWinding);
        gl.glCullFace(state.cullSide);
    }
}
