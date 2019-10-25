package com.wildrune.rune.renderer.batch;

import com.wildrune.rune.geometry.Box;
import com.wildrune.rune.renderer.Color;
import com.wildrune.rune.renderer.IDisposable;
import com.wildrune.rune.renderer.IRenderer;
import com.wildrune.rune.renderer.gl.ShaderProgram;
import com.wildrune.rune.renderer.gl.VertexAttribute;
import com.wildrune.rune.renderer.gl.VertexBuffer;
import com.wildrune.rune.renderer.gl.states.CullingState;
import com.wildrune.rune.util.Maths;

import com.jogamp.opengl.GL2ES2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * @author Mark "Wilds" van der Wal
 * @since 28-2-2018
 */
public class PrimitiveBatch implements IDisposable {

    private static final Logger LOGGER = LogManager.getLogger(PrimitiveBatch.class);

    private final int lineVertexCount = 16384;
    private final int polygonVertexCount = 16384;
    private final IRenderer<GL2ES2> renderer;

    // temps
    private final Vector3f[] cubePositions = new Vector3f[]{
            // front
            new Vector3f(-0.5f, -0.5f, -0.5f), new Vector3f(-0.5f, 0.5f, -0.5f),
            new Vector3f(0.5f, 0.5f, -0.5f), new Vector3f(0.5f, -0.5f, -0.5f),
            // back
            new Vector3f(0.5f, -0.5f, 0.5f), new Vector3f(0.5f, 0.5f, 0.5f),
            new Vector3f(-0.5f, 0.5f, 0.5f), new Vector3f(-0.5f, -0.5f, 0.5f)
    };
    private final short[][] cubeFaceIndices = new short[][]{

            {0, 1, 2, 3}, // front
            {4, 5, 6, 7}, // back
            {7, 6, 1, 0}, // left
            {3, 2, 5, 4}, // right
            {7, 0, 3, 4}, // bottom
            {1, 6, 5, 2} // top
    };
    private ShaderProgram shaderDefault;
    private VertexBuffer triangleBuffer;
    private VertexBuffer lineBuffer;
    private Color currentColor;
    private boolean beginEndPair;
    private Vector3f[] quadVertices = new Vector3f[]{
            new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f()
    };
    private Vector3f[] circleVertices = new Vector3f[]{
            new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f(),
            new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f(),
            new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f(),
            new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f(),
            new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f(),
            new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f(),
            new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f()
    };
    private String defaultVertShader = "attribute vec3 " + ShaderProgram.POSITION_ATTRIBUTE + ";" +
            "attribute vec4 " + ShaderProgram.COLOR4_ATTRIBUTE + ";" +
            "uniform mat4 " + ShaderProgram.VIEW_MATRIX_UNIFORM + ";" +
            "uniform mat4 " + ShaderProgram.PROJECTION_MATRIX_UNIFORM + ";" +
            "varying vec4 color" + ";" +
            "void main()" +
            "{" +
            "gl_Position = " + ShaderProgram.PROJECTION_MATRIX_UNIFORM +
            " * " + ShaderProgram.VIEW_MATRIX_UNIFORM +
            " * vec4(" + ShaderProgram.POSITION_ATTRIBUTE + ", 1);" +
            "color = " + ShaderProgram.COLOR4_ATTRIBUTE + ";" +
            "}";
    private String defaultFragShader = "precision mediump float;" +
            "varying vec4 color;" +
            "void main()" +
            "{" +
            "gl_FragColor = color;" +
            "}";

    public PrimitiveBatch(IRenderer renderer) {
        this.renderer = renderer;
        this.currentColor = Color.WHITE;

        // init circle
        int length = circleVertices.length;
        float step = (float) (Math.PI * 2 / length);
        for (int i = 0; i < length; i++) {
            Vector3f pos = circleVertices[i];
            pos.x = (float) Math.cos(Math.PI * 2 - i * step);
            pos.y = (float) Math.sin(Math.PI * 2 - i * step);
        }
    }

    public void create() {
        GL2ES2 gl = renderer.getGL();
        shaderDefault = ShaderProgram.createShaderProgram(gl, defaultVertShader, defaultFragShader);
        triangleBuffer = VertexBuffer.createVertexBuffer(gl, false, polygonVertexCount,
                VertexAttribute.POSITION, VertexAttribute.COLOR4);
        lineBuffer = VertexBuffer.createVertexBuffer(gl, false, lineVertexCount,
                VertexAttribute.POSITION, VertexAttribute.COLOR4);
    }

