package com.cgfay.video.engine;

import android.content.Context;
import android.util.SparseArray;

import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.base.GLImageYUVInputFilter;
import com.cgfay.filterlibrary.glfilter.color.GLImageDynamicColorFilter;
import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColor;
import com.cgfay.filterlibrary.glfilter.effect.GLImageEffectFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;
import com.cgfay.filterlibrary.glfilter.utils.TextureRotationUtils;
import com.cgfay.video.bean.EffectType;

import java.nio.FloatBuffer;

final class VideoRenderManager {

    // 滤镜列表
    private SparseArray<GLImageFilter> mFilterArrays = new SparseArray<GLImageFilter>();

    // 坐标缓冲
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;

    // 输入图像大小
    private int mTextureWidth, mTextureHeight;

    private Context mContext;

    public VideoRenderManager(Context context) {
        mContext = context;
        initBuffers();
    }

    /**
     * 初始化
     */
    public void init() {
        initFilters(mContext);
    }

    /**
     * 释放资源
     */
    public void release() {
        releaseBuffers();
        releaseFilters();
        mContext = null;
    }

    /**
     * 释放滤镜
     */
    private void releaseFilters() {
        for (int i = 0; i < mFilterArrays.size(); i++) {
            if (mFilterArrays.get(i) != null) {
                mFilterArrays.get(i).release();
            }
        }
        mFilterArrays.clear();
    }

    /**
     * 释放缓冲区
     */
    private void releaseBuffers() {
        if (mVertexBuffer != null) {
            mVertexBuffer.clear();
            mVertexBuffer = null;
        }
        if (mTextureBuffer != null) {
            mTextureBuffer.clear();
            mTextureBuffer = null;
        }
    }

    /**
     * 初始化缓冲区
     */
    private void initBuffers() {
        releaseBuffers();
        mVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
    }

    /**
     * 初始化滤镜
     * @param context
     */
    private void initFilters(Context context) {
        releaseFilters();
        // 输入
        mFilterArrays.put(RenderIndex.InputIndex, new GLImageYUVInputFilter(context));
        // 滤镜
        mFilterArrays.put(RenderIndex.FilterIndex, null);
        // 特效
        mFilterArrays.put(RenderIndex.EffectIndex, null);
        // 输出
        mFilterArrays.put(RenderIndex.DisplayIndex, new GLImageFilter(context));
    }

    /**
     * 设置输入纹理大小
     * @param width
     * @param height
     */
    public void setTextureSize(int width, int height) {
        mTextureWidth = width;
        mTextureHeight = height;
        onFilterChanged();
    }

    /**
     * 调整滤镜
     */
    private void onFilterChanged() {
        for (int i = 0; i < mFilterArrays.size(); i++) {
            if (mFilterArrays.get(i) != null) {
                mFilterArrays.get(i).onInputSizeChanged(mTextureWidth, mTextureHeight);
                // 到显示之前都需要创建FBO，这里限定是防止创建多余的FBO，节省GPU资源
                if (i < RenderIndex.DisplayIndex) {
                    mFilterArrays.get(i).initFrameBuffer(mTextureWidth, mTextureHeight);
                }
                mFilterArrays.get(i).onDisplaySizeChanged(mTextureWidth, mTextureHeight);
            }
        }
    }

    /**
     * 切换动态滤镜
     * @param color
     */
    public synchronized void changeDynamicFilter(DynamicColor color) {
        if (mFilterArrays.get(RenderIndex.FilterIndex) != null) {
            mFilterArrays.get(RenderIndex.FilterIndex).release();
            mFilterArrays.put(RenderIndex.FilterIndex, null);
        }
        if (color == null) {
            return;
        }
        GLImageDynamicColorFilter filter = new GLImageDynamicColorFilter(mContext, color);
        filter.onInputSizeChanged(mTextureWidth, mTextureHeight);
        filter.initFrameBuffer(mTextureWidth, mTextureHeight);
        filter.onDisplaySizeChanged(mTextureWidth, mTextureHeight);
        mFilterArrays.put(RenderIndex.FilterIndex, filter);
    }

    /**
     * 切换特效滤镜
     * @param type
     */
    public synchronized void changeEffectFilter(EffectType type) {
        // 释放旧滤镜，防止GPU资源使用过多导致崩溃
        if (mFilterArrays.get(RenderIndex.EffectIndex) != null) {
            mFilterArrays.get(RenderIndex.EffectIndex).release();
            mFilterArrays.put(RenderIndex.EffectIndex, null);
        }
        if (type == null) {
            return;
        }

        GLImageEffectFilter filter = EffectFilterHelper.getInstance().changeEffectFilter(mContext, type);
        filter.onInputSizeChanged(mTextureWidth, mTextureHeight);
        filter.initFrameBuffer(mTextureWidth, mTextureHeight);
        filter.onDisplaySizeChanged(mTextureWidth, mTextureHeight);
        mFilterArrays.put(RenderIndex.EffectIndex, filter);
    }

    /**
     * 设置YUV纹理数据
     * @param ydata
     * @param udata
     * @param vdata
     * @param yLinesize
     * @param uLinesize
     * @param vLinesize
     */
    public synchronized void updateYUVData(byte[] ydata, byte[] udata, byte[] vdata,
                                           int yLinesize, int uLinesize, int vLinesize) {

        if (mFilterArrays.get(RenderIndex.InputIndex) != null) {
            ((GLImageYUVInputFilter)mFilterArrays.get(RenderIndex.InputIndex))
                    .setYUVData(ydata, udata, vdata, yLinesize, uLinesize, vLinesize);
        }
    }

    /**
     * 设置BGRA纹理数据
     * @param data
     * @param linesize
     */
    public synchronized void updateBGRAData(byte[] data, int linesize) {
        if (mFilterArrays.get(RenderIndex.InputIndex) != null) {
            ((GLImageYUVInputFilter)mFilterArrays.get(RenderIndex.InputIndex))
                    .setBGRAData(data, linesize);
        }
    }

    /**
     * 绘制当前时间的纹理
     * @param currentPosition   当前时间
     * @return 当前的纹理id
     */
    public int drawCurrentFrame(long currentPosition) {

        int currentTexture = -1;
        if (mFilterArrays.get(RenderIndex.InputIndex) == null
                || mFilterArrays.get(RenderIndex.DisplayIndex) == null) {
            return currentTexture;
        }

        // 绘制YUV/BGRA输入纹理到FBO
        currentTexture = ((GLImageYUVInputFilter)mFilterArrays.get(RenderIndex.InputIndex)).drawFrameBuffer();

        // 绘制颜色滤镜到FBO
        if (mFilterArrays.get(RenderIndex.FilterIndex) != null) {
            currentTexture = mFilterArrays.get(RenderIndex.FilterIndex).drawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer);
        }

        // 绘制特效滤镜到FBO
        if (mFilterArrays.get(RenderIndex.EffectIndex) != null) {
            ((GLImageEffectFilter) mFilterArrays.get(RenderIndex.EffectIndex)).setCurrentPosition(currentPosition);
            currentTexture = mFilterArrays.get(RenderIndex.EffectIndex).drawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer);
        }

        // 显示输出
        mFilterArrays.get(RenderIndex.DisplayIndex).drawFrame(currentTexture, mVertexBuffer, mTextureBuffer);

        return currentTexture;
    }
}
