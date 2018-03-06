package com.cgfay.cainfilter.core;

import android.opengl.GLES30;

import com.cgfay.cainfilter.glfilter.base.BaseImageFilter;
import com.cgfay.cainfilter.glfilter.base.BaseImageFilterGroup;
import com.cgfay.cainfilter.glfilter.camera.CameraFilter;
import com.cgfay.cainfilter.type.FilterGroupType;
import com.cgfay.cainfilter.type.FilterType;
import com.cgfay.cainfilter.type.ScaleType;
import com.cgfay.cainfilter.utils.TextureRotationUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * 渲染管理器
 * Created by cain.huang on 2017/11/1.
 */
public final class RenderManager {

    private static final String TAG = "RenderManager";

    private static RenderManager mInstance;

    private static Object mSyncObject = new Object();

    // 相机输入流滤镜
    private CameraFilter mCameraFilter;
    // 实时滤镜组
    private BaseImageFilterGroup mRealTimeFilter;
    // 显示输出
    private BaseImageFilter mDisplayFilter;

    // 当前的TextureId
    private int mCurrentTextureId;

    // 输入流大小
    private int mTextureWidth;
    private int mTextureHeight;
    // 显示大小
    private int mDisplayWidth;
    private int mDisplayHeight;

    private ScaleType mScaleType = ScaleType.CENTER_CROP;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;

    public static RenderManager getInstance() {
        if (mInstance == null) {
            mInstance = new RenderManager();
        }
        return mInstance;
    }

    private RenderManager() {
    }

    /**
     * 初始化
     */
    public void init() {
        // 释放之前的滤镜
        releaseFilters();
        releaseBuffers();
        initBuffers();
        // 初始化滤镜
        initFilters();
    }