    @Override
    public void dispose() {
        triangleBuffer.dispose();
        lineBuffer.dispose();
        shaderDefault.dispose();
    }

    public void begin(Matrix4f modelView, Matrix4f projection) {
        if (beginEndPair) {
            LOGGER.warn("Cannot nest begin calls on a single batch!");
            return;
        }

        beginEndPair = true;

        renderer.pushCullingState(CullingState.CullClockwise);

        shaderDefault.bind();
        shaderDefault.setUniformf(ShaderProgram.VIEW_MATRIX_UNIFORM, modelView);
        shaderDefault.setUniformf(ShaderProgram.PROJECTION_MATRIX_UNIFORM, projection);
    }

    public void end() {
        if (isBeginCalled()) {
            flushBatch(triangleBuffer, GL2ES2.GL_TRIANGLES);
            flushBatch(lineBuffer, GL2ES2.GL_LINES);

            renderer.popCullingState();
            shaderDefault.unbind();
        }

        beginEndPair = false;
    }

    private void drawBox(float x, float y, float z, float xSize, float ySize, float zSize, DrawType drawType) {
        int faceCount = drawType == DrawType.FILLED ? 6 : 4;

        for (int i = 0; i < faceCount; i++) {
            short[] indices = cubeFaceIndices[i];

            for (int v = 0; v < 4; v++) {
                Vector3f pos = cubePositions[indices[v]];
                quadVertices[v].set(x, y, z).add(pos.x * xSize, pos.y * ySize, pos.z * zSize);
            }

            if (drawType == DrawType.FILLED) {
                fillPolygon(quadVertices);
            } else {
                polygon(quadVertices);
            }
        }
    }

    public void fillBox(float x, float y, float z, float sizeX, float sizeY, float sizeZ) {
        drawBox(x, y, z,
                sizeX, sizeY, sizeZ, DrawType.FILLED);
    }

    public void box(float x, float y, float z, float sizeX, float sizeY, float sizeZ) {
        drawBox(x, y, z,
                sizeX, sizeY, sizeZ, DrawType.WIREFRAME);
    }

    public void fillBox(Box box) {
        Vector3f center = box.getCenter();
        Vector3f min = box.getMin();
        Vector3f max = box.getMax();
        drawBox(center.x, center.y, center.z,
                max.x - min.x, max.y - min.y, max.z - min.z, DrawType.FILLED);
    }

    public void box(Box box) {
        Vector3f center = box.getCenter();
        Vector3f min = box.getMin();
        Vector3f max = box.getMax();
        drawBox(center.x, center.y, center.z,
                max.x - min.x, max.y - min.y, max.z - min.z, DrawType.WIREFRAME);
    }

    public void fillCube(float x, float y, float z, float size) {
        drawBox(x, y, z,
                size, size, size, DrawType.FILLED);
    }

    public void cube(float x, float y, float z, float size) {
        drawBox(x, y, z,
                size, size, size, DrawType.WIREFRAME);
    }

    public void fillRect(Vector3f planeNormal, float x, float y, float z, float width, float height) {
        drawRect(planeNormal, x, y, z, width, height, DrawType.FILLED);
    }

    public void rect(Vector3f planeNormal, float x, float y, float z, float width, float height) {
        drawRect(planeNormal, x, y, z, width, height, DrawType.WIREFRAME);
    }

    private void drawRect(Vector3f planeNormal, float x, float y, float z, float width, float height, DrawType drawType) {
        fillQuadVerticesRect(planeNormal, x, y, z, width, height);

        if (drawType == DrawType.WIREFRAME) {
            polygon(quadVertices);
        } else if (drawType == DrawType.FILLED) {
            fillPolygon(quadVertices);
        }

    }

    public void fillCircle(Vector3f position, float radius) {
        for (int i = 0; i < circleVertices.length; i++) {
            circleVertices[i].x = position.x + circleVertices[i].x * radius;
            circleVertices[i].y = position.y + circleVertices[i].y * radius;
        }

        fillPolygon(circleVertices);

        // inverse
        for (int i = 0; i < circleVertices.length; i++) {
            circleVertices[i].x = (circleVertices[i].x - position.x) / radius;
            circleVertices[i].y = (circleVertices[i].y - position.y) / radius;
        }
    }

