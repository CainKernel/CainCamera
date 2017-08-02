package com.cgfay.caincamera.utils;

import android.hardware.Camera;
import android.util.Log;

import java.nio.FloatBuffer;

/**
 * Created by cain on 17-7-26.
 */

public class TextureRotationUtils {

    // 摄像头是否倒置，主要是应对Nexus 5X (bullhead) 的后置摄像头图像倒置的问题
    private static boolean mBackReverse = false;

    public static final int CoordsPerVertex = 3;

    public static final float CubeVertices[] = {
            -1.0f, -1.0f, 0.0f,  // 0 bottom left
            1.0f, -1.0f,  0.0f,  // 1 bottom right
            -1.0f,  1.0f, 0.0f,  // 2 top left
            1.0f,  1.0f,  0.0f,  // 3 top right
    };

    public static final float TextureVertices[] = {
            0.0f, 0.0f,     // 0 bottom left
            1.0f, 0.0f,     // 1 bottom right
            0.0f, 1.0f,     // 2 top left
            1.0f, 1.0f      // 3 top right
    };

    public static final float TextureVertices_90[] = {
            1.0f, 0.0f, 0.0f,
            1.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
    };

    public static final float TextureVertices_180[] = {
            1.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f,
    };

    public static final float TextureVertices_270[] = {
            0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f,
            1.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f,
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
                if (CameraUtils.getCameraID() == Camera.CameraInfo.CAMERA_FACING_BACK
                        && mBackReverse) {
                    result = GlUtil.createFloatBuffer(TextureRotationUtils.TextureVertices_270);
                } else {
                    result = GlUtil.createFloatBuffer(TextureRotationUtils.TextureVertices_90);
                }
                break;

            case 90:
                if (CameraUtils.getCameraID() == Camera.CameraInfo.CAMERA_FACING_BACK
                        && mBackReverse) {
                    result = GlUtil.createFloatBuffer(TextureRotationUtils.TextureVertices_180);
                } else {
                    result = GlUtil.createFloatBuffer(TextureRotationUtils.TextureVertices);
                }
                break;

            case 180:
                if (CameraUtils.getCameraID() == Camera.CameraInfo.CAMERA_FACING_BACK
                        && mBackReverse) {
                    result = GlUtil.createFloatBuffer(TextureRotationUtils.TextureVertices_90);
                } else {
                    result = GlUtil.createFloatBuffer(TextureRotationUtils.TextureVertices_270);
                }
                break;

            case 270:
                if (CameraUtils.getCameraID() == Camera.CameraInfo.CAMERA_FACING_BACK
                        && mBackReverse) {
                    result = GlUtil.createFloatBuffer(TextureRotationUtils.TextureVertices);
                } else {
                    result = GlUtil.createFloatBuffer(TextureRotationUtils.TextureVertices_180);
                }
                break;

            default:
                if (CameraUtils.getCameraID() == Camera.CameraInfo.CAMERA_FACING_BACK
                        && mBackReverse) {
                    result = GlUtil.createFloatBuffer(TextureRotationUtils.TextureVertices_180);
                } else {
                    result = GlUtil.createFloatBuffer(TextureRotationUtils.TextureVertices);
                }

        }
        return result;
    }

    public static void setBackReverse(boolean reverse) {
        mBackReverse = reverse;
    }
}
