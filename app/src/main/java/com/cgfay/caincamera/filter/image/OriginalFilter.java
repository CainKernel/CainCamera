package com.cgfay.caincamera.filter.image;

import android.opengl.GLES30;
import android.opengl.Matrix;

import com.cgfay.caincamera.core.ScaleType;
import com.cgfay.caincamera.filter.base.BaseImageFilter;
import com.cgfay.caincamera.utils.GlUtil;
import com.cgfay.caincamera.utils.TextureRotationUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * 原始图片滤镜，没有做过任何修改的
 * Created by cain.huang on 2017/8/10.
 */
public class OriginalFilter extends BaseImageFilter {

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;
    private int[] mFramebuffers;
    private int[] mFramebufferTextures;

    public OriginalFilter() {
        super();
        init();
    }

    public OriginalFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
        init();
    }

    private void init() {
        setVertices(TextureRotationUtils.CubeVertices);
        setTextures(TextureRotationUtils.TextureVertices_180);
        flipTexture();
    }

    /**
     * 绘制Frame
     * @param textureId
     * @param vertexBuffer
     * @param textureBuffer
     */
    public void drawFrame(int textureId, FloatBuffer vertexBuffer,
                          FloatBuffer textureBuffer) {
        if (textureId == GlUtil.GL_NOT_INIT) {
            return;
        }
        GLES30.glUseProgram(mProgramHandle);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(getTextureType(), textureId);
        GLES30.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mMVPMatrix, 0);
        runPendingOnDrawTasks();
        GLES30.glEnableVertexAttribArray(maPositionLoc);
        GLES30.glVertexAttribPointer(maPositionLoc, mCoordsPerVertex,
                GLES30.GL_FLOAT, false, mVertexStride, mVertexBuffer);
        GLES30.glEnableVertexAttribArray(maTextureCoordLoc);
        GLES30.glVertexAttribPointer(maTextureCoordLoc, CoordsPerTexture,
                GLES30.GL_FLOAT, false, mTexCoordStride, mTextureBuffer);
        GLES30.glUniform1i(mInputTextureLoc, 0);
        onDrawArraysBegin();
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, mVertexCount);
        GLES30.glDisableVertexAttribArray(maPositionLoc);
        GLES30.glDisableVertexAttribArray(maTextureCoordLoc);
        onDrawArraysAfter();
        GLES30.glBindTexture(getTextureType(), 0);
        GLES30.glUseProgram(0);
    }

    /**
     * 将SurfaceTexture的纹理绘制到FBO
     * @param textureId
     * @return 绘制完成返回绑定到Framebuffer中的Texture
     */
    public int drawToTexture(int textureId) {
        if (mFramebuffers == null) {
            return GlUtil.GL_NOT_INIT;
        }
        runPendingOnDrawTasks();
        GLES30.glViewport(0, 0, mImageWidth, mImageHeight);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFramebuffers[0]);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        GLES30.glUseProgram(mProgramHandle);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);
        GLES30.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mMVPMatrix, 0);
        GLES30.glEnableVertexAttribArray(maPositionLoc);
        GLES30.glVertexAttribPointer(maPositionLoc, mCoordsPerVertex,
                GLES30.GL_FLOAT, false, mVertexStride, mVertexBuffer);
        GLES30.glEnableVertexAttribArray(maTextureCoordLoc);
        GLES30.glVertexAttribPointer(maTextureCoordLoc, CoordsPerTexture,
                GLES30.GL_FLOAT, false, mTexCoordStride, mTextureBuffer);
        GLES30.glUniform1i(mInputTextureLoc, 0);
        onDrawArraysBegin();
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, mVertexCount);
        GLES30.glDisableVertexAttribArray(maPositionLoc);
        GLES30.glDisableVertexAttribArray(maTextureCoordLoc);
        onDrawArraysAfter();
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
        GLES30.glUseProgram(0);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        GLES30.glViewport(0, 0, mDisplayWidth, mDisplayHeight);
        return mFramebufferTextures[0];
    }

    /**
     * 初始化Framebuffer
     * @param width
     * @param height
     */
    public void initFramebuffer(int width, int height) {
        if (mFramebuffers != null && (mImageWidth != width || mImageHeight != height)) {
            destroyFramebuffer();
        }
        if (mFramebuffers == null) {
            mImageWidth = width;
            mImageHeight = height;
            mFramebuffers = new int[1];
            mFramebufferTextures = new int[1];
            GlUtil.createSampler2DFrameBuff(mFramebuffers, mFramebufferTextures, width, height);
        }
    }

    /**
     * 销毁Framebuffer
     */
    public void destroyFramebuffer() {
        if (mFramebufferTextures != null) {
            GLES30.glDeleteTextures(1, mFramebufferTextures, 0);
            mFramebufferTextures = null;
        }

        if (mFramebuffers != null) {
            GLES30.glDeleteFramebuffers(1, mFramebuffers, 0);
            mFramebuffers = null;
        }
        mImageWidth = -1;
        mImageHeight = -1;
    }

    /**
     * 设置Texture顶点
     * @param textureVertices
     */
    private void setTextures(float[] textureVertices) {
        ByteBuffer mByteBuffers = ByteBuffer.allocateDirect(textureVertices.length * 4);
        mByteBuffers.order(ByteOrder.nativeOrder());
        mTextureBuffer = mByteBuffers.asFloatBuffer();
        mTextureBuffer.position(0);
        mTextureBuffer.put(textureVertices);
        mTextureBuffer.position(0);
    }
    /**
     * 设置顶点
     * @param vertices
     */
    private void setVertices(float[] vertices) {
        ByteBuffer mByteBuffers = ByteBuffer.allocateDirect(vertices.length * 4);
        mByteBuffers.order(ByteOrder.nativeOrder());
        mVertexBuffer = mByteBuffers.asFloatBuffer();
        mVertexBuffer.position(0);
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);
    }

    /**
     * 沿Y轴翻转（默认情况下，图像是反过来的，这里需要做一定的调整）
     */
    private void flipTexture() {
        Matrix.rotateM(mMVPMatrix, 0, 180, 0, 1, 0);
    }

}
