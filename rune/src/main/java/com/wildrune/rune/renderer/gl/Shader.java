package com.wildrune.rune.renderer.gl;

import com.wildrune.rune.renderer.IDisposable;
import com.wildrune.rune.renderer.jogl.JoglEs2Context;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Mark "Wilds" van der Wal
 * @since 28-1-2018
 */
public class Shader extends JoglEs2Context implements IDisposable {
    private final static Logger LOGGER = LogManager.getLogger(Shader.class);

    private ShaderType type;

    private int id;

    private Shader(ShaderType type, int id) {
        this.type = type;
        this.id = id;
    }

    public static Shader createShader(GL2ES2 gl, ShaderType type, String source) throws GLException {
        int shaderId = gl.glCreateShader(type.getIdentifier());
        if (shaderId == 0) {
            throw new GLException(String.format("Could not create OpenGL %s shader resource", type.toString()));
        }

        Shader shader = new Shader(type, shaderId);
        shader.setGL(gl);
        shader.compileSource(source);
        if (!shader.isCompiled()) {
            String infoLog = shader.getInfoLog();
            shader.dispose();
            throw new GLException(String.format("%s shader failed to compile!\n%s", shader.type, infoLog));
        }

        return shader;
    }

    private void compileSource(String source) {
        final int[] sourceLength = new int[]{source.length()};
        final String[] shaderSource = new String[]{source};
        gl.glShaderSource(id, 1, shaderSource, sourceLength, 0);
        gl.glCompileShader(id);
    }

    private String getInfoLog() {
        final int[] logLength = new int[1];
        gl.glGetShaderiv(id, gl.GL_INFO_LOG_LENGTH, logLength, 0);

        byte[] log = new byte[logLength[0]];
        gl.glGetShaderInfoLog(id, logLength[0], (int[]) null, 0, log, 0);

        return new String(log);
    }

    private boolean isCompiled() {
        final int[] compileStatus = new int[1];
        gl.glGetShaderiv(id, gl.GL_COMPILE_STATUS, compileStatus, 0);
        return compileStatus[0] == gl.GL_TRUE;
    }

    public int getId() {
        return id;
    }

    @Override
    public void dispose() {
        if (id != 0) {
            gl.glDeleteShader(id);
        }
    }
}
