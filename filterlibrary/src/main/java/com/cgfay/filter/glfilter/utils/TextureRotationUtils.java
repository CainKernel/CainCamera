package com.cgfay.filter.glfilter.utils;

/**
 * Created by cain on 17-7-26.
 */

public class TextureRotationUtils {

    public static final int CoordsPerVertex = 2;

    public static final float CubeVertices[] = {
            -1.0f, -1.0f,  // 0 bottom left
            1.0f,  -1.0f,  // 1 bottom right
            -1.0f,  1.0f,  // 2 top left
            1.0f,   1.0f,  // 3 top right
    };

    public static final float TextureVertices[] = {
            0.0f, 0.0f,     // 0 left bottom
            1.0f, 0.0f,     // 1 right bottom
            0.0f, 1.0f,     // 2 left top
            1.0f, 1.0f      // 3 right top
    };

    // x轴反过来
    public static final float TextureVertices_flipx[] = {
            1.0f, 0.0f,     // 0 right bottom
            0.0f, 0.0f,     // 1 left  bottom
            1.0f, 1.0f,     // 2 right top
            0.0f, 1.0f      // 3 left  top
    };

    public static final float TextureVertices_90[] = {
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            0.0f, 1.0f,
    };

    public static final float TextureVertices_180[] = {
            1.0f, 1.0f, // right top
            0.0f, 1.0f, // left top
            1.0f, 0.0f, // right bottom
            0.0f, 0.0f, // left bottom
    };

    public static final float TextureVertices_270[] = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
    };

    /**
     * 索引，glDrawElements使用
     */
    public static final short[] Indices = {
            0, 1, 2,
            2, 1, 3,
    };


    private TextureRotationUtils() {}

    /**
     * 获取旋转后的Buffer
     * @param rotation
     * @param flipHorizontal
     * @param flipVertical
     * @return
     */
    public static float[] getRotation(final Rotation rotation, final boolean flipHorizontal,
                                      final boolean flipVertical) {
        float[] rotatedTex;
        switch (rotation) {
            case ROTATION_90:
                rotatedTex = TextureVertices_90;
                break;

            case ROTATION_180:
                rotatedTex = TextureVertices_180;
                break;

            case ROTATION_270:
                rotatedTex = TextureVertices_270;
                break;

            case NORMAL:
            default:
                rotatedTex = TextureVertices;
                break;
        }
        // 左右翻转
        if (flipHorizontal) {
            rotatedTex = new float[] {
                    flip(rotatedTex[0]), rotatedTex[1],
                    flip(rotatedTex[2]), rotatedTex[3],
                    flip(rotatedTex[4]), rotatedTex[5],
                    flip(rotatedTex[6]), rotatedTex[7],
            };
        }
        // 上下翻转
        if (flipVertical) {
            rotatedTex = new float[]{
                    rotatedTex[0], flip(rotatedTex[1]),
                    rotatedTex[2], flip(rotatedTex[3]),
                    rotatedTex[4], flip(rotatedTex[5]),
                    rotatedTex[6], flip(rotatedTex[7]),
            };
        }
        return rotatedTex;
    }

    /**
     * 翻转
     * @param i
     * @return
     */
    private static float flip(final float i) {
        if (i == 0.0f) {
            return 1.0f;
        }
        return 0.0f;
    }
}
