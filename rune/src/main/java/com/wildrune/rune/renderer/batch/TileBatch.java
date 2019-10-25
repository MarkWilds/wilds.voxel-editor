package com.wildrune.rune.renderer.batch;

import com.wildrune.rune.renderer.IDisposable;
import com.wildrune.rune.renderer.IRenderer;
import com.wildrune.rune.renderer.gl.ShaderProgram;
import com.wildrune.rune.renderer.gl.Texture2D;
import com.wildrune.rune.renderer.gl.VertexAttribute;
import com.wildrune.rune.renderer.gl.VertexBuffer;
import com.wildrune.rune.renderer.gl.states.CullingState;

import com.jogamp.opengl.GL2ES2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * @author Mark "Wilds" van der Wal
 * @since 6-3-2018
 * <p>
 * todo: sort on texture, create intermediate structure
 */
public class TileBatch implements IDisposable {

    private static final Logger LOGGER = LogManager.getLogger(TileBatch.class);

    private final int decalVertexCount = 65536;
    private final IRenderer<GL2ES2> renderer;
    private final Vector3f lightVector = new Vector3f(-0.5f, -0.9f, 0.7f).normalize();
    private final Vector3f cubeFaceNormal = new Vector3f();

    private final Vector3f[] cubePositions = new Vector3f[]{
            // front
            new Vector3f(0, 0, 0), new Vector3f(0, 1, 0), new Vector3f(1, 1, 0), new Vector3f(1, 0, 0),
            // back
            new Vector3f(1, 0, 1), new Vector3f(1, 1, 1), new Vector3f(0, 1, 1), new Vector3f(0, 0, 1)
    };

    private final short[][] cubeFaceIndices = new short[][]{

            {0, 1, 2, 3}, // front
            {4, 5, 6, 7}, // back
            {7, 6, 1, 0}, // left
            {3, 2, 5, 4}, // right
            {7, 0, 3, 4}, // bottom
            {1, 6, 5, 2} // top
    };

    private final Vector2f[] axisTextureCoordinates = new Vector2f[]{
            new Vector2f(0, 1), new Vector2f(0, 0), new Vector2f(1, 0), new Vector2f(1, 1)
    };

    private final Vector3f[] tilePositions = new Vector3f[]{
            new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f()
    };

    private final Vector2f[] tileTextureCoordinates = new Vector2f[]{
            new Vector2f(), new Vector2f(), new Vector2f(), new Vector2f()
    };

    private final String tileVertShader = "attribute vec3 " + ShaderProgram.POSITION_ATTRIBUTE + ";" +
            "attribute vec2 " + ShaderProgram.TEXCOORD0_ATTRIBUTE + ";" +
            "attribute vec2 " + ShaderProgram.TEXCOORDAXIS_ATTRIBUTE + ";" +
            "attribute vec3 " + ShaderProgram.NORMAL_ATTRIBUTE + ";" +
            "uniform mat4 " + ShaderProgram.VIEW_MATRIX_UNIFORM + ";" +
            "uniform mat4 " + ShaderProgram.PROJECTION_MATRIX_UNIFORM + ";" +
            "varying vec2 texCoord;" +
            "varying vec2 texCoordAxis;" +
            "varying vec3 camNormal;" +
            "varying float distanceToVertex;" +

            "void main()" +
            "{" +
            "camNormal = " + ShaderProgram.NORMAL_ATTRIBUTE + ";" +
            "texCoord = " + ShaderProgram.TEXCOORD0_ATTRIBUTE + ";" +
            "texCoordAxis = " + ShaderProgram.TEXCOORDAXIS_ATTRIBUTE + ";" +
            "vec4 cameraSpaceVertex = " + ShaderProgram.VIEW_MATRIX_UNIFORM +
            " * vec4(" + ShaderProgram.POSITION_ATTRIBUTE + ", 1);" +
            "distanceToVertex = length(cameraSpaceVertex);" +

            "gl_Position = " + ShaderProgram.PROJECTION_MATRIX_UNIFORM + " * cameraSpaceVertex;" +
            "}";

