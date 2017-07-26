package com.cgfay.caincamera.utils;

import java.nio.FloatBuffer;

/**
 * Created by cain on 17-7-26.
 */

public class TextureRotationUtils {

    public static final float SquareVertices[] = {
            -1.0f, -1.0f,   // 0 bottom left
            1.0f, -1.0f,   // 1 bottom right
            -1.0f,  1.0f,   // 2 top left
            1.0f,  1.0f,   // 3 top right
    };

    public static final float TextureVertices[] = {
            0.0f, 0.0f,     // 0 bottom left
            1.0f, 0.0f,     // 1 bottom right
            0.0f, 1.0f,     // 2 top left
            1.0f, 1.0f      // 3 top right
    };

    public static final float TextureVertices_90[] = {
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            0.0f, 1.0f
    };

    public static final float TextureVertices_180[] = {
            1.0f, 1.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f
    };

    public static final float TextureVertices_270[] = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f
    };

    public static float[] getTextureVertices() {
        float[] result;
        switch (CameraUtils.getPreviewOrientation()) {
            case 0:
                result = TextureVertices_90;
                break;

            case 90:
                result = TextureVertices;
                break;

            case 180:
                result = TextureVertices_270;
                break;

            case 270:
                result = TextureVertices_180;
                break;

            default:
                result = TextureVertices;
        }
        return result;
    }

    public static FloatBuffer getTextureBuffer() {
        FloatBuffer result;
        switch (CameraUtils.getPreviewOrientation()) {
            case 0:
                result = GlUtil.createFloatBuffer(TextureRotationUtils.TextureVertices_90);
                break;

            case 90:
                result = GlUtil.createFloatBuffer(TextureRotationUtils.TextureVertices);
                break;

            case 180:
                result = GlUtil.createFloatBuffer(TextureRotationUtils.TextureVertices_270);
                break;

            case 270:
                result = GlUtil.createFloatBuffer(TextureRotationUtils.TextureVertices_180);
                break;

            default:
                result = GlUtil.createFloatBuffer(TextureRotationUtils.TextureVertices);
        }
        return result;
    }
}
