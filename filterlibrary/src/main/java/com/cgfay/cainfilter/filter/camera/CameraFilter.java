package com.cgfay.cainfilter.filter.camera;

import android.opengl.GLES11Ext;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.cgfay.cainfilter.filter.base.BaseImageFilter;
import com.cgfay.cainfilter.utils.TextureRotationUtils;

/**
 * Created by cain on 2017/7/9.
 */

public class CameraFilter extends BaseImageFilter {
    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;                               \n" +
            "uniform mat4 uTexMatrix;                               \n" +
            "attribute vec4 aPosition;                              \n" +
            "attribute vec4 aTextureCoord;                          \n" +
            "varying vec2 textureCoordinate;                            \n" +
            "void main() {                                          \n" +
            "    gl_Position = uMVPMatrix * aPosition;              \n" +
            "    textureCoordinate = (uTexMatrix * aTextureCoord).xy;   \n" +
            "}                                                      \n";

    private static final String FRAGMENT_SHADER_OES =
            "#extension GL_OES_EGL_image_external : require         \n" +
            "precision mediump float;                               \n" +
            "varying vec2 textureCoordinate;                            \n" +
            "uniform samplerExternalOES inputTexture;                   \n" +
            "void main() {                                          \n" +
            "    gl_FragColor = texture2D(inputTexture, textureCoordinate); \n" +
            "}                                                      \n";


    private int muTexMatrixLoc;
    private float[] mTextureMatrix;

    public CameraFilter() {
        this(VERTEX_SHADER, FRAGMENT_SHADER_OES);
    }

    public CameraFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
        muTexMatrixLoc = GLES30.glGetUniformLocation(mProgramHandle, "uTexMatrix");
    }

    @Override
    public int getTextureType() {
        return GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
    }

    @Override
    public void onDrawArraysBegin() {
        GLES30.glUniformMatrix4fv(muTexMatrixLoc, 1, false, mTextureMatrix, 0);
    }

    public void updateTextureBuffer() {
        mTexCoordArray = TextureRotationUtils.getTextureBuffer();
    }

    /**
     * 设置SurfaceTexture的变换矩阵
     * @param texMatrix
     */
    public void setTextureTransformMatirx(float[] texMatrix) {
        mTextureMatrix = texMatrix;
    }

    /**
     * 镜像翻转
     * @param coords
     * @param matrix
     * @return
     */
    private float[] transformTextureCoordinates(float[] coords, float[] matrix) {
        float[] result = new float[coords.length];
        float[] vt = new float[4];

        for (int i = 0; i < coords.length; i += 2) {
            float[] v = { coords[i], coords[i + 1], 0, 1 };
            Matrix.multiplyMV(vt, 0, matrix, 0, v, 0);
            result[i] = vt[0];// x轴镜像
            // result[i + 1] = vt[1];y轴镜像
            result[i + 1] = coords[i + 1];
        }
        return result;
    }
}
