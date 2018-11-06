package com.cgfay.filterlibrary.glfilter.base;

import android.content.Context;
import android.opengl.GLES30;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * 滤镜组基类
 * Created by cain on 17-7-17.
 */
public abstract class GLImageGroupFilter extends GLImageFilter {

    protected List<GLImageFilter> mFilters = new ArrayList<GLImageFilter>();

    public GLImageGroupFilter(Context context) {
        super(context, null, null);
    }

    protected GLImageGroupFilter(Context context, List<GLImageFilter> filters) {
        super(context, null, null);
        if (filters != null) {
            mFilters.addAll(filters);
        }
    }

    /**
     * 重载设置输入纹理大小方法，用于设置各个子滤镜的输入纹理大小
     * @param width
     * @param height
     */
    @Override
    public void onInputSizeChanged(int width, int height) {
        super.onInputSizeChanged(width, height);
        for (int i = 0; i < mFilters.size(); i++) {
            if (mFilters.get(i) != null) {
                mFilters.get(i).onInputSizeChanged(width, height);
            }
        }
    }

    /**
     * 重载显示大小方法，用于设置各个子滤镜的显示大小
     * @param width
     * @param height
     */
    @Override
    public void onDisplaySizeChanged(int width, int height) {
        super.onDisplaySizeChanged(width, height);
        for (int i = 0; i < mFilters.size(); i++) {
            if (mFilters.get(i) != null) {
                mFilters.get(i).onDisplaySizeChanged(width, height);
            }
        }
    }

    /**
     * 重载绘制渲染方法，用于绘制渲染各个子滤镜
     * @param textureId
     * @param vertexBuffer
     * @param textureBuffer
     * @return
     */
    @Override
    public boolean drawFrame(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        if (mFilters.size() == 0) {
            return false;
        }
        boolean result = super.drawFrame(textureId, vertexBuffer, textureBuffer);
        int currentTexture = textureId;
        int size = mFilters.size();
        for (int i = 0; i < size; i++) {
            if (i == size - 1) {
                int displayWidth = mFilters.get(i).getDisplayWidth();
                int displayHeight = mFilters.get(i).getDisplayHeight();
                GLES30.glViewport(0, 0, displayWidth, displayHeight);
                if (mFilters.get(i) != null) {
                    result = mFilters.get(i).drawFrame(currentTexture, vertexBuffer, textureBuffer);
                }
            } else {
                if (mFilters.get(i) != null) {
                    currentTexture = mFilters.get(i)
                            .drawFrameBuffer(currentTexture, vertexBuffer, textureBuffer);
                }
            }
        }
        return result;
    }

    /**
     * 重载绘制到FBO方法，用于各个子滤镜绘制到FBO
     * @param textureId
     * @param vertexBuffer
     * @param textureBuffer
     * @return
     */
    @Override
    public int drawFrameBuffer(int textureId, FloatBuffer vertexBuffer, FloatBuffer textureBuffer) {
        if (mFilters.size() == 0) {
            return textureId;
        }
        int size = mFilters.size();
        int currentTexture = textureId;
        for (int i = 0; i < size; i++) {
            if (mFilters.get(i) != null) {
                currentTexture = mFilters.get(i)
                        .drawFrameBuffer(currentTexture, vertexBuffer, textureBuffer);
            }
        }
        return currentTexture;
    }

    /**
     * 重载初始化FBO方法，用于初始化各个子滤镜的FBO
     * @param width
     * @param height
     */
    @Override
    public void initFrameBuffer(int width, int height) {
        super.initFrameBuffer(width, height);
        if (mFilters.size() == 0) {
            return;
        }
        int size = mFilters.size();
        for (int i = 0; i < size; i++) {
            if (mFilters.get(i) != null) {
                mFilters.get(i).initFrameBuffer(width, height);
            }
        }
    }

    /**
     * 重载释放方法，用于释放各个子滤镜
     */
    @Override
    public void release() {
        super.release();
        int size = mFilters.size();
        for (int i = 0; i < size; i++) {
            if (mFilters.get(i) != null) {
                mFilters.get(i).release();
            }
        }
        mFilters.clear();
    }
}
