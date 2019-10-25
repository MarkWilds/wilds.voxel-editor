package com.wildrune.rune.geometry;

import com.wildrune.rune.util.Maths;

import org.joml.Math;
import org.joml.Vector3f;

import java.util.Collection;

/**
 * Box is an axis aligned bounding box
 *
 * @author Mark "Wilds" van der Wal
 * @since 15/08/17
 */
public class Box {

    private final Vector3f min;
    private final Vector3f max;
    private final Vector3f center;

    public Box() {
        min = new Vector3f();
        max = new Vector3f();
        center = new Vector3f();
        reset();
    }

    public void reset() {
        min.x = Float.MAX_VALUE;
        min.y = Float.MAX_VALUE;
        min.z = Float.MAX_VALUE;
        max.x = -Float.MAX_VALUE;
        max.y = -Float.MAX_VALUE;
        max.z = -Float.MAX_VALUE;
    }

    public void grow(Vector3f vector) {
        grow(vector.x, vector.y, vector.z);
    }

    public void grow(float x, float y, float z) {
        if (x < min.x) min.x = x;
        if (y < min.y) min.y = y;
        if (z < min.z) min.z = z;
        if (x > max.x) max.x = x;
        if (y > max.y) max.y = y;
        if (z > max.z) max.z = z;
    }

    public void grow(Collection<Vector3f> vectors) {
        vectors.forEach(this::grow);
    }

    /**
     * reset the box and grow from all gives values
     *
     * @param values all values the box is going to grow from
     */
    public void regenerate(float... values) {
        if (values.length > 0 && values.length % 3 == 0) {
            reset();

            for (int i = 0; i < values.length; i += 3) {
                grow(values[i], values[i + 1], values[i + 2]);
            }
        }
    }

    public void regenerate(Vector3f... points) {
        if (points.length > 0) {
            reset();

            for (Vector3f point : points) {
                grow(point);
            }
        }
    }

    /***
     * If the box has volume in any of the axis it has 2D volume
     * @return true if it has 2D volume
     */
    public boolean hasVolume2D() {
        // check if the brush has valid bounds
        float absX = Math.abs(max.x - min.x);
        float absY = Math.abs(max.y - min.y);
        float absZ = Math.abs(max.z - min.z);

        return absX > Maths.EPSILON && absY > Maths.EPSILON ||
                absX > Maths.EPSILON && absZ > Maths.EPSILON ||
                absY > Maths.EPSILON && absZ > Maths.EPSILON;
    }

    /***
     *  If the box has volume in all of the axis it has 3D volume
     * @return true if it has 3D volume
     */
    public boolean hasVolume3D() {
        // check if the brush has valid bounds
        float absX = Math.abs(max.x - min.x);
        float absY = Math.abs(max.y - min.y);
        float absZ = Math.abs(max.z - min.z);

        return absX > Maths.EPSILON && absY > Maths.EPSILON && absZ > Maths.EPSILON;
    }

    public Vector3f getCenter() {
        return center.set(min).add(max).mul(0.5f);
    }

    public Vector3f getMin() {
        return min;
    }

    public Vector3f getMax() {
        return max;
    }
}
