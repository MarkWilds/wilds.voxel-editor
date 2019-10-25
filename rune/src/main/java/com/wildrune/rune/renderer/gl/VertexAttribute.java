package com.wildrune.rune.renderer.gl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL2ES1;

/**
 * @author Mark "Wilds" van der Wal
 * @since 28-1-2018
 */
public class VertexAttribute {

    public final static VertexAttribute POSITION = new VertexAttribute(ShaderProgram.POSITION_ATTRIBUTE,
            3, VertexAttribute.Type.Float, false);
    public final static VertexAttribute COLOR3 = new VertexAttribute(ShaderProgram.COLOR3_ATTRIBUTE,
            3, VertexAttribute.Type.Float, false);
    public final static VertexAttribute COLOR4 = new VertexAttribute(ShaderProgram.COLOR4_ATTRIBUTE,
            4, VertexAttribute.Type.Float, false);
    public final static VertexAttribute NORMAL = new VertexAttribute(ShaderProgram.NORMAL_ATTRIBUTE,
            3, VertexAttribute.Type.Float, false);
    public final static VertexAttribute TEX_COORD0 = new VertexAttribute(ShaderProgram.TEXCOORD0_ATTRIBUTE,
            2, VertexAttribute.Type.Float, false);
    public final static VertexAttribute TEX_COORD_AXIS = new VertexAttribute(ShaderProgram.TEXCOORDAXIS_ATTRIBUTE,
            2, VertexAttribute.Type.Float, false);

    private String identifier;
    private int componentsSize;
    private Type componentsType;
    private boolean normalized;
    private int offset;

    public VertexAttribute(String name, int size, Type type, boolean norm) {
        identifier = name;
        componentsSize = size;
        componentsType = type;
        normalized = norm;
    }

    public int getSizeInBytes() {
        switch (componentsType) {
            case Float:
            case Fixed:
                return componentsSize * Buffers.SIZEOF_FLOAT;
            case UByte:
            case Byte:
                return componentsSize * Buffers.SIZEOF_BYTE;
            case UShort:
            case Short:
                return componentsSize * Buffers.SIZEOF_SHORT;
        }
        return 0;
    }

    public int getType() {
        switch (componentsType) {
            case Fixed:
                return GL2ES1.GL_FIXED;
            case UByte:
                return GL2ES1.GL_UNSIGNED_BYTE;
            case Byte:
                return GL2ES1.GL_BYTE;
            case UShort:
                return GL2ES1.GL_UNSIGNED_SHORT;
            case Short:
                return GL2ES1.GL_SHORT;
        }
        return GL2ES1.GL_FLOAT;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offs) {
        offset = offs;
    }

    public int getComponentsSize() {
        return componentsSize;
    }

    public boolean isNormalized() {
        return normalized;
    }

    public String getIdentifier() {
        return identifier;
    }

    public enum Type {
        Float,
        Fixed,
        UByte,
        Byte,
        UShort,
        Short
    }
}
