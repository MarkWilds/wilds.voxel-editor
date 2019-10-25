package com.wildrune.rune.viewport.camera;

import com.wildrune.rune.geometry.Ray;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * @author Mark "Wilds" van der Wal
 * @since 20/08/17
 */
public class PerspCamera extends BaseCamera {

    private float fieldOfView;

    public PerspCamera(float fov, float near, float far) {
        super(near, far);
        fieldOfView = fov;
    }

    @Override
    public Matrix4f getViewportProjectionMatrix() {
        viewportProjectionMatrix.identity();
        viewportProjectionMatrix.perspectiveLH((float) Math.toRadians(fieldOfView), viewportWidth / (float) viewportHeight, near, far);

        return viewportProjectionMatrix;
    }

    /**
     * Find the direction of the ray only, origin of the ray is always the camera position in perspective.
     */
    public void setScreenToRay(int x, int y, final Ray outRay) {
        worldMatrix.getTranslation(outRay.getPosition());
        Vector3f worldCoordinate = viewportToWorld(x, y);

        // get the correct direction in world space
        // relative to the camera position
        worldCoordinate.sub(outRay.getPosition(), outRay.getDirection());
        outRay.getDirection().normalize();
    }
}
