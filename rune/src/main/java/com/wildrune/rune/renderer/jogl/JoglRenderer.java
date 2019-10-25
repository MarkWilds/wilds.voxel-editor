package com.wildrune.rune.renderer.jogl;

import com.wildrune.rune.renderer.Color;
import com.wildrune.rune.renderer.IRenderer;
import com.wildrune.rune.renderer.gl.states.BlendState;
import com.wildrune.rune.renderer.gl.states.CullingState;
import com.wildrune.rune.renderer.gl.states.RasterizerState;
import com.wildrune.rune.renderer.gl.states.SamplerState;

import com.jogamp.opengl.GL2ES2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Stack;
import java.util.function.Consumer;

/**
 * @author Mark "Wilds" van der Wal
 * @since 23-1-2018
 * <p>
 * todo research and fix occasionally crash
 */
public final class JoglRenderer implements IRenderer<GL2ES2> {

    private final static Logger LOGGER = LogManager.getLogger(JoglRenderer.class);

    private GL2ES2 gl;
    private Stack<RasterizerState> rasterizerStateStack;
    private Stack<CullingState> cullingStateStack;
    private Stack<BlendState> blendStateStack;

    public JoglRenderer() {
        rasterizerStateStack = new Stack<>();
        cullingStateStack = new Stack<>();
        blendStateStack = new Stack<>();
    }

    @Override
    public void initState() {
        gl.glEnable(SamplerState.ENABLE_HANDLE);
        gl.glEnable(RasterizerState.ENABLE_HANDLE);
        gl.glEnable(CullingState.ENABLE_HANDLE);
        gl.glEnable(BlendState.ENABLE_HANDLE);

        RasterizerState rasterizerState = RasterizerState.getCurrentState(gl);
        CullingState cullingState = CullingState.getCurrentState(gl);
        BlendState blendState = BlendState.getCurrentState(gl);

        rasterizerStateStack.push(rasterizerState);
        cullingStateStack.push(cullingState);
        blendStateStack.push(blendState);
    }

    @Override
    public void pushRasterizerState(RasterizerState state) {
        pushState(rasterizerStateStack, state, () -> RasterizerState.activateState(gl, state));
    }

    @Override
    public void popRasterizerState() {
        popState(rasterizerStateStack, state -> RasterizerState.activateState(gl, state));
    }

    @Override
    public void pushCullingState(CullingState state) {
        pushState(cullingStateStack, state, () -> CullingState.activateState(gl, state));
    }

    @Override
    public void popCullingState() {
        popState(cullingStateStack, state -> CullingState.activateState(gl, state));
    }

    @Override
    public void pushBlendState(BlendState state) {
        pushState(blendStateStack, state, () -> BlendState.activateState(gl, state));
    }

    @Override
    public void popBlendState() {
        popState(blendStateStack, state -> BlendState.activateState(gl, state));
    }

    private <E> void pushState(Stack<E> stack, E state, Runnable activateAction) {
        if (!stack.empty()) {
            E currentState = stack.peek();
            if (activateAction != null && !currentState.equals(state)) {
                activateAction.run();
            }
        }

        stack.push(state);
    }

    private <E> void popState(Stack<E> stack, Consumer<E> activateAction) {
        if (stack.size() >= 2) {
            E poppedState = stack.pop();
            E currentState = stack.peek();

            if (activateAction != null && !poppedState.equals(currentState)) {
                activateAction.accept(currentState);
            }
        }
    }

    @Override
    public void setViewport(int x, int y, int width, int height) {
        gl.glViewport(x, y, width, height);
    }

    @Override
    public void setClearColor(Color color) {
        gl.glClearColor(color.r, color.g, color.b, color.a);
    }

    @Override
    public void clear() {
        gl.glClear(RasterizerState.CLEAR_FLAG);
    }

    @Override
    public GL2ES2 getGL() {
        return gl;
    }

    @Override
    public void setGL(GL2ES2 gl) {
        this.gl = gl;
    }
}
