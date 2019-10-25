package com.wildrune.rune.viewport.camera;

import com.wildrune.rune.geometry.Ray;

import org.joml.Matrix4f;

/**
 * @author Mark "Wilds" van der Wal
 * @since 20/08/17
 */
public class OrthoCamera extends BaseCamera {

    public OrthoCamera(float near, float far) {
        super(near, far);
    }

    @Override
    public Matrix4f getViewportProjectionMatrix() {
        viewportProjectionMatrix.identity();
        viewportProjectionMatrix.orthoLH(-viewportWidth / 2 * zoom, viewportWidth / 2 * zoom,
                -viewportHeight / 2 * zoom, viewportHeight / 2 * zoom, near, far, viewportProjectionMatrix);

        return viewportProjectionMatrix;
    }

    /**
     * Find the origin of the ray only, orthographic ray direction is always the camera look direction.
     */
    public void setScreenToRay(int x, int y, final Ray outRay) {
        outRay.getDirection().set(worldMatrix.m20(), worldMatrix.m21(), worldMatrix.m22());
        outRay.getDirection().normalize();

        outRay.getPosition().set(viewportToWorld(x, y));
    }
}
