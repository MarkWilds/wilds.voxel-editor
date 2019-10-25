package com.wildrune.rune.viewport.camera;

import com.wildrune.rune.geometry.Plane;
import com.wildrune.rune.geometry.Ray;
import com.wildrune.rune.util.Maths;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;

/**
 * @author Mark "Wilds" van der Wal
 * @since 20/08/17
 *
 * todo fix math !! moveLocal should really be moveLocal...
 */
public abstract class BaseCamera {

    private final Ray tempRay = new Ray();

    protected Matrix4f viewportProjectionMatrix;
    protected Matrix4f screenProjectionMatrix;
    protected Matrix4f worldMatrix;
    protected Matrix4f viewMatrix;
    protected float zoom;
    protected float near, far;
    protected int viewportWidth, viewportHeight;

    BaseCamera(float _near, float _far) {
        zoom = 1.0f;
        near = _near;
        far = _far;
        viewportWidth = 1;
        viewportHeight = 1;
        screenProjectionMatrix = new Matrix4f();
        viewportProjectionMatrix = new Matrix4f();
        worldMatrix = new Matrix4f();
        viewMatrix = new Matrix4f();
    }

    /**
     * Abstract method for getting the ray for the concrete camera.
     *
     * @param x   mouse x screen coordinate
     * @param y   mouse y screen coordinate
     * @param ray ray
     */
    public abstract void setScreenToRay(int x, int y, final Ray ray);

    /**
     * transforms a screen coordinate to a world coordinate projected onto the given plane
     *
     * @param screen        the coordinates in screen space
     * @param plane         the plane to project onto
     * @param worldPosition the world position to fill
     * @return true if succeeded
     */
    public boolean setScreenToWorldOnPlane(Vector2i screen,
                                           final Plane plane, final Vector3f worldPosition) {
        setScreenToRay(screen.x, screen.y, tempRay);
        return Maths.rayIntersectsPlane(tempRay, plane, worldPosition);
    }

    /**
     * Always gives the world position on the near camera plane
     *
     * @param x Mouse x position
     * @param y Mouse y position
     * @return mouse coordinate in world space on the near camera plane
     */
    public Vector3f viewportToWorld(float x, float y) {
        Matrix4f projectionMatrix = getViewportProjectionMatrix();
        Matrix4f worldMatrix = getWorldMatrix();

        // screen to clip/ndc space to camera space
        Vector3f viewSpaceVector = new Vector3f(
                (2 * x / viewportWidth - 1) / projectionMatrix.m00(),
                (1 - 2 * y / viewportHeight) / projectionMatrix.m11(),
                1.0f);

        // camera space to world space
        return worldMatrix.transformPosition(viewSpaceVector);
    }

    /**
     * Converst world coordinates to screen coordinates
     * z is always 0
     *
     * @param position world space position
     * @return the screen coordinates
     */
    public Vector3f worldToViewport(Vector3f position) {
        Vector3f transformedPosition = new Vector3f(position);
        Matrix4f projectionMatrix = getViewportProjectionMatrix();
        Matrix4f viewMatrix = getViewMatrix();

        // world to camera space
        viewMatrix.transformPosition(transformedPosition);

        // camera to clip/ndc space
        projectionMatrix.transformPosition(transformedPosition);

        // from clip/ndc to screen space
        float x = (transformedPosition.x + 1) * viewportWidth / 2;
        float y = (transformedPosition.y + 1) * viewportHeight / 2;

        return new Vector3f(x, y, 0);
    }

    /**
     * Move by a given vector
     *
     * @param movement vector3f to move along
     */
    public void move(Vector3f movement) {
        move(movement.x, movement.y, movement.z);
    }

    public void move(float x, float y, float z) {
        worldMatrix.translateLocal(x, y, z);
    }

    public void moveLocal(Vector3f movement) {
        moveLocal(movement.x, movement.y, movement.z);
    }

    public void moveLocal(float x, float y, float z) {
        worldMatrix.translate(x, y, z);
    }

    public void setPosition(Vector3f position) {
        setPosition(position.x, position.y, position.z);
    }

    public void setPosition(float x, float y, float z) {
        worldMatrix.setTranslation(x, y, z);
    }

    /**
     * rotate the camera by axis angle
     *
     * @param angle in degrees
     * @param axis  to rotate about
     */
    public void rotate(float angle, Vector3f axis) {
        worldMatrix.rotate((float) Math.toRadians(angle), axis);
    }

    public void rotateWorld(float angle, Vector3f axis) {
        Vector3f translation = new Vector3f();
        AxisAngle4f rotation = new AxisAngle4f();
        worldMatrix.getTranslation(translation);
        worldMatrix.getRotation(rotation);

        worldMatrix.identity()
                .rotate((float) Math.toRadians(angle), axis.x, axis.y, axis.z)
                .rotate(rotation)
                .setTranslation(translation);
    }

    public abstract Matrix4f getViewportProjectionMatrix();

    public Matrix4f getScreenProjectionMatrix() {
        return new Matrix4f().ortho2DLH(0, viewportWidth, 0, viewportHeight);
    }

    public Matrix4f getWorldMatrix() {
        return worldMatrix;
    }

    public Matrix4f getViewMatrix() {
        viewMatrix.set(worldMatrix).invert();
        return viewMatrix;
    }

    public void setWindowDimensions(int width, int height) {
        viewportWidth = width;
        viewportHeight = height;
    }

    public void setClipDistance(float near, float far) {
        this.near = near;
        this.far = far;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public float getNear() {
        return near;
    }

    public float getFar() {
        return far;
    }
}
