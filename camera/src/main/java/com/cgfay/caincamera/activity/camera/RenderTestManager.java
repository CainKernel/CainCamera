package com.cgfay.caincamera.activity.camera;

import com.cgfay.cainfilter.core.FilterManager;
import com.cgfay.cainfilter.glfilter.base.GLBaseImageFilterGroup;
import com.cgfay.cainfilter.glfilter.camera.GLCameraFilter;
import com.cgfay.cainfilter.type.GlFilterGroupType;
import com.cgfay.cainfilter.type.GlFilterType;
import com.cgfay.cainfilter.type.ScaleType;
import com.cgfay.cainfilter.utils.TextureRotationUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * 渲染管理器
 * Created by cain.huang on 2017/11/1.
 */
public final class RenderTestManager {

    private static final String TAG = "RenderTestManager";

    private static RenderTestManager mInstance;

    private static Object mSyncObject = new Object();

    // 是否允许绘制人脸关键点
    private boolean enableDrawPoints = false;

    // 相机输入流滤镜
    private GLCameraFilter mCameraFilter;
    // 实时滤镜组
    private GLBaseImageFilterGroup mRealTimeFilter;
    // 关键点绘制（调试用）
    private FacePointsDrawer mFacePointsDrawer;
    // 顶点和UV坐标缓冲
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;

    // 输入流大小
    private int mTextureWidth;
    private int mTextureHeight;
    // 显示大小
    private int mDisplayWidth;
    private int mDisplayHeight;

    // 显示的缩放裁剪类型
    private ScaleType mScaleType = ScaleType.CENTER_CROP;



    public static RenderTestManager getInstance() {
        if (mInstance == null) {
            mInstance = new RenderTestManager();
        }
        return mInstance;
    }

    private RenderTestManager() {
    }

    /**
     * 初始化
     */
    public void init() {
        // 释放之前的滤镜和缓冲
        releaseBuffers();
        releaseFilters();
        // 初始化滤镜和缓冲
        initBuffers();
        initFilters();
    }

    /**
     * 初始化滤镜
     */
    private void initFilters() {
        mCameraFilter = new GLCameraFilter();
        mFacePointsDrawer = new FacePointsDrawer();
//        mRealTimeFilter = FilterManager.getFilterGroup();
    }

    /**
     * 初始化缓冲
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
        if (mFacePointsDrawer != null) {
            mFacePointsDrawer.release();
            mFacePointsDrawer = null;
        }
        if (mRealTimeFilter != null) {
            mRealTimeFilter.release();
            mRealTimeFilter = null;
        }
    }

    /**
     * 释放缓冲资源
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
     * 渲染Texture的大小
     * @param width
     * @param height
     */
    public void onInputSizeChanged(int width, int height) {
        mTextureWidth = width;
        mTextureHeight = height;
        if (mCameraFilter != null) {
            mCameraFilter.onInputSizeChanged(width, height);
        }
        if (mRealTimeFilter != null) {
            mRealTimeFilter.onInputSizeChanged(width, height);
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
        cameraFilterChanged();
        if (mRealTimeFilter != null) {
            mRealTimeFilter.onDisplayChanged(width, height);
        }
        // 调整视图大小
        adjustViewSize();
    }


    /**
     * 更新filter
     * @param type Filter类型
     */
    public void changeFilter(GlFilterType type) {
        if (mRealTimeFilter != null) {
            mRealTimeFilter.changeFilter(type);
        }
    }

    /**
     * 切换滤镜组
     * @param type
     */
    public void changeFilterGroup(GlFilterGroupType type) {
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
     * 绘制渲染
     * @param textureId
     */
    public void drawFrame(int textureId) {
        if (mRealTimeFilter == null) {
            mCameraFilter.drawFrame(textureId);
        } else {
            int id = mCameraFilter.drawFrameBuffer(textureId);
            mRealTimeFilter.drawFrame(id, mVertexBuffer, mTextureBuffer);
        }
        // 是否绘制点
        if (enableDrawPoints && mFacePointsDrawer != null) {
            mFacePointsDrawer.drawPoints();
        }
    }

    /**
     * 调整由于surface的大小与SurfaceView显示大小不一致带来的显示问题
     */
    private void adjustViewSize() {
        float[] textureCoords = null;
        float[] vertexCoords = null;
        // TODO 这里可以做成镜像翻转的
        float[] textureVertices = TextureRotationUtils.getTextureVertices();
        float[] vertexVertices = TextureRotationUtils.CubeVertices;
        float ratioMax = Math.max((float) mDisplayWidth / mTextureWidth,
                (float) mDisplayHeight / mTextureHeight);
        // 新的宽高
        int imageWidth = Math.round(mTextureWidth * ratioMax);
        int imageHeight = Math.round(mTextureHeight * ratioMax);
        // 获取视图跟texture的宽高比
        float ratioWidth = (float) imageWidth / (float) mTextureWidth;
        float ratioHeight = (float) imageHeight / (float) mTextureHeight;
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
     * 滤镜或视图发生变化时调用
     */
    private void cameraFilterChanged() {
        if (mDisplayWidth != mDisplayHeight) {
            mCameraFilter.onDisplayChanged(mDisplayWidth, mDisplayHeight);
        }
        mCameraFilter.initFramebuffer(mTextureWidth, mTextureHeight);
    }

    //------------------------------ setter and getter ---------------------------------

    /**
     * 设置顶点坐标缓冲
     * @param buffer
     */
    public void setVertexBuffer(FloatBuffer buffer) {
        mVertexBuffer = buffer;
    }

    /**
     *  设置UV坐标缓冲
     * @param buffer
     */
    public void setTextureBuffer(FloatBuffer buffer) {
        mTextureBuffer = buffer;
    }

    /**
     * 设置SurfaceTexture 的Transform矩阵
     * @param matrix
     */
    public void setTransformMatrix(float[] matrix) {
        mCameraFilter.setTextureTransformMatirx(matrix);
    }


}
