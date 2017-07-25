package com.cgfay.caincamera.filter.base;

import android.opengl.GLES30;

import java.util.ArrayList;
import java.util.List;

/**
 * 滤镜组基类
 * Created by cain on 17-7-17.
 */
public class BaseImageFilterGroup extends BaseImageFilter {

    private static int[] mFramebuffers;
    private static int[] mFrameBufferTextures;
    private int mFrameWdith;
    private int mFrameHeight;

    private int mTextureId = -1;

    protected List<BaseImageFilter> mFilters = new ArrayList<BaseImageFilter>();

    public BaseImageFilterGroup() {

    }

    public BaseImageFilterGroup(List<BaseImageFilter> filters) {
        mFilters = filters;
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        int size = mFilters.size();
        for (int i = 0; i < size; i++) {
            mFilters.get(i).onInputSizeChanged(width, height);
        }
        // 先销毁原来的Framebuffers
        if(mFramebuffers != null && (mFrameWdith != width
                || mFrameHeight != height || mFramebuffers.length != size-1)) {
            destroyFramebuffers();
            mFrameWdith = width;
            mFrameHeight = height;
        }
        // 创建Framebuffers 和 Textures
        if (mFramebuffers == null) {
            mFramebuffers = new int[size - 1];
            mFrameBufferTextures = new int[size - 1];
            for (int i = 0; i < size - 1; i++) {
                GLES30.glGenFramebuffers(1, mFramebuffers, i);

                GLES30.glGenTextures(1, mFrameBufferTextures, i);
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mFrameBufferTextures[i]);
                GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height, 0,
                        GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                        GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                        GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                        GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                        GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFramebuffers[i]);
                GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
                        GLES30.GL_TEXTURE_2D, mFrameBufferTextures[i], 0);

                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
            }
        }
    }

    @Override
    public void drawFrame(int textureId, float[] texMatrix) {
        if (mFramebuffers == null || mFrameBufferTextures == null) {
            return;
        }
        int size = mFilters.size();
        int previewTexture = textureId;
        for (int i = 0; i < size; i++) {
            BaseImageFilter filter = mFilters.get(i);
            if (i < size - 1) {
                GLES30.glViewport(0, 0, mImageWidth, mImageHeight);
                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFramebuffers[i]);
                GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
                filter.drawFrame(previewTexture, texMatrix);
                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
                previewTexture = mFrameBufferTextures[i];
            } else {
                GLES30.glViewport(0, 0, mDisplayWidth, mDisplayHeight);
                filter.drawFrame(previewTexture, texMatrix);
            }
        }
    }

    @Override
    public void release() {
        if (mFilters != null) {
            for (BaseImageFilter filter : mFilters) {
                filter.release();
            }
            mFilters.clear();
        }
        destroyFramebuffers();
        mTextureId = -1;
    }

    /**
     * 销毁Framebuffers
     */
    private void destroyFramebuffers() {
        if (mFrameBufferTextures != null) {
            GLES30.glDeleteTextures(mFrameBufferTextures.length, mFrameBufferTextures, 0);
            mFrameBufferTextures = null;
        }
        if (mFramebuffers != null) {
            GLES30.glDeleteFramebuffers(mFramebuffers.length, mFramebuffers, 0);
            mFramebuffers = null;
        }
    }
}
