package com.cgfay.caincamera.filter.base;

import android.opengl.GLES20;

import com.cgfay.caincamera.utils.GlUtil;

/**
 * Created by cain on 2017/7/11.
 */

public class SurfaceFilter extends BaseImageFilter {

    public SurfaceFilter() {
        super();
    }
    /**
     * 将Sampler2D绘制到屏幕
     * @param textureId
     * @param texMatrix
     */
    public void drawToScreen(int textureId, float[] texMatrix) {
        GLES20.glUseProgram(mProgramHandle);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
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
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);
    }

}
