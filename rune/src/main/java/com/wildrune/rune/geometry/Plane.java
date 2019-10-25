package com.wildrune.rune.geometry;

import org.joml.Vector3f;

/**
 * @author Mark "Wilds" van der Wal
 * @since 19-2-2018
 */
public class Plane {

    private Vector3f normal;
    private float distance;

    public Plane() {
        normal = new Vector3f();
        distance = 0.0f;
    }

    public Plane(Vector3f norm, Vector3f pointOnPlane) {
        this(norm, norm.dot(pointOnPlane));
    }

    public Plane(Vector3f norm, float d) {
        normal = new Vector3f(norm);
        distance = d;
    }

    public Vector3f getNormal() {
        return normal;
    }

    public void setNormal(Vector3f norm) {
        setNormal(norm.x, norm.y, norm.z);
    }

    public void setNormal(float x, float y, float z) {
        this.normal.set(x, y, z).normalize();
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public void setDistance(Vector3f pointOnPlane) {
        setDistance(pointOnPlane.x, pointOnPlane.y, pointOnPlane.z);
    }

    public void setDistance(float x, float y, float z) {
        this.distance = normal.dot(x, y, z);
    }

    public float distanceToPlane(Vector3f vector) {
        return normal.dot(vector) - distance;
    }
}