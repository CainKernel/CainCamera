package com.cgfay.coregraphics;

/**
 * 工具类
 */
public class AffineTransformUtils {

    private AffineTransformUtils() {

    }

    /**
     * Return a transform which translates by `(tx, ty)':
     * t' = [ 1 0 0 1 tx ty ]
     */
    public static AffineTransform CGAffineTransformMakeTranslation(float tx, float ty) {
        return new AffineTransform(1, 0, 0, 1, tx, ty);
    }

    /**
     * Return a transform which scales by `(sx, sy)':
     * t' = [ sx 0 0 sy 0 0 ]
     */
    public static AffineTransform CGAffineTransformMakeScale(float sx, float sy) {
        return new AffineTransform(sx, 0, 0, sy, 0, 0);
    }

    /**
     * Return a transform which rotates by `angle' radians:
     * t' = [ cos(angle) sin(angle) -sin(angle) cos(angle) 0 0 ]
     */
    public static AffineTransform CGAffineTransformMakeRotation(float angle) {
        return new AffineTransform((float) Math.cos(angle), (float)Math.sin(angle),
                (float)-Math.sin(angle), (float)Math.cos(angle), 0, 0);
    }



}
