package com.wildrune.rune.util;

import com.wildrune.rune.geometry.Box;
import com.wildrune.rune.geometry.Plane;
import com.wildrune.rune.geometry.Ray;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.function.Function;

/**
 * @author Mark "Wilds" van der Wal
 * @since 17/08/17
 */
public final class Maths {

    public static final float EPSILON = 1e-5f;
    public static final float BIG_EPSILON = 1e-2f;
    public static final Vector3f ZERO = new Vector3f(0, 0, 0);
    public static final Vector3f ONE = new Vector3f(1, 1, 1);
    public static final Vector3f UNIT_X = new Vector3f(1, 0, 0);
    public static final Vector3f UNIT_Y = new Vector3f(0, 1, 0);
    public static final Vector3f UNIT_Z = new Vector3f(0, 0, 1);
    public static final Vector3f UNIT_NEG_X = new Vector3f(-1, 0, 0);
    public static final Vector3f UNIT_NEG_Y = new Vector3f(0, -1, 0);
    public static final Vector3f UNIT_NEG_Z = new Vector3f(0, 0, -1);

    public static final Matrix4f IDENTITY = new Matrix4f().identity();

    private static final Vector3i tileCoords = new Vector3i();
    private static final Vector2f hitExtends = new Vector2f();
    private static final Vector3f absNormal = new Vector3f();

    /**
     * From "Realtime collision detection" by Christer Ericson
     * Chapter 7 Spatial partitioning, page 324
     */
    public static void visitCellsOverlappedBySegment(Vector3f start, Vector3f end,
                                                     int cellSize, Function<Vector3i, Boolean> visit) {
        tileCoords.x = (int) Math.floor(start.x / cellSize);
        tileCoords.y = (int) Math.floor(start.y / cellSize);
        tileCoords.z = (int) Math.floor(start.z / cellSize);

        int endX = (int) Math.floor(end.x / cellSize);
        int endY = (int) Math.floor(end.y / cellSize);
        int endZ = (int) Math.floor(end.z / cellSize);

        int totalTiles = 1 + Math.abs(endX - tileCoords.x) + Math.abs(endY - tileCoords.y) + Math.abs(endZ - tileCoords.z);

        int incX = Float.compare(end.x, start.x);
        int incY = Float.compare(end.y, start.y);
        int incZ = Float.compare(end.z, start.z);

        float absDeltaX = Math.abs(end.x - start.x);
        float absDeltaY = Math.abs(end.y - start.y);
        float absDeltaZ = Math.abs(end.z - start.z);

        float deltaX = cellSize / absDeltaX;
        float deltaY = cellSize / absDeltaY;
        float deltaZ = cellSize / absDeltaZ;

        float minX = cellSize * tileCoords.x;
        float maxX = minX + cellSize;

        float minY = cellSize * tileCoords.y;
        float maxY = minY + cellSize;

        float minZ = cellSize * tileCoords.z;
        float maxZ = minZ + cellSize;

        float tX = (start.x > end.x ? start.x - minX : maxX - start.x) / absDeltaX;
        float tY = (start.y > end.y ? start.y - minY : maxY - start.y) / absDeltaY;
        float tZ = (start.z > end.z ? start.z - minZ : maxZ - start.z) / absDeltaZ;

        while (totalTiles > 0) {
            if (visit.apply(tileCoords)) {
                break;
            }
            totalTiles--;

            if (tX <= tY && tX <= tZ) {
                tX += deltaX;
                tileCoords.x += incX;
            } else if (tY <= tX && tY <= tZ) {
                tY += deltaY;
                tileCoords.y += incY;
            } else {
                tZ += deltaZ;
                tileCoords.z += incZ;
            }
        }
    }

    /**
     * Using slabs method to check for intersection between ray and box
     *
     * @param ray    the ray to check with
     * @param box    the box to check against
     * @param expand push box sides outwards if specified > 0 else inwards if < 0
     * @return true if succeeded
     */
    public static boolean rayIntersectsBox(final Ray ray, final Box box, float expand, Vector2f tVector) {
        Vector3f direction = ray.getDirection();
        Vector3f position = ray.getPosition();
        Vector3f min = box.getMin();
        Vector3f max = box.getMax();

        float reciprocalX = 1.0f / direction.x;
        float reciprocalY = 1.0f / direction.y;
        float reciprocalZ = 1.0f / direction.z;

        float t1 = (min.x - expand - position.x) * reciprocalX;
        float t2 = (max.x + expand - position.x) * reciprocalX;
        float t3 = (min.y - expand - position.y) * reciprocalY;
        float t4 = (max.y + expand - position.y) * reciprocalY;
        float t5 = (min.z - expand - position.z) * reciprocalZ;
        float t6 = (max.z + expand - position.z) * reciprocalZ;

        float tMin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
        float tMax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

        if (tMax < 0 || tMin > tMax) {
            tMin = 0.0f;
            return false;
        }

        if (tVector != null) {
            tVector.x = tMin;
            tVector.y = tMax;
        }

        return true;
    }

