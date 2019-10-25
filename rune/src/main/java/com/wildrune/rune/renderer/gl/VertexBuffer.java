package com.wildrune.rune.renderer.gl;

import com.wildrune.rune.renderer.Color;
import com.wildrune.rune.renderer.IBindable;
import com.wildrune.rune.renderer.IDisposable;
import com.wildrune.rune.renderer.jogl.JoglEs2Context;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author Mark van der Wal
 * @since 31/01/18
 */
public class VertexBuffer extends JoglEs2Context implements IBindable, IDisposable {

    private final static Logger LOGGER = LogManager.getLogger(VertexBuffer.class);

    private final VertexDescriptor vertexDescriptor;
    private FloatBuffer directNativeBuffer;

    private int id;
    private int usage;
    private int bufferSizeInBytes;

    private VertexBuffer(int id, int vertexCount, int usage, VertexAttribute... attributes) {
        this.vertexDescriptor = new VertexDescriptor(attributes);
        this.id = id;
        this.usage = usage;
        this.bufferSizeInBytes = vertexCount * vertexDescriptor.getSizeInBytes();

        directNativeBuffer = ByteBuffer.allocateDirect(bufferSizeInBytes)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
    }

    /**
     * Creates a vertex buffer with specified vertex attributes
     *
     * @param gl          the opengl context
     * @param isStatic    determines if the buffer is used for static or dynamic usage
     * @param vertexCount the size in vertices
     * @return the freshly created vertex buffer
     */
    public static VertexBuffer createVertexBuffer(GL2ES2 gl, boolean isStatic, int vertexCount,
                                                  VertexAttribute... attributes) {
        if (attributes.length <= 0) {
            LOGGER.error("No vertex attributes specified!");
        }

        int[] intPtr = new int[1];
        gl.glGenBuffers(1, intPtr, 0);
        int id = intPtr[0];
        int usage = isStatic ? gl.GL_STATIC_DRAW : gl.GL_DYNAMIC_DRAW;

        VertexBuffer buffer = new VertexBuffer(id, vertexCount, usage, attributes);
        buffer.setGL(gl);

        buffer.bind();
        buffer.reset();
        buffer.unbind();

        return buffer;
    }

    public void reset() {
        clear();
        gl.glBufferData(gl.GL_ARRAY_BUFFER, bufferSizeInBytes, null, usage);
    }

    public void clear() {
        directNativeBuffer.clear();
    }

    public void render(int type) {
        directNativeBuffer.flip();
        if (directNativeBuffer.hasRemaining()) {
            int remaining = directNativeBuffer.remaining();
            gl.glBufferSubData(gl.GL_ARRAY_BUFFER, 0, remaining * Buffers.SIZEOF_FLOAT, directNativeBuffer);

            int verticesToDraw = remaining / vertexDescriptor.getComponentsSize();
            gl.glDrawArrays(type, 0, verticesToDraw);
        }
        directNativeBuffer.clear();
    }

    /**
     * check if the buffer can still hold vertexCount amount of vertices
     *
     * @param vertexCount the amount of vertices
     * @return true if the amount of vertices passed still fit into this buffer
     */
    public boolean doVerticesFit(int vertexCount) {
        int verticesInBytes = vertexCount * vertexDescriptor.getSizeInBytes();
        return verticesInBytes <= directNativeBuffer.remaining() * Buffers.SIZEOF_FLOAT;
    }

    public void putVector2(Vector2f data) {
        putVector2(data.x, data.y);
    }

    public void putVector2(float x, float y) {
        directNativeBuffer.put(x);
        directNativeBuffer.put(y);
    }

    public void putVector3(Vector3f data) {
        putVector3(data.x, data.y, data.z);
    }

    public void putVector3(float x, float y, float z) {
        directNativeBuffer.put(x);
        directNativeBuffer.put(y);
        directNativeBuffer.put(z);
    }

    public void putColorRGB(Color data) {
        directNativeBuffer.put(data.r);
        directNativeBuffer.put(data.g);
        directNativeBuffer.put(data.b);
    }

    public void putColorRGBA(Color data) {
        directNativeBuffer.put(data.r);
        directNativeBuffer.put(data.g);
        directNativeBuffer.put(data.b);
        directNativeBuffer.put(data.a);
    }

    public void bind(ShaderProgram shader) {
        bind();
        for (VertexAttribute attribute : vertexDescriptor) {
            String identifier = attribute.getIdentifier();
            shader.setVertexAttribute(identifier, attribute.getComponentsSize(),
                    attribute.getType(), attribute.isNormalized(), vertexDescriptor.getSizeInBytes(),
                    attribute.getOffset());
            shader.enableVertexAttribute(identifier);
        }
    }

    public void unbind(ShaderProgram shader) {
        for (VertexAttribute attribute : vertexDescriptor) {
            shader.disableVertexAttribute(attribute.getIdentifier());
        }
        unbind();
    }

    @Override
    public void bind() {
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, id);
    }

    @Override
    public void unbind() {
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void dispose() {
        directNativeBuffer.clear();
        directNativeBuffer = null;
        if (id > 0) {
            unbind();
            int[] intPtr = new int[]{id};
            gl.glDeleteBuffers(1, intPtr, 0);
        }
    }
}