    /**
     * 初始化缓冲区
     */
    private void initBuffers() {
        mVertexBuffer = ByteBuffer
                .allocateDirect(TextureRotationUtils.CubeVertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVertexBuffer.put(TextureRotationUtils.CubeVertices).position(0);
        mTextureBuffer = ByteBuffer
                .allocateDirect(TextureRotationUtils.getTextureVertices().length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mTextureBuffer.put(TextureRotationUtils.getTextureVertices()).position(0);
    }

    /**
     * 初始化滤镜
     */
    private void initFilters() {
        // 相机输入流
        mCameraFilter = new CameraFilter();
        // 渲染滤镜组
        mRealTimeFilter = FilterManager.getFilterGroup();
        // 显示输出
        mDisplayFilter = FilterManager.getFilter(FilterType.NONE);
    }

    /**
     * 调整由于surface的大小与SurfaceView大小不一致带来的显示问题
     */
    public void adjustViewSize() {
        float[] textureCoords = null;
        float[] vertexCoords = null;
        float[] textureVertices = TextureRotationUtils.getTextureVertices();
        float[] vertexVertices = TextureRotationUtils.CubeVertices;
        float ratioMax = Math.max((float) mDisplayWidth / mTextureWidth,
                (float) mDisplayHeight / mTextureHeight);
        // 新的宽高
        int imageWidth = Math.round(mTextureWidth * ratioMax);
        int imageHeight = Math.round(mTextureHeight * ratioMax);
        // 获取视图跟texture的宽高比
        float ratioWidth = (float) imageWidth / (float) mDisplayWidth;
        float ratioHeight = (float) imageHeight / (float) mDisplayHeight;
        if (mScaleType == ScaleType.CENTER_INSIDE) {
            vertexCoords = new float[] {
                    vertexVertices[0] / ratioHeight, vertexVertices[1] / ratioWidth, vertexVertices[2],
                    vertexVertices[3] / ratioHeight, vertexVertices[4] / ratioWidth, vertexVertices[5],
                    vertexVertices[6] / ratioHeight, vertexVertices[7] / ratioWidth, vertexVertices[8],
                    vertexVertices[9] / ratioHeight, vertexVertices[10] / ratioWidth, vertexVertices[11],
            };
        } else if (mScaleType == ScaleType.CENTER_CROP) {
            float distHorizontal = (1 - 1 / ratioWidth) / 2;
            float distVertical = (1 - 1 / ratioHeight) / 2;
            textureCoords = new float[] {
                    addDistance(textureVertices[0], distVertical), addDistance(textureVertices[1], distHorizontal),
                    addDistance(textureVertices[2], distVertical), addDistance(textureVertices[3], distHorizontal),
                    addDistance(textureVertices[4], distVertical), addDistance(textureVertices[5], distHorizontal),
                    addDistance(textureVertices[6], distVertical), addDistance(textureVertices[7], distHorizontal),
            };
        }
        if (vertexCoords == null) {
            vertexCoords = vertexVertices;
        }
        if (textureCoords == null) {
            textureCoords = textureVertices;
        }
        // 更新VertexBuffer 和 TextureBuffer
        mVertexBuffer.clear();
        mVertexBuffer.put(vertexCoords).position(0);
        mTextureBuffer.clear();
        mTextureBuffer.put(textureCoords).position(0);
    }

    /**
     * 计算距离
     * @param coordinate
     * @param distance
     * @return
     */
    private float addDistance(float coordinate, float distance) {
        return coordinate == 0.0f ? distance : 1 - distance;
    }

    /**
     * 释放资源
     */
    public void release() {
        releaseFilters();
        releaseBuffers();
    }

    /**
     * 释放Filters资源
     */
    private void releaseFilters() {
        if (mCameraFilter != null) {
            mCameraFilter.release();
            mCameraFilter = null;
        }
        if (mRealTimeFilter != null) {
            mRealTimeFilter.release();
            mRealTimeFilter = null;
        }
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
     * 更新TextureBuffer
     */
    public void updateTextureBuffer() {
        if (mCameraFilter != null) {
            mCameraFilter.updateTextureBuffer();
        }
    }

    /**
     * 渲染Texture的大小
     * @param width
     * @param height
     */
    public void onInputSizeChanged(int width, int height) {
        mTextureWidth = width;
        mTextureHeight = height;
        if (mCameraFilter != null) {
            mCameraFilter.onInputSizeChanged(width, height);
            // 在渲染大小发生变化时，需要重新创建FBO
            mCameraFilter.initFramebuffer(mTextureWidth, mTextureHeight);
        }
        if (mRealTimeFilter != null) {
            mRealTimeFilter.onInputSizeChanged(width, height);
        }
        if (mDisplayFilter != null) {
            mDisplayFilter.onInputSizeChanged(width, height);
        }
    }

    /**
     * Surface显示的大小
     * @param width
     * @param height
     */
    public void onDisplaySizeChanged(int width, int height) {
        mDisplayWidth = width;
        mDisplayHeight = height;
        adjustViewSize();
        if (mCameraFilter != null) {
            mCameraFilter.onDisplayChanged(width, height);
        }
        if (mRealTimeFilter != null) {
            mRealTimeFilter.onDisplayChanged(width, height);
        }
        if (mDisplayFilter != null) {
            mDisplayFilter.onDisplayChanged(width, height);
        }
    }

    /**
     * 设置美颜等级（百分比）
     * @param percent 0 ~ 100
     */
    public void setBeautifyLevel(int percent) {
        if (mRealTimeFilter != null) {
            mRealTimeFilter.setBeautifyLevel(percent / 100.0f);
        }
    }

    /**
     * 更新filter
     * @param type Filter类型
     */
    public void changeFilter(FilterType type) {
        if (mRealTimeFilter != null) {
            mRealTimeFilter.changeFilter(type);
        }
    }

    /**
     * 切换滤镜组
     * @param type
     */
    public void changeFilterGroup(FilterGroupType type) {
        synchronized (mSyncObject) {
            if (mRealTimeFilter != null) {
                mRealTimeFilter.release();
            }
            mRealTimeFilter = FilterManager.getFilterGroup(type);
            mRealTimeFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            mRealTimeFilter.onDisplayChanged(mDisplayWidth, mDisplayHeight);
        }
    }

    /**
     * 滤镜或视图发生变化时调用
     */
    public void onFilterChanged() {
        if (mDisplayWidth != mDisplayHeight) {
            mCameraFilter.onDisplayChanged(mDisplayWidth, mDisplayHeight);
        }
        mCameraFilter.initFramebuffer(mTextureWidth, mTextureHeight);
    }


    /**
     * 设置SurfaceTexture TransformMatrix
     * @param matirx
     */
    public void setTextureTransformMatirx(float[] matirx) {
        if (mCameraFilter != null) {
            mCameraFilter.setTextureTransformMatirx(matirx);
        }
    }

    /**
     * 绘制渲染
     * @param textureId
     */
    public void drawFrame(int textureId) {

        mCurrentTextureId = textureId;

        // 将相机流绘制到FBO中
        if (mCameraFilter != null) {
            mCurrentTextureId = mCameraFilter.drawFrameBuffer(mCurrentTextureId);
        }
        // 如果存在滤镜，则绘制滤镜
        if (mRealTimeFilter != null) {
            mCurrentTextureId = mRealTimeFilter.drawFrameBuffer(mCurrentTextureId, mVertexBuffer, mTextureBuffer);
        }
        // 显示输出，需要调整视口大小
        if (mDisplayFilter != null) {
            GLES30.glViewport(0, 0, mDisplayWidth, mDisplayHeight);
            mDisplayFilter.drawFrame(mCurrentTextureId);
        }
    }

    /**
     * 获取当前渲染的Texture
     * @return
     */
    public int getCurrentTexture() {
        return mCurrentTextureId;
    }
}