    public static boolean rayIntersectsBox(final Ray ray, final Box box) {
        return rayIntersectsBox(ray, box, 0.0f, null);
    }

    public static boolean rayIntersectsBox(final Ray ray, final Box box, Vector2f tVector) {
        return rayIntersectsBox(ray, box, 0.0f, tVector);
    }

    public static boolean rayIntersectsBox(final Ray ray, final Box box, Vector3f nearPoint, Vector3f farPoint) {
        if (Maths.rayIntersectsBox(ray, box, -Maths.BIG_EPSILON, hitExtends)) {
            Vector3f rayPosition = ray.getPosition();
            Vector3f rayDirection = ray.getDirection();

            farPoint.set(rayDirection).mul(hitExtends.y).add(rayPosition);
            if (hitExtends.x > 0) {
                nearPoint.set(rayDirection).mul(hitExtends.x).add(rayPosition);
            } else {
                nearPoint.set(rayPosition);
            }

            return true;
        }

        return false;
    }

    /**
     * Check for ray and plane intersection
     *
     * @param ray           the ray to check with
     * @param plane         the plane to check against
     * @param worldPosition the position in world coordinates to write into
     * @return true if succeeded
     */
    public static boolean rayIntersectsPlane(final Ray ray, final Plane plane,
                                             final Vector3f worldPosition) {

        // project direction onto normal of plane
        Vector3f normal = plane.getNormal();
        float directionDistanceToPlane = ray.getDirection().dot(normal);

        // ray is parallel to the plane and will never hit it.
        if (Math.abs(directionDistanceToPlane) < Maths.EPSILON) {
            return false;
        }

        float distanceToPlane = plane.distanceToPlane(ray.getPosition());
        float t = distanceToPlane / -directionDistanceToPlane;
        if (t < -EPSILON) {
            return false;
        }

        worldPosition.set(ray.getDirection()).mul(t).add(ray.getPosition());

        return true;
    }

    public static Vector3f getBoxFaceNormal(Box box, Vector3f hitPosition) {
        Vector3f min = box.getMin();
        Vector3f max = box.getMax();

        if (Math.abs(hitPosition.x - min.x) <= Maths.BIG_EPSILON) {
            return Maths.UNIT_NEG_X;
        } else if (Math.abs(hitPosition.x - max.x) <= Maths.BIG_EPSILON) {
            return Maths.UNIT_X;
        } else if (Math.abs(hitPosition.y - min.y) <= Maths.BIG_EPSILON) {
            return Maths.UNIT_NEG_Y;
        } else if (Math.abs(hitPosition.y - max.y) <= Maths.BIG_EPSILON) {
            return Maths.UNIT_Y;
        } else if (Math.abs(hitPosition.z - min.z) <= Maths.BIG_EPSILON) {
            return Maths.UNIT_NEG_Z;
        } else if (Math.abs(hitPosition.z - max.z) <= Maths.BIG_EPSILON) {
            return Maths.UNIT_Z;
        }

        return Maths.ZERO;
    }

    /**
     * get closest axis align normal based on passed in normal
     *
     * @param normal get axis aligned normal for this passed in normal
     * @return axis aligned normal
     */
    public static Vector3f getClosestAxisAlignedNormal(final Vector3f normal) {
        absNormal.set(normal);
        absNormal.absolute();
        return absNormal.x >= absNormal.y && absNormal.x >= absNormal.z
                ? absNormal.set(Maths.UNIT_X)
                : (absNormal.y >= absNormal.z)
                ? absNormal.set(Maths.UNIT_Y)
                : absNormal.set(Maths.UNIT_Z);
    }

    public static Vector3f getAbsHorizontalAxisAligned(Vector3f normal) {
        return getClosestAxisAlignedNormal(normal).equals(Maths.UNIT_X) ? Maths.UNIT_Z : Maths.UNIT_X;
    }

    public static Vector3f getAbsVerticalAxisAligned(Vector3f normal) {
        return getClosestAxisAlignedNormal(normal).equals(Maths.UNIT_Y) ? Maths.UNIT_Z : Maths.UNIT_Y;
    }

    /**
     * Only works if the input vector is axis aligned.
     * Based on the left handed coordinate system.
     */
    public static Vector3f getHorizontalAlignedAxis(Vector3f normal) {
        if (normal.equals(Maths.UNIT_NEG_X)) {
            return Maths.UNIT_NEG_Z;
        } else if (normal.equals(Maths.UNIT_Z)) {
            return Maths.UNIT_NEG_X;
        }

        return normal.equals(Maths.UNIT_X) ? Maths.UNIT_Z : Maths.UNIT_X;
    }

    /**
     * Only works if the input vector is axis aligned.
     * Based on the left handed coordinate system.
     */
    public static Vector3f getVerticalAlignedAxis(Vector3f normal) {
        if (normal.equals(Maths.UNIT_NEG_Y)) {
            return Maths.UNIT_NEG_Z;
        }
        return normal.equals(Maths.UNIT_Y) ? Maths.UNIT_Z : Maths.UNIT_Y;
    }
}
