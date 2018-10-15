package com.cgfay.cameralibrary.engine.render;

import android.content.Context;
import android.util.SparseArray;

import com.cgfay.cameralibrary.engine.camera.CameraParam;
import com.cgfay.cameralibrary.engine.model.ScaleType;
import com.cgfay.filterlibrary.glfilter.GLImageFilterManager;
import com.cgfay.filterlibrary.glfilter.advanced.GLImageDepthBlurFilter;
import com.cgfay.filterlibrary.glfilter.advanced.GLImageOESInputFilter;
import com.cgfay.filterlibrary.glfilter.advanced.GLImageVignetteFilter;
import com.cgfay.filterlibrary.glfilter.advanced.beauty.GLImageRealTimeBeautyFilter;
import com.cgfay.filterlibrary.glfilter.advanced.face.GLImageFaceAdjustFilter;
import com.cgfay.filterlibrary.glfilter.advanced.face.GLImageFacePointsFilter;
import com.cgfay.filterlibrary.glfilter.advanced.makeup.GLImageMakeupFilter;
import com.cgfay.filterlibrary.glfilter.advanced.sticker.GLImageStickerFilter;
import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.model.IBeautify;
import com.cgfay.filterlibrary.glfilter.utils.GLImageFilterType;
import com.cgfay.filterlibrary.glfilter.utils.TextureRotationUtils;
import com.cgfay.landmarklibrary.LandmarkEngine;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * 渲染管理器
 */
public final class RenderManager {

    private static class RenderManagerHolder {
        public static RenderManager instance = new RenderManager();
    }

    private RenderManager() {
        mCameraParam = CameraParam.getInstance();
    }

    public static RenderManager getInstance() {
        return RenderManagerHolder.instance;
    }

    // 滤镜列表
    private SparseArray<GLImageFilter> mFilterArrays = new SparseArray<GLImageFilter>();

    // 坐标缓冲
    private ScaleType mScaleType = ScaleType.CENTER_CROP;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;

    // 视图宽高
    private int mViewWidth, mViewHeight;
    // 输入图像大小
    private int mTextureWidth, mTextureHeight;

    // 相机参数
    private CameraParam mCameraParam;

    /**
     * 初始化
     */
    public void init(Context context) {
        initBuffers();
        initFilters(context);
    }

    /**
     * 释放资源
     */
    public void release() {
        releaseBuffers();
        releaseFilters();
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
        mVertexBuffer = ByteBuffer
                .allocateDirect(TextureRotationUtils.CubeVertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVertexBuffer.put(TextureRotationUtils.CubeVertices).position(0);
        mTextureBuffer = ByteBuffer
                .allocateDirect(TextureRotationUtils.TextureVertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mTextureBuffer.put(TextureRotationUtils.TextureVertices).position(0);
    }

    /**
     * 初始化滤镜
     * @param context
     */
    private void initFilters(Context context) {
        releaseFilters();
        // 相机输入滤镜
        mFilterArrays.put(RenderIndex.CameraIndex, new GLImageOESInputFilter(context));
        // 美颜滤镜
        mFilterArrays.put(RenderIndex.BeautyIndex, new GLImageRealTimeBeautyFilter(context));
        // 彩妆滤镜
        mFilterArrays.put(RenderIndex.MakeupIndex, new GLImageMakeupFilter(context));
        // 美型滤镜
        mFilterArrays.put(RenderIndex.FaceAdjustIndex, new GLImageFaceAdjustFilter(context));
        // 动态贴纸滤镜
        mFilterArrays.put(RenderIndex.StickerIndex, new GLImageStickerFilter(context));
        // LUT滤镜
//        mFilterArrays.put(RenderIndex.ColorIndex, new GLImageFilter(context));
        // 景深滤镜
        mFilterArrays.put(RenderIndex.DepthBlurIndex, new GLImageDepthBlurFilter(context));
        // 暗角滤镜
        mFilterArrays.put(RenderIndex.VignetteIndex, new GLImageVignetteFilter(context));
        // 显示输出
        mFilterArrays.put(RenderIndex.DisplayIndex, new GLImageFilter(context));
        // 人脸关键点调试
        mFilterArrays.put(RenderIndex.FacePointIndex, new GLImageFacePointsFilter(context));
    }

    /**
     * 切换滤镜
     * @param context
     * @param type
     */
    public void changeFilter(Context context, GLImageFilterType type) {
        // TODO 有机会这里改成LUT滤镜，剔除旧的滤镜
        if (mFilterArrays.get(RenderIndex.ColorIndex) != null) {
            mFilterArrays.get(RenderIndex.ColorIndex).release();
        }
        GLImageFilter filter = GLImageFilterManager.getFilter(context, type);
        filter.onInputSizeChanged(mTextureWidth, mTextureHeight);
        filter.initFrameBuffer(mTextureWidth, mTextureHeight);
        filter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        mFilterArrays.put(RenderIndex.ColorIndex, filter);
    }

    /**
     * 绘制纹理
     * @param inputTexture
     * @param mMatrix
     * @return
     */
    public int drawFrame(int inputTexture, float[] mMatrix) {
        int currentTexture = inputTexture;
        if (mFilterArrays.get(RenderIndex.CameraIndex) == null
                || mFilterArrays.get(RenderIndex.DisplayIndex) == null) {
            return currentTexture;
        }
        if (mFilterArrays.get(RenderIndex.CameraIndex) instanceof GLImageOESInputFilter) {
            ((GLImageOESInputFilter)mFilterArrays.get(RenderIndex.CameraIndex)).setTextureTransformMatirx(mMatrix);
        }
        currentTexture = mFilterArrays.get(RenderIndex.CameraIndex)
                .drawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer);
        // 如果处于对比状态，不做处理
        if (!mCameraParam.showCompare) {
            // 美颜滤镜
            if (mFilterArrays.get(RenderIndex.BeautyIndex) != null) {
                if (mFilterArrays.get(RenderIndex.BeautyIndex) instanceof IBeautify
                        && mCameraParam.beauty != null) {
                    ((IBeautify) mFilterArrays.get(RenderIndex.BeautyIndex)).onBeauty(mCameraParam.beauty);
                }
                currentTexture = mFilterArrays.get(RenderIndex.BeautyIndex).drawFrameBuffer(currentTexture);
            }
            // 彩妆滤镜
            if (mFilterArrays.get(RenderIndex.MakeupIndex) != null) {
                currentTexture = mFilterArrays.get(RenderIndex.MakeupIndex).drawFrameBuffer(currentTexture);
            }
            // 美型滤镜
            if (mFilterArrays.get(RenderIndex.FaceAdjustIndex) != null) {
                if (mFilterArrays.get(RenderIndex.FaceAdjustIndex) instanceof IBeautify) {
                    ((IBeautify) mFilterArrays.get(RenderIndex.FaceAdjustIndex)).onBeauty(mCameraParam.beauty);
                }
                currentTexture = mFilterArrays.get(RenderIndex.FaceAdjustIndex).drawFrameBuffer(currentTexture);
            }
            // 动态贴纸滤镜
            if (mFilterArrays.get(RenderIndex.StickerIndex) != null) {
                currentTexture = mFilterArrays.get(RenderIndex.StickerIndex).drawFrameBuffer(currentTexture);
            }

            // 绘制LUT滤镜
            if (mFilterArrays.get(RenderIndex.ColorIndex) != null) {
                currentTexture = mFilterArrays.get(RenderIndex.ColorIndex).drawFrameBuffer(currentTexture);
            }

            // 景深
            if (mFilterArrays.get(RenderIndex.DepthBlurIndex) != null) {
                mFilterArrays.get(RenderIndex.DepthBlurIndex).setFilterEnable(mCameraParam.enableDepthBlur);
                currentTexture = mFilterArrays.get(RenderIndex.DepthBlurIndex).drawFrameBuffer(currentTexture);
            }

            // 暗角
            if (mFilterArrays.get(RenderIndex.VignetteIndex) != null) {
                mFilterArrays.get(RenderIndex.VignetteIndex).setFilterEnable(mCameraParam.enableVignette);
                currentTexture = mFilterArrays.get(RenderIndex.VignetteIndex).drawFrameBuffer(currentTexture);
            }
        }

        // 显示输出，需要调整视口大小
        mFilterArrays.get(RenderIndex.DisplayIndex).drawFrame(currentTexture);

        return currentTexture;
    }

    /**
     * 绘制调试用的人脸关键点
     * @param mCurrentTexture
     */
    public void drawFacePoint(int mCurrentTexture) {
        if (mFilterArrays.get(RenderIndex.FacePointIndex) != null) {
            if (mCameraParam.drawFacePoints && LandmarkEngine.getInstance().hasFace()) {
                mFilterArrays.get(RenderIndex.FacePointIndex).drawFrame(mCurrentTexture);
            }
        }
    }

    /**
     * 设置输入纹理大小
     * @param width
     * @param height
     */
    public void setTextureSize(int width, int height) {
        mTextureWidth = width;
        mTextureHeight = height;
    }

    /**
     * 设置纹理显示大小
     * @param width
     * @param height
     */
    public void setDisplaySize(int width, int height) {
        mViewWidth = width;
        mViewHeight = height;
        adjustCoordinateSize();
        onFilterChanged();
    }

    /**
     * 调整滤镜
     */
    private void onFilterChanged() {
        for (int i = 0; i < mFilterArrays.size(); i++) {
            if (mFilterArrays.get(i) != null) {
                mFilterArrays.get(i).onInputSizeChanged(mTextureWidth, mTextureHeight);
                mFilterArrays.get(i).initFrameBuffer(mTextureWidth, mTextureHeight);
                mFilterArrays.get(i).onDisplaySizeChanged(mViewWidth, mViewHeight);
            }
        }
    }

    /**
     * 调整由于surface的大小与SurfaceView大小不一致带来的显示问题
     */
    private void adjustCoordinateSize() {
        float[] textureCoord = null;
        float[] vertexCoord = null;
        float[] textureVertices = TextureRotationUtils.TextureVertices;
        float[] vertexVertices = TextureRotationUtils.CubeVertices;
        float ratioMax = Math.max((float) mViewWidth / mTextureWidth,
                (float) mViewHeight / mTextureHeight);
        // 新的宽高
        int imageWidth = Math.round(mTextureWidth * ratioMax);
        int imageHeight = Math.round(mTextureHeight * ratioMax);
        // 获取视图跟texture的宽高比
        float ratioWidth = (float) imageWidth / (float) mViewWidth;
        float ratioHeight = (float) imageHeight / (float) mViewHeight;
        if (mScaleType == ScaleType.CENTER_INSIDE) {
            vertexCoord = new float[] {
                    vertexVertices[0] / ratioHeight, vertexVertices[1] / ratioWidth, vertexVertices[2],
                    vertexVertices[3] / ratioHeight, vertexVertices[4] / ratioWidth, vertexVertices[5],
                    vertexVertices[6] / ratioHeight, vertexVertices[7] / ratioWidth, vertexVertices[8],
                    vertexVertices[9] / ratioHeight, vertexVertices[10] / ratioWidth, vertexVertices[11],
            };
        } else if (mScaleType == ScaleType.CENTER_CROP) {
            float distHorizontal = (1 - 1 / ratioWidth) / 2;
            float distVertical = (1 - 1 / ratioHeight) / 2;
            textureCoord = new float[] {
                    addDistance(textureVertices[0], distVertical), addDistance(textureVertices[1], distHorizontal),
                    addDistance(textureVertices[2], distVertical), addDistance(textureVertices[3], distHorizontal),
                    addDistance(textureVertices[4], distVertical), addDistance(textureVertices[5], distHorizontal),
                    addDistance(textureVertices[6], distVertical), addDistance(textureVertices[7], distHorizontal),
            };
        }
        if (vertexCoord == null) {
            vertexCoord = vertexVertices;
        }
        if (textureCoord == null) {
            textureCoord = textureVertices;
        }
        // 更新VertexBuffer 和 TextureBuffer
        mVertexBuffer.clear();
        mVertexBuffer.put(vertexCoord).position(0);
        mTextureBuffer.clear();
        mTextureBuffer.put(textureCoord).position(0);
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
}
