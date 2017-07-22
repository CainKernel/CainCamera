package com.cgfay.caincamera.filter.base;

import android.opengl.GLES30;

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
        GLES30.glUseProgram(mProgramHandle);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);
        GLES30.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, GlUtil.IDENTITY_MATRIX, 0);
        GLES30.glUniformMatrix4fv(muTexMatrixLoc, 1, false, texMatrix, 0);
        GLES30.glEnableVertexAttribArray(maPositionLoc);
        GLES30.glVertexAttribPointer(maPositionLoc, mCoordsPerVertex,
                GLES30.GL_FLOAT, false, mVertexStride, mVertexArray);
        GLES30.glEnableVertexAttribArray(maTextureCoordLoc);
        GLES30.glVertexAttribPointer(maTextureCoordLoc, 2,
                GLES30.GL_FLOAT, false, mTexCoordStride, getTexCoordArray());
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, mVertexCount);
        GLES30.glFinish();
        GLES30.glDisableVertexAttribArray(maPositionLoc);
        GLES30.glDisableVertexAttribArray(maTextureCoordLoc);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        GLES30.glUseProgram(0);
    }

}
