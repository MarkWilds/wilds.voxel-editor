package com.wildrune.rune.geometry;

import org.joml.Math;
import org.joml.Vector2f;

/**
 * 2D Equivalent of the box
 *
 * @author Mark van der Wal
 * @since 22/02/18
 */
public class Area {

    private Vector2f min;
    private Vector2f max;

    public Area(float x, float y) {
        this();
        grow(x, y);
    }

    public Area() {
        min = new Vector2f();
        max = new Vector2f();
        reset();
    }

    public void reset() {
        min.x = Float.MAX_VALUE;
        min.y = Float.MAX_VALUE;

        max.x = Float.MIN_VALUE;
        max.y = Float.MIN_VALUE;
    }

    public void grow(Vector2f point) {
        grow(point.x, point.y);
    }

    public void grow(float x, float y) {
        min.x = Math.min(x, min.x);
        min.y = Math.min(y, min.y);

        max.x = Math.max(x, max.x);
        max.y = Math.max(y, max.y);
    }

    /**
     * reset the area and grow from all gives values
     *
     * @param values all values the area is going to grow from
     */
    public void regenerate(float... values) {
        if (values.length > 0 && values.length % 2 == 0) {
            reset();

            for (int i = 0; i < values.length; i += 2) {
                grow(values[i], values[i + 1]);
            }
        }
    }

    public void regenerate(Vector2f... points) {
        if (points.length > 0) {
            reset();

            for (Vector2f point : points) {
                grow(point);
            }
        }
    }

    public boolean intersects(int x, int y) {
        return x >= min.x && x <= max.x && y >= min.y && y <= max.y;
    }

    public boolean hasVolume() {
        float absX = Math.abs(max.x - min.x);
        float absY = Math.abs(max.y - min.y);

        return absX > 0 && absY > 0;
    }

    public boolean hasArea() {
        float absX = Math.abs(max.x - min.x);
        float absY = Math.abs(max.y - min.y);

        return absX > 0 || absY > 0;
    }

    public float getX() {
        return min.x;
    }

    public float getY() {
        return min.y;
    }

    public float getWidth() {
        return max.x - min.x;
    }

    public float getHeight() {
        return max.y - min.y;
    }

    public Vector2f getMin() {
        return min;
    }

    public Vector2f getMax() {
        return max;
    }
}