    public void circle(Vector3f position, float radius) {
        for (int i = 0; i < circleVertices.length; i++) {
            circleVertices[i].x = position.x + circleVertices[i].x * radius;
            circleVertices[i].y = position.y + circleVertices[i].y * radius;
        }

        polygon(circleVertices);

        // inverse
        for (int i = 0; i < circleVertices.length; i++) {
            circleVertices[i].x = (circleVertices[i].x - position.x) / radius;
            circleVertices[i].y = (circleVertices[i].y - position.y) / radius;
        }
    }

    public void fillPolygon(Vector3f... points) {
        if (points.length < 3) {
            LOGGER.warn("A polygon cant have less than 3 vertices!");
            return;
        }

        int pointSize = points.length;
        if (doBatch(pointSize + 2, GL2ES2.GL_TRIANGLES, triangleBuffer)) {

            Vector3f startPoint = points[0];
            for (int i = 0; i < pointSize - 2; i++) {

                triangleBuffer.putVector3(startPoint);
                triangleBuffer.putColorRGBA(currentColor);

                triangleBuffer.putVector3(points[i + 1]);
                triangleBuffer.putColorRGBA(currentColor);

                triangleBuffer.putVector3(points[i + 2]);
                triangleBuffer.putColorRGBA(currentColor);
            }
        }
    }

    public void polygon(Vector3f... points) {
        if (points.length < 3) {
            LOGGER.warn("A polygon cant have less than 3 vertices!");
            return;
        }

        int pointSize = points.length;
        if (doBatch(pointSize * 2, GL2ES2.GL_LINES, lineBuffer)) {
            for (int i = 0; i < pointSize; i++) {
                Vector3f from = points[i];
                Vector3f to = points[(i + 1) % pointSize];

                line(from, to);
            }
        }
    }

    public void line(float x, float y, float z, float x2, float y2, float z2) {
        if (doBatch(2, GL2ES2.GL_LINES, lineBuffer)) {
            lineBuffer.putVector3(x, y, z);
            lineBuffer.putColorRGBA(currentColor);
            lineBuffer.putVector3(x2, y2, z2);
            lineBuffer.putColorRGBA(currentColor);
        }
    }

    public void line(Vector3f from, Vector3f to) {
        line(from.x, from.y, from.z, to.x, to.y, to.z);
    }

    public void color(Color color) {
        currentColor = color;
    }

    private boolean isBeginCalled() {
        if (!beginEndPair) {
            LOGGER.warn("Begin must be called before trying to render anything!");
            return false;
        }

        return true;
    }

    private boolean doBatch(int vertexCount, int type, VertexBuffer buffer) {
        boolean canBatch = isBeginCalled();
        if (canBatch) {
            if (!buffer.doVerticesFit(vertexCount)) {
                flushBatch(buffer, type);
            }

            return true;
        }

        return false;
    }

    private void flushBatch(VertexBuffer buffer, int primitive) {
        buffer.bind(shaderDefault);
        buffer.render(primitive);
        buffer.unbind(shaderDefault);
    }

    private void fillQuadVerticesRect(Vector3f planeNormal, float x, float y, float z, float width, float height) {
        Vector3f xAxis = Maths.getHorizontalAlignedAxis(planeNormal);
        Vector3f yAxis = Maths.getVerticalAlignedAxis(planeNormal);

        float halfWidth = width / 2;
        float halfHeight = height / 2;

        // create local vectors used to create quad on plane
        quadVertices[0].set(xAxis.x * -halfWidth, xAxis.y * -halfWidth, xAxis.z * -halfWidth)
                .add(yAxis.x * halfHeight, yAxis.y * halfHeight, yAxis.z * halfHeight);
        quadVertices[1].set(xAxis.x * halfWidth, xAxis.y * halfWidth, xAxis.z * halfWidth)
                .add(yAxis.x * halfHeight, yAxis.y * halfHeight, yAxis.z * halfHeight);

        quadVertices[2].set(quadVertices[0]).mul(-1);
        quadVertices[3].set(quadVertices[1]).mul(-1);

        // translate
        quadVertices[0].add(x, y, z);
        quadVertices[1].add(x, y, z);
        quadVertices[2].add(x, y, z);
        quadVertices[3].add(x, y, z);
    }

    private enum DrawType {
        WIREFRAME,
        FILLED
    }
}
