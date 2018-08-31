package com.cgfay.filterlibrary.glfilter.advanced;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;

/**
 * 外部纹理(OES纹理)输入
 * Created by cain on 2017/7/9.
 */

public class GLImageOESInputFilter extends GLImageFilter {
    private static final String VERTEX_SHADER = "" +
            "uniform mat4 uMVPMatrix;                               \n" +
            "uniform mat4 uTexMatrix;                               \n" +
            "attribute vec4 aPosition;                              \n" +
            "attribute vec4 aTextureCoord;                          \n" +
            "varying vec2 textureCoordinate;                            \n" +
            "void main() {                                          \n" +
            "    gl_Position = uMVPMatrix * aPosition;              \n" +
            "    textureCoordinate = (uTexMatrix * aTextureCoord).xy;   \n" +
            "}                                                      \n";

    private static final String FRAGMENT_SHADER_OES = "" +
            "#extension GL_OES_EGL_image_external : require         \n" +
            "precision mediump float;                               \n" +
            "varying vec2 textureCoordinate;                            \n" +
            "uniform samplerExternalOES inputTexture;                   \n" +
            "void main() {                                          \n" +
            "    gl_FragColor = texture2D(inputTexture, textureCoordinate); \n" +
            "}                                                      \n";


    private int mTexMatrixHandle;
    private float[] mTextureMatrix;

    public GLImageOESInputFilter(Context context) {
        this(context, VERTEX_SHADER, FRAGMENT_SHADER_OES);
    }

    public GLImageOESInputFilter(Context context, String vertexShader, String fragmentShader) {
        super(context, vertexShader, fragmentShader);
    }

    @Override
    public void initProgramHandle() {
        super.initProgramHandle();
        mTexMatrixHandle = GLES30.glGetUniformLocation(mProgramHandle, "uTexMatrix");
    }

    @Override
    public int getTextureType() {
        return GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
    }

    @Override
    public void onDrawFrameBegin() {
        super.onDrawFrameBegin();
        GLES30.glUniformMatrix4fv(mTexMatrixHandle, 1, false, mTextureMatrix, 0);
    }

    /**
     * 设置SurfaceTexture的变换矩阵
     * @param texMatrix
     */
    public void setTextureTransformMatirx(float[] texMatrix) {
        mTextureMatrix = texMatrix;
    }

}
