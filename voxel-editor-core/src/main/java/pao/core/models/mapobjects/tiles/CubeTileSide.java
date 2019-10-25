package pao.core.models.mapobjects.tiles;

import com.wildrune.rune.util.Maths;

import org.joml.Vector3f;

/**
 * @author Mark "Wilds" van der Wal
 * @since 16-3-2018
 */
public enum CubeTileSide {
    Front(Maths.UNIT_NEG_Z),
    Back(Maths.UNIT_Z),
    Left(Maths.UNIT_NEG_X),
    Right(Maths.UNIT_X),
    Bottom(Maths.UNIT_NEG_Y),
    Top(Maths.UNIT_Y);

    private Vector3f axis;

    CubeTileSide(Vector3f axis) {
        this.axis = new Vector3f(axis);
    }

    public static CubeTileSide getSideByNormal(Vector3f normal) {
        if (normal.equals(Maths.UNIT_X)) {
            return Right;
        } else if (normal.equals(Maths.UNIT_NEG_X)) {
            return Left;
        } else if (normal.equals(Maths.UNIT_Y)) {
            return Top;
        } else if (normal.equals(Maths.UNIT_NEG_Y)) {
            return Bottom;
        } else if (normal.equals(Maths.UNIT_Z)) {
            return Back;
        } else {
            return Front;
        }
    }

    public Vector3f getAxis() {
        return axis;
    }
}
