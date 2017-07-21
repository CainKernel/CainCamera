package com.cgfay.caincamera.filter.base;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.cgfay.caincamera.utils.GlUtil;

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
            "uniform samplerExternalOES sTexture;                   \n" +
            "void main() {                                          \n" +
            "    gl_FragColor = texture2D(sTexture, textureCoordinate); \n" +
            "}                                                      \n";

    public CameraFilter() {
        mProgramHandle = GlUtil.createProgram(VERTEX_SHADER, FRAGMENT_SHADER_OES);
        muMVPMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
        muTexMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uTexMatrix");
        maPositionLoc = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
        maTextureCoordLoc = GLES20.glGetAttribLocation(mProgramHandle, "aTextureCoord");
    }

    @Override
    public int getTextureType() {
        return GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
    }

    /**
     * 将SurfaceTexture的纹理绘制到FBO
     * @param frameBuffer
     * @param textureId
     * @param texMatrix
     */
    public void drawToTexture(int frameBuffer, int textureId, float[] texMatrix) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer);
        GLES20.glUseProgram(mProgramHandle);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, GlUtil.IDENTITY_MATRIX, 0);
        GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, texMatrix, 0);
        GLES20.glEnableVertexAttribArray(maPositionLoc);
        GLES20.glVertexAttribPointer(maPositionLoc, mCoordsPerVertex,
                GLES20.GL_FLOAT, false, mVertexStride, mVertexArray);
        GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
        GLES20.glVertexAttribPointer(maTextureCoordLoc, 2,
                GLES20.GL_FLOAT, false, mTexCoordStride, getTexCoordArray());
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexCount);
        GLES20.glFinish();
        GLES20.glDisableVertexAttribArray(maPositionLoc);
        GLES20.glDisableVertexAttribArray(maTextureCoordLoc);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glUseProgram(0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }
}
