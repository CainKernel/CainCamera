package com.cgfay.caincamera.filter.base;

import android.opengl.GLES30;

import com.cgfay.caincamera.type.FilterType;
import com.cgfay.caincamera.utils.GlUtil;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * 滤镜组基类
 * Created by cain on 17-7-17.
 */
public abstract class BaseImageFilterGroup extends BaseImageFilter {

    private static int[] mFramebuffers;
    private static int[] mFrameBufferTextures;

    protected List<BaseImageFilter> mFilters = new ArrayList<BaseImageFilter>();

    public BaseImageFilterGroup() {

    }

    public BaseImageFilterGroup(List<BaseImageFilter> filters) {
        mFilters = filters;
    }

    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        if (mFilters.size() <= 0) {
            return;
        }
        int size = mFilters.size();
        for (int i = 0; i < size; i++) {
            mFilters.get(i).onInputSizeChanged(width, height);
        }
        // 先销毁原来的Framebuffers
        if(mFramebuffers != null && (mImageWidth != width
                || mImageHeight != height || mFramebuffers.length != size-1)) {
            destroyFramebuffer();
            mImageWidth = width;
            mImageWidth = height;
        }
        initFramebuffer(width, height);
    }

    @Override
    public void onDisplayChanged(int width, int height) {
        super.onDisplayChanged(width, height);
        // 更新显示的的视图大小
        if (mFilters.size() <= 0) {
            return;
        }
        int size = mFilters.size();
        for (int i = 0; i < size; i++) {
            mFilters.get(i).onDisplayChanged(width, height);
        }
    }

    @Override
    public void drawFrame(int textureId) {
        if (mFramebuffers == null || mFrameBufferTextures == null || mFilters.size() <= 0) {
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
                filter.drawFrame(previewTexture);
                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
                previewTexture = mFrameBufferTextures[i];
            } else {
                GLES30.glViewport(0, 0, mDisplayWidth, mDisplayHeight);
                filter.drawFrame(previewTexture);
            }
        }
    }

    @Override
    public void drawFrame(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        if (mFramebuffers == null || mFrameBufferTextures == null || mFilters.size() <= 0) {
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
                filter.drawFrame(previewTexture, vertexBuffer, textureBuffer);
                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
                previewTexture = mFrameBufferTextures[i];
            } else {
                GLES30.glViewport(0, 0, mDisplayWidth, mDisplayHeight);
                filter.drawFrame(previewTexture, vertexBuffer, textureBuffer);
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
        destroyFramebuffer();
    }

    /**
     * 初始化framebuffer
     */
    public void initFramebuffer(int width, int height) {
        int size = mFilters.size();
        // 创建Framebuffers 和 Textures
        if (mFramebuffers == null) {
            mFramebuffers = new int[size - 1];
            mFrameBufferTextures = new int[size - 1];
            createFramebuffer(0, size - 1);
        }
    }

    /**
     * 创建Framebuffer
     * @param start
     * @param size
     */
    private void createFramebuffer(int start, int size) {
        for (int i = start; i < size; i++) {
            GLES30.glGenFramebuffers(1, mFramebuffers, i);

            GLES30.glGenTextures(1, mFrameBufferTextures, i);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mFrameBufferTextures[i]);

            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA,
                    mImageWidth, mImageHeight, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);
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

    /**
     * 销毁Framebuffers
     */
    public void destroyFramebuffer() {
        if (mFrameBufferTextures != null) {
            GLES30.glDeleteTextures(mFrameBufferTextures.length, mFrameBufferTextures, 0);
            mFrameBufferTextures = null;
        }
        if (mFramebuffers != null) {
            GLES30.glDeleteFramebuffers(mFramebuffers.length, mFramebuffers, 0);
            mFramebuffers = null;
        }
    }

    /**
     * 添加新滤镜
     * @param filters
     */
    public void addFilters(List<BaseImageFilter> filters) {
        mFilters.addAll(filters);
        addFrambuffers();
    }

    public abstract void changeFilter(FilterType type);

    /**
     * 添加Framebuffer
     */
    private void addFrambuffers() {
        // 复制和创建新的frambuffer
        int size = mFilters.size();
        int[] framebuffers = new int[size - 1];
        int[] framebufferTextures = new int[size - 1];
        // 复制原来的Framebuffer和Texture
        if (mFramebuffers != null) {
            for (int i = 0; i < mFramebuffers.length; i++) {
                framebuffers[i] = mFramebuffers[i];
                framebufferTextures[i] = mFrameBufferTextures[i];
            }
        }

        int start = 0;
        if (mFramebuffers != null) {
            start = mFramebuffers.length;
        }
        mFramebuffers = framebuffers;
        mFrameBufferTextures = framebufferTextures;
        // 创建新的
        createFramebuffer(start, size - 1);
    }

    /**
     * 替换滤镜组
     * @param filters
     */
    public void replaceWidthFilters(List<BaseImageFilter> filters) {
        for (int i = 0; i < mFilters.size(); i++) {
            mFilters.get(i).release();
        }
        mFilters.clear();
        mFilters = filters;
        if (mFramebuffers != null && mFilters.size() < mFramebuffers.length) {
            // 销毁多余的Framebuffers
            int size = mFilters.size() - 1;
            GLES30.glDeleteTextures(mFrameBufferTextures.length - size, mFrameBufferTextures, size);
            GLES30.glDeleteFramebuffers(mFramebuffers.length - size, mFramebuffers, size);
            int[] framebuffers = new int[size];
            int[] framebuffersTextures = new int[size];
            for (int i = 0; i < size; i++) {
                framebuffers[i] = mFramebuffers[i];
                framebuffersTextures[i] = mFrameBufferTextures[i];
            }
            mFramebuffers = framebuffers;
            mFrameBufferTextures = framebuffersTextures;
        } else if (mFramebuffers == null || mFilters.size() > mFramebuffers.length) {
            // 添加Framebuffers
            addFrambuffers();
        }
    }
}
