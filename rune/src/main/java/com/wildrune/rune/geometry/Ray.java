package com.wildrune.rune.geometry;

import org.joml.Vector3f;

/**
 * @author Mark "Wilds" van der Wal
 * @since 19-2-2018
 */
public class Ray {

    private Vector3f position;
    private Vector3f direction;

    public Ray() {
        position = new Vector3f();
        direction = new Vector3f();
    }

    public Ray(Vector3f pos, Vector3f dir) {
        position = new Vector3f(pos);
        direction = new Vector3f(dir);
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public void setDirection(Vector3f direction) {
        this.direction = direction;
    }
}
