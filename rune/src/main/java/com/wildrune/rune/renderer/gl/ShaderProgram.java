package com.wildrune.rune.renderer.gl;

import com.wildrune.rune.renderer.IBindable;
import com.wildrune.rune.renderer.IDisposable;
import com.wildrune.rune.renderer.jogl.JoglEs2Context;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mark "Wilds" van der Wal
 * @since 28-1-2018
 */
public class ShaderProgram extends JoglEs2Context implements IBindable, IDisposable {

    private final static Logger LOGGER = LogManager.getLogger(ShaderProgram.class);

    public final static String POSITION_ATTRIBUTE = "a_position";
    public final static String COLOR3_ATTRIBUTE = "a_color3";
    public final static String COLOR4_ATTRIBUTE = "a_color4";
    public final static String NORMAL_ATTRIBUTE = "a_normal";
    public final static String TEXCOORD0_ATTRIBUTE = "a_texCoord0";
    public final static String TEXCOORDAXIS_ATTRIBUTE = "a_texCoordAxis";

    public final static String TEXUNIT0_UNIFORM = "u_texUnit0";
    public final static String VIEW_MATRIX_UNIFORM = "u_viewMatrix";
    public final static String PROJECTION_MATRIX_UNIFORM = "u_projMatrix";

    private final static FloatBuffer matrix3Buffer = ByteBuffer.allocateDirect(9)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer();

    private final static FloatBuffer matrix4Buffer = ByteBuffer.allocateDirect(16)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer();

    private final Map<String, Integer> attributeLocationMap;
    private final Map<String, Integer> uniformLocationMap;
    private int id;

    private ShaderProgram(int id) {
        attributeLocationMap = new HashMap<>();
        uniformLocationMap = new HashMap<>();
        this.id = id;
    }

    public static ShaderProgram createShaderProgram(GL2ES2 gl, String vertex, String fragment) throws GLException {
        Shader vertexShader = Shader.createShader(gl, ShaderType.Vertex, vertex);
        Shader fragmentShader = Shader.createShader(gl, ShaderType.Fragment, fragment);

        // link program
        int id = gl.glCreateProgram();
        if (id == 0) {
            vertexShader.dispose();
            fragmentShader.dispose();
            throw new GLException("Could not create OpenGL shader program resource");
        }

        ShaderProgram shaderProgram = new ShaderProgram(id);
        shaderProgram.setGL(gl);

        shaderProgram.linkShaders(vertexShader, fragmentShader);

        if (!shaderProgram.isLinked() && shaderProgram.isValid()) {
            String infoLog = shaderProgram.getInfoLog();
            shaderProgram.dispose();
            vertexShader.dispose();
            fragmentShader.dispose();
            throw new GLException(String.format("Shader program failed to compile!\n%s", infoLog));
        }

        // fetch locations
        shaderProgram.fetchAttributeLocations();
        shaderProgram.fetchUniformLocations();

        // clean up the shaders as they are now linked into the program
        vertexShader.dispose();
        fragmentShader.dispose();

        return shaderProgram;
    }

    /**
     * This must be called before linking a shader program
     *
     * @param location  the attribute shader location to specify
     * @param attribute the attribute to bind to the shader
     */
    public void bindAttributeLocation(int location, String attribute) {
        gl.glBindAttribLocation(id, location, attribute);

        attributeLocationMap.put(attribute, location);
    }

    /**
     * Gets the attribute location from the shader.
     * Location is only known after linking the program.
     *
     * @param attribute the attribute to find the location for
     * @return the location of the attribute inside the shader
     */
    public int getAttributeLocation(String attribute) {
        if (!attributeLocationMap.containsKey(attribute)) {
            int location = gl.glGetAttribLocation(id, attribute);
            attributeLocationMap.put(attribute, location);
        }

        return attributeLocationMap.get(attribute);
    }

    /**
     * Gets the uniform location.
     * Location is only known after linking the program.
     *
     * @param uniform the uniform to get the location for
     * @return the uniform location inside the shader
     */
    public int getUniformLocation(String uniform) {
        if (!uniformLocationMap.containsKey(uniform)) {
            int location = gl.glGetUniformLocation(id, uniform);

            uniformLocationMap.put(uniform, location);
        }

        return uniformLocationMap.get(uniform);
    }

    public void setUniformf(String uniform, float value) {
        int location = getUniformLocation(uniform);
        if (location == -1) {
            LOGGER.debug(String.format("Uniform %s location not found!", uniform));
            return;
        }

        gl.glUniform1f(location, value);
    }

