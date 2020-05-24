package com.cgfay.coregraphics;

/**
 * 仿射变换
 */
public class AffineTransform {

    public static final AffineTransform kAffineTransformIdentity = new AffineTransform(1, 0, 0, 1, 0, 0);

    float a, b, c, d;
    float tx, ty;

    public AffineTransform(float a, float b, float c, float d, float tx, float ty) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.tx = tx;
        this.ty = ty;
    }
}
