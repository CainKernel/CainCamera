package com.cgfay.coregraphics;

import java.io.Serializable;

/**
 * 仿射变换
 */
public class AffineTransform implements Serializable {

    public static final AffineTransform kAffineTransformIdentity = new AffineTransform().idt();

    public float m00 = 1, m01 = 0, m02 = 0;
    public float m10 = 0, m11 = 1, m12 = 0;

    public AffineTransform() {
    }
    public AffineTransform(AffineTransform other) {
        set(other);
    }

    public AffineTransform idt() {
        m00 = 1;
        m01 = 0;
        m02 = 0;
        m10 = 0;
        m11 = 1;
        m12 = 0;
        return this;
    }

    public AffineTransform set(AffineTransform other) {
        m00 = other.m00;
        m01 = other.m01;
        m02 = other.m02;
        m10 = other.m10;
        m11 = other.m11;
        m12 = other.m12;
        return this;
    }

    public AffineTransform setTranslation(float x, float y) {
        m00 = 1;
        m01 = 0;
        m02 = x;
        m10 = 0;
        m11 = 1;
        m12 = y;
        return this;
    }

    public AffineTransform setScaling(float scaleX, float scaleY) {
        m00 = scaleX;
        m01 = 0;
        m02 = 0;
        m10 = 0;
        m11 = scaleY;
        m12 = 0;
        return this;
    }

    public AffineTransform setRotation(float degrees) {
        float cos = MathUtils.cosDeg(degrees);
        float sin = MathUtils.sinDeg(degrees);

        m00 = cos;
        m01 = -sin;
        m02 = 0;
        m10 = sin;
        m11 = cos;
        m12 = 0;
        return this;
    }
}