    public void setUniformf(String uniform, Vector2f vector2) {
        int location = getUniformLocation(uniform);
        if (location == -1) {
            LOGGER.debug(String.format("Uniform %s location not found!", uniform));
            return;
        }

        gl.glUniform2f(location, vector2.x, vector2.y);
    }

    public void setUniformf(String uniform, Vector3f vector3) {
        int location = getUniformLocation(uniform);
        if (location == -1) {
            LOGGER.debug(String.format("Uniform %s location not found!", uniform));
            return;
        }

        gl.glUniform3f(location, vector3.x, vector3.y, vector3.z);
    }

    public void setUniformf(String uniform, Vector4f vector4) {
        int location = getUniformLocation(uniform);
        if (location == -1) {
            LOGGER.debug(String.format("Uniform %s location not found!", uniform));
            return;
        }

        gl.glUniform4f(location, vector4.x, vector4.y, vector4.z, vector4.w);
    }

    public void setUniformf(String uniform, Matrix3f matrix3) {
        int location = getUniformLocation(uniform);
        if (location == -1) {
            LOGGER.debug(String.format("Uniform %s location not found!", uniform));
            return;
        }

        matrix3.get(matrix3Buffer);
        gl.glUniformMatrix3fv(location, 1, false, matrix3Buffer);
    }

    public void setUniformf(String uniform, Matrix4f matrix4) {
        int location = getUniformLocation(uniform);
        if (location == -1) {
            LOGGER.debug(String.format("Uniform %s location not found!", uniform));
            return;
        }

        matrix4.get(matrix4Buffer);
        gl.glUniformMatrix4fv(location, 1, false, matrix4Buffer);
    }

    /**
     * The VBO must be bound before calling this method
     */
    public void setVertexAttribute(String attribute, int componentSize, int bytesPerComponent,
                                   boolean normalized, int stride, int offset) {
        int location = getAttributeLocation(attribute);
        if (location == -1) {
            LOGGER.debug(String.format("Attribute %s location not found!", attribute));
            return;
        }

        gl.glVertexAttribPointer(location, componentSize, bytesPerComponent, normalized,
                stride, offset);
    }

    public void enableVertexAttribute(String attribute) {
        int location = getAttributeLocation(attribute);
        gl.glEnableVertexAttribArray(location);
    }

    public void disableVertexAttribute(String attribute) {
        int location = getAttributeLocation(attribute);
        gl.glDisableVertexAttribArray(location);
    }

    private void fetchUniformLocations() {

    }

    private void fetchAttributeLocations() {

    }

    /**
     * Links given vertex and fragment shader
     *
     * @param vertex   the vertex shader to link
     * @param fragment the fragment shader to link
     */
    private void linkShaders(Shader vertex, Shader fragment) {
        gl.glAttachShader(id, vertex.getId());
        gl.glAttachShader(id, fragment.getId());

        gl.glLinkProgram(id);
    }

    /**
     * Checks if the shader program is linked to the attached shaders
     *
     * @return true if linked, false otherwise
     */
    private boolean isLinked() {
        final int[] linkStatus = new int[1];
        gl.glGetProgramiv(id, gl.GL_LINK_STATUS, linkStatus, 0);

        return linkStatus[0] == gl.GL_TRUE;
    }

    /**
     * Checks if the shader program is valid with its attached shaders
     *
     * @return true if valid, false otherwise
     */
    private boolean isValid() {
        final int[] linkStatus = new int[1];
        gl.glGetProgramiv(id, gl.GL_VALIDATE_STATUS, linkStatus, 0);

        return linkStatus[0] == gl.GL_TRUE;
    }

    /**
     * Gets the information log for the shader program
     *
     * @return string containing shader information
     */
    private String getInfoLog() {
        final int[] logLength = new int[1];
        gl.glGetProgramiv(id, gl.GL_INFO_LOG_LENGTH, logLength, 0);

        byte[] log = new byte[logLength[0]];
        gl.glGetProgramInfoLog(id, logLength[0], (int[]) null, 0, log, 0);

        return new String(log);
    }

    @Override
    public void dispose() {
        if (id > 0) {
            gl.glDeleteProgram(id);
        }

        attributeLocationMap.clear();
        uniformLocationMap.clear();
    }

    @Override
    public void bind() {
        gl.glUseProgram(id);
    }

    @Override
    public void unbind() {
        gl.glUseProgram(0);
    }
}
