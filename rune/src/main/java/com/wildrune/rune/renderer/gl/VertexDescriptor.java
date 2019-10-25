package com.wildrune.rune.renderer.gl;

import java.util.*;

/**
 * @author Mark van der Wal
 * @since 31/01/18
 */
public class VertexDescriptor implements Iterable<VertexAttribute> {

    private VertexAttribute[] attributes;
    private int sizeInBytes;
    private int componentsSize;

    public VertexDescriptor(VertexAttribute... attr) {
        attributes = attr;
        calculateData();
    }

    private void calculateData() {
        sizeInBytes = 0;
        componentsSize = 0;

        for (VertexAttribute attribute : attributes) {
            attribute.setOffset(sizeInBytes);
            componentsSize += attribute.getComponentsSize();
            sizeInBytes += attribute.getSizeInBytes();
        }
    }

    public int getSizeInBytes() {
        return sizeInBytes;
    }

    public int getComponentsSize() {
        return componentsSize;
    }

    @Override
    public Iterator<VertexAttribute> iterator() {
        return Arrays.asList(attributes).iterator();
    }
}