    private final String tileFragShader = "precision mediump float;" +
            "varying vec2 texCoord;" +
            "varying vec2 texCoordAxis;" +
            "varying vec3 camNormal;" +
            "varying float distanceToVertex;" +
            "uniform sampler2D " + ShaderProgram.TEXUNIT0_UNIFORM + ";" +
            "uniform vec3 u_lightVector;" +
            "uniform float u_lineWidth;" +
            "uniform float u_tileSize;" +
            "uniform float u_gridEnabled;" +

            "void main()" +
            "{" +
            "vec2 st = texCoordAxis * u_tileSize;" +
            "vec2 bl = step(vec2(u_lineWidth), st);" +
            "vec2 tr = step(vec2(u_lineWidth), u_tileSize - st);" +

            "float distanceFactor = smoothstep(1024, 0, distanceToVertex);" +
            "float gridPixel = bl.x * bl.y * tr.x * tr.y * distanceFactor * u_gridEnabled;" +

            "vec4 rgbaColor = texture2D(" + ShaderProgram.TEXUNIT0_UNIFORM + ", texCoord);" +
            "float baseFactor = 0.5;" +
            "float lightFactor = baseFactor * max(0, dot(-u_lightVector, camNormal));" +
            "float gridFactor = 0.2 * gridPixel;" +

            "gl_FragColor = vec4(rgbaColor.rgb * (baseFactor + lightFactor + gridFactor), rgbaColor.a);" +
            "}";

    private ShaderProgram shaderDefault;
    private VertexBuffer tileBuffer;
    private boolean beginEndPair;

    public TileBatch(IRenderer<GL2ES2> renderer) {
        this.renderer = renderer;
    }

    public void create() {
        GL2ES2 gl = renderer.getGL();
        shaderDefault = ShaderProgram.createShaderProgram(gl, tileVertShader, tileFragShader);
        tileBuffer = VertexBuffer.createVertexBuffer(gl, false, decalVertexCount,
                VertexAttribute.POSITION, VertexAttribute.TEX_COORD0, VertexAttribute.TEX_COORD_AXIS, VertexAttribute.NORMAL);
    }

    @Override
    public void dispose() {
        shaderDefault.dispose();
        tileBuffer.dispose();
    }

    public void begin(Matrix4f modelView, Matrix4f projection, int tileSize, float lineWidth, boolean grid) {
        if (beginEndPair) {
            LOGGER.warn("Cannot nest begin calls on a single batch!");
            return;
        }

        beginEndPair = true;

        renderer.pushCullingState(CullingState.CullClockwise);

        shaderDefault.bind();
        shaderDefault.setUniformf("u_lightVector", lightVector);
        shaderDefault.setUniformf("u_lineWidth", lineWidth);
        shaderDefault.setUniformf("u_tileSize", tileSize);
        shaderDefault.setUniformf("u_gridEnabled", grid ? 1.0f : 0.0f);
        shaderDefault.setUniformf(ShaderProgram.VIEW_MATRIX_UNIFORM, modelView);
        shaderDefault.setUniformf(ShaderProgram.PROJECTION_MATRIX_UNIFORM, projection);
    }

    public void end() {
        if (isBeginCalled()) {
            flushBatch(tileBuffer, GL2ES2.GL_TRIANGLES);

            renderer.popCullingState();
            shaderDefault.unbind();
        }

        beginEndPair = false;
    }

    private boolean isBeginCalled() {
        if (!beginEndPair) {
            LOGGER.warn("Begin must be called before trying to render anything!");
            return false;
        }

        return true;
    }

    private void flushBatch(VertexBuffer buffer, int primitive) {
        buffer.bind(shaderDefault);
        buffer.render(primitive);
        buffer.unbind(shaderDefault);
    }

    public void tileAxisAligned(int tileX, int tileY, int tileZ, int tilesetX, int tilesetY, int tileSize,
                                int sideIndex, Texture2D texture2D) {
        if (texture2D == null) {
            LOGGER.error("drawTexture: Texture is null!");
            return;
        }

        if (isBeginCalled()) {
            int texWidth = texture2D.getWidth();
            int texHeight = texture2D.getHeight();
            int size = tilePositions.length;

            float srcTextCoordX = tilesetX * tileSize / (float) texWidth;
            float srcTextCoordY = tilesetY * tileSize / (float) texHeight;
            float srcTexCoordWidth = tileSize / (float) texWidth;
            float srcTexCoordHeight = tileSize / (float) texHeight;

            setFaceNormal(sideIndex);
            fillTileTexCoords(FlipAxis.None, srcTextCoordX, srcTextCoordY, srcTexCoordWidth, srcTexCoordHeight);
            fillTileVertices(sideIndex, tileX, tileY, tileZ, tileSize);

            if (!tileBuffer.doVerticesFit(size + 2)) {
                flushBatch(tileBuffer, GL2ES2.GL_TRIANGLES);
            }

            putTileVertices(0);
            putTileVertices(1);
            putTileVertices(2);

            putTileVertices(0);
            putTileVertices(2);
            putTileVertices(3);
        }
    }

    private void putTileVertices(int index) {
        tileBuffer.putVector3(tilePositions[index]);
        tileBuffer.putVector2(tileTextureCoordinates[index]);
        tileBuffer.putVector2(axisTextureCoordinates[index]);
        tileBuffer.putVector3(cubeFaceNormal);
    }

    private void setFaceNormal(int sideIndex) {
        switch (sideIndex) {
            case 0:
                cubeFaceNormal.set(0, 0, -1);
                break;
            case 1:
                cubeFaceNormal.set(0, 0, 1);
                break;
            case 2:
                cubeFaceNormal.set(-1, 0, 0);
                break;
            case 3:
                cubeFaceNormal.set(1, 0, 0);
                break;
            case 4:
                cubeFaceNormal.set(0, -1, 0);
                break;
            case 5:
                cubeFaceNormal.set(0, 1, 0);
                break;
        }
    }

    private void fillTileTexCoords(FlipAxis flip, float texPosX, float texPosY, float texWidth, float texHeight) {
        int flipValue = flip.ordinal();

        tileTextureCoordinates[flipValue].set(texPosX + axisTextureCoordinates[0].x * texWidth, texPosY + axisTextureCoordinates[0].y * texHeight);
        tileTextureCoordinates[1 ^ flipValue].set(texPosX + axisTextureCoordinates[1].x * texWidth, texPosY + axisTextureCoordinates[1].y * texHeight);
        tileTextureCoordinates[2 ^ flipValue].set(texPosX + axisTextureCoordinates[2].x * texWidth, texPosY + axisTextureCoordinates[2].y * texHeight);
        tileTextureCoordinates[3 ^ flipValue].set(texPosX + axisTextureCoordinates[3].x * texWidth, texPosY + axisTextureCoordinates[3].y * texHeight);
    }

    private void fillTileVertices(int sideIndex, float x, float y, float z, float tileSize) {
        short[] faceIndices = cubeFaceIndices[sideIndex];

        tilePositions[0].set(cubePositions[faceIndices[0]]);
        tilePositions[1].set(cubePositions[faceIndices[1]]);
        tilePositions[2].set(cubePositions[faceIndices[2]]);
        tilePositions[3].set(cubePositions[faceIndices[3]]);

        float worldX = x * tileSize;
        float worldY = y * tileSize;
        float worldZ = z * tileSize;

        tilePositions[0].mul(tileSize).add(worldX, worldY, worldZ);
        tilePositions[1].mul(tileSize).add(worldX, worldY, worldZ);
        tilePositions[2].mul(tileSize).add(worldX, worldY, worldZ);
        tilePositions[3].mul(tileSize).add(worldX, worldY, worldZ);
    }

    /**
     * Front face = Clockwise starting at lower left.
     * our texture coordinates are flipped by default to show textures correctly
     * because of OpenGL default texture coordinates:
     * 0,1 -- 1,1
     * |      |
     * 0,0 -- 1,0
     * <p>
     * if we had the opposite:
     * 0,0 -- 1,0
     * |      |
     * 0,1 -- 1,1
     * <p>
     * our FlipAxis would look as follows:
     * None = 0,
     * Horizontal = 1,
     * Vertical = 2,
     * Both = 3
     */
    public enum FlipAxis {
        None,
        Vertical,
        Both,
        Horizontal
    }
}
