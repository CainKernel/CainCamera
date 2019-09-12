package com.cgfay.filter.glfilter.makeup;

import android.opengl.GLES30;

import com.cgfay.filter.glfilter.makeup.bean.MakeupBaseData;
import com.cgfay.filter.glfilter.utils.OpenGLUtils;
import com.cgfay.landmark.LandmarkEngine;

import java.nio.FloatBuffer;

/**
 * 美瞳加载器
 */
public class MakeupPupilLoader extends MakeupBaseLoader {

    private int[] mFrameBuffer;
    private int[] mFrameBufferTexture;

    public MakeupPupilLoader(GLImageMakeupFilter filter, MakeupBaseData makeupData, String folderPath) {
        super(filter, makeupData, folderPath);
    }

    @Override
    protected void initBuffers() {
        // TODO 创建眼睛的遮罩缓冲
    }

    @Override
    protected void updateVertices(int faceIndex) {
        if (mVertexBuffer == null || mVertices == null) {
            return;
        }
        mVertexBuffer.clear();
        if (LandmarkEngine.getInstance().hasFace()
                && LandmarkEngine.getInstance().getFaceSize() > faceIndex) {
            LandmarkEngine.getInstance().getEyeVertices(mVertices, faceIndex);
        }
        mVertexBuffer.put(mVertices);
        mVertexBuffer.position(0);
    }

    @Override
    public void drawMakeup(int faceIndex, int inputTexture, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        // 还没初始化FBO时，则无法处理美瞳
        if (mFrameBuffer == null || mFrameBufferTexture == null) {
            return;
        }
        // 更新顶点缓冲
        updateVertices(faceIndex);

        // 1、先将美瞳素材绘制到FBO中
        if (mWeakFilter.get() != null) {
            mWeakFilter.get().drawMakeup(mFrameBuffer[0], inputTexture, mMaterialTexture, mMaskTexture, mVertexBuffer,
                    mTextureBuffer, mIndexBuffer, mMakeupType, mStrength);
        }

        // 2、将超出眼眶部分的美瞳裁掉
        // 这里将绘制了美瞳的FBO纹理取出来作为素材纹理输入到shader中
        if (mWeakFilter.get() != null) {
            // mFrameBufferTexture 是绘制了美瞳的图像，这里作为素材输入进行裁剪
            mWeakFilter.get().drawMakeup(inputTexture, mFrameBufferTexture[0], mMaskTexture, mVertexBuffer,
                    mTextureBuffer, mIndexBuffer, mMakeupType, mStrength);
        }
    }


    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        initFrameBuffer(width, height);
    }

    /**
     * 创建FBO
     * @param width
     * @param height
     */
    private void initFrameBuffer(int width, int height) {
        if (mFrameBuffer != null && (mImageWidth != width || mImageHeight != height)) {
            destroyFrameBuffer();
        }
        if (mFrameBuffer == null) {
            mFrameBuffer = new int[1];
            mFrameBufferTexture = new int[1];
            OpenGLUtils.createFrameBuffer(mFrameBuffer, mFrameBufferTexture, width, height);
        }
    }

    /**
     * 销毁FBO
     */
    private void destroyFrameBuffer() {
        if (mFrameBufferTexture != null) {
            GLES30.glDeleteTextures(1, mFrameBufferTexture, 0);
            mFrameBufferTexture = null;
        }

        if (mFrameBuffer != null) {
            GLES30.glDeleteFramebuffers(1, mFrameBuffer, 0);
            mFrameBuffer = null;
        }
    }

    @Override
    public void release() {
        super.release();
        destroyFrameBuffer();
    }
}
