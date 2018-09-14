package com.cgfay.cameralibrary.engine.render;

import android.content.Context;

import com.cgfay.cameralibrary.engine.camera.CameraParam;
import com.cgfay.cameralibrary.engine.model.ScaleType;
import com.cgfay.filterlibrary.glfilter.GLImageFilterManager;
import com.cgfay.filterlibrary.glfilter.advanced.GLImageDepthBlurFilter;
import com.cgfay.filterlibrary.glfilter.advanced.GLImageOESInputFilter;
import com.cgfay.filterlibrary.glfilter.advanced.GLImageVignetteFilter;
import com.cgfay.filterlibrary.glfilter.advanced.beauty.GLImageBeautyFilter;
import com.cgfay.filterlibrary.glfilter.advanced.beauty.GLImageRealTimeBeautyFilter;
import com.cgfay.filterlibrary.glfilter.advanced.face.GLImageFaceAdjustFilter;
import com.cgfay.filterlibrary.glfilter.advanced.face.GLImageFacePointsFilter;
import com.cgfay.filterlibrary.glfilter.advanced.makeup.GLImageMakeupFilter;
import com.cgfay.filterlibrary.glfilter.advanced.sticker.GLImageStickerFilter;
import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.model.IBeautify;
import com.cgfay.filterlibrary.glfilter.model.IFacePoints;
import com.cgfay.filterlibrary.glfilter.utils.GLImageFilterType;
import com.cgfay.filterlibrary.glfilter.utils.TextureRotationUtils;

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


    // 相机输入流滤镜
    private GLImageOESInputFilter mInputFilter;
    // 美颜滤镜
    private GLImageFilter mBeautyFilter;
    // 彩妆滤镜
    private GLImageMakeupFilter mMakeupFilter;
    // 瘦脸滤镜
    private GLImageFilter mFaceAdjustFilter;
    // 动态贴纸滤镜
    private GLImageStickerFilter mStickerFilter;
    // LUT滤镜
    private GLImageFilter mColorFilter;
    // 景深滤镜
    private GLImageDepthBlurFilter mDepthBlurFilter;
    // 暗角滤镜
    private GLImageVignetteFilter mVignetteFilter;
    // 显示输出
    private GLImageFilter mDisplayFilter;
    // 人脸关键点调试
    private GLImageFacePointsFilter mFacePointsFilter;

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
        if (mInputFilter != null) {
            mInputFilter.release();
            mInputFilter = null;
        }
        if (mBeautyFilter != null) {
            mBeautyFilter.release();
            mBeautyFilter = null;
        }
        if (mMakeupFilter != null) {
            mMakeupFilter.release();
            mMakeupFilter = null;
        }
        if (mFaceAdjustFilter != null) {
            mFaceAdjustFilter.release();
            mFaceAdjustFilter = null;
        }
        if (mStickerFilter != null) {
            mStickerFilter.release();
            mStickerFilter = null;
        }
        if (mColorFilter != null) {
            mColorFilter.release();
            mColorFilter = null;
        }
        if (mDepthBlurFilter != null) {
            mDepthBlurFilter.release();
            mDepthBlurFilter = null;
        }
        if (mVignetteFilter != null) {
            mVignetteFilter.release();
            mVignetteFilter = null;
        }
        if (mDisplayFilter != null) {
            mDisplayFilter.release();
            mDisplayFilter = null;
        }
        if (mFacePointsFilter != null) {
            mFacePointsFilter.release();
            mFacePointsFilter = null;
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
        // 相机输入流
        mInputFilter = new GLImageOESInputFilter(context);
        // 美颜滤镜
        mBeautyFilter = new GLImageRealTimeBeautyFilter(context);
        // 彩妆滤镜
        mMakeupFilter = new GLImageMakeupFilter(context);
        // 美型滤镜
        mFaceAdjustFilter = new GLImageFaceAdjustFilter(context);
        // 动态贴纸滤镜
        mStickerFilter = new GLImageStickerFilter(context);
        // 景深滤镜
        mDepthBlurFilter = new GLImageDepthBlurFilter(context);
        // 暗角滤镜
        mVignetteFilter = new GLImageVignetteFilter(context);
        // 显示输出
        mDisplayFilter = new GLImageFilter(context);
        // 人脸关键点调试
        mFacePointsFilter = new GLImageFacePointsFilter(context);
    }

    /**
     * 切换滤镜
     * @param context
     * @param type
     */
    public void changeFilter(Context context, GLImageFilterType type) {
        if (mColorFilter != null) {
            mColorFilter.release();
            mColorFilter = null;
        }
        mColorFilter = GLImageFilterManager.getFilter(context, type);
        mColorFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
        mColorFilter.initFrameBuffer(mTextureWidth, mTextureHeight);
        mColorFilter.onDisplaySizeChanged(mViewWidth, mViewHeight);
    }

    /**
     * 绘制纹理
     * @param inputTexture
     * @param mMatrix
     * @return
     */
    public int drawFrame(int inputTexture, float[] mMatrix) {
        int currentTexture = inputTexture;
        if (mInputFilter != null) {
            mInputFilter.setTextureTransformMatirx(mMatrix);
            currentTexture = mInputFilter.drawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer);
        }
        // 如果处于对比状态，不做处理
        if (!mCameraParam.showCompare) {
            // 美颜滤镜
            if (mBeautyFilter != null) {
                if (mBeautyFilter instanceof IBeautify
                        && mCameraParam.beauty != null) {
                    ((IBeautify) mBeautyFilter).onBeauty(mCameraParam.beauty);
                }
                currentTexture = mBeautyFilter.drawFrameBuffer(currentTexture);
            }
            // 彩妆滤镜
            if (mMakeupFilter != null) {
                currentTexture = mMakeupFilter.drawFrameBuffer(currentTexture);
            }
            // 美型滤镜
            if (mFaceAdjustFilter != null) {
                if (mFaceAdjustFilter instanceof IBeautify) {
                    ((IBeautify) mFaceAdjustFilter).onBeauty(mCameraParam.beauty);
                }
                if (mFaceAdjustFilter instanceof IFacePoints) {
                    ((IFacePoints) mFaceAdjustFilter).onFacePoints(mCameraParam.facePoints);
                }
                currentTexture = mFaceAdjustFilter.drawFrameBuffer(currentTexture);
            }
            // 动态贴纸滤镜
            if (mStickerFilter != null) {
                currentTexture = mStickerFilter.drawFrameBuffer(currentTexture);
            }

            // 绘制LUT滤镜
            if (mColorFilter != null) {
                currentTexture = mColorFilter.drawFrameBuffer(currentTexture);
            }

            // 景深
            if (mDepthBlurFilter != null) {
                mDepthBlurFilter.setFilterEnable(mCameraParam.enableDepthBlur);
                currentTexture = mDepthBlurFilter.drawFrameBuffer(currentTexture);
            }

            // 暗角
            if (mVignetteFilter != null) {
                mVignetteFilter.setFilterEnable(mCameraParam.enableVignette);
                currentTexture = mVignetteFilter.drawFrameBuffer(currentTexture);
            }
        }

        // 显示输出，需要调整视口大小
        if (mDisplayFilter != null) {
            mDisplayFilter.drawFrame(currentTexture);
        }

        return currentTexture;
    }

    /**
     * 绘制调试用的人脸关键点
     * @param mCurrentTexture
     */
    public void drawFacePoint(int mCurrentTexture) {
        if (mFacePointsFilter != null) {
            if (mCameraParam.facePointsListener != null
                    && mCameraParam.facePointsListener.showFacePoints()) {
                mFacePointsFilter.setFacePoints(mCameraParam.facePointsListener.getDebugFacePoints());
                mFacePointsFilter.drawFrame(mCurrentTexture);
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
        if (mInputFilter != null) {
            mInputFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            mInputFilter.initFrameBuffer(mTextureWidth, mTextureHeight);
            mInputFilter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        }
        if (mBeautyFilter != null) {
            mBeautyFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            mBeautyFilter.initFrameBuffer(mTextureWidth, mTextureHeight);
            mBeautyFilter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        }
        if (mMakeupFilter != null) {
            mMakeupFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            mMakeupFilter.initFrameBuffer(mTextureWidth, mTextureHeight);
            mMakeupFilter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        }
        if (mFaceAdjustFilter != null) {
            mFaceAdjustFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            mFaceAdjustFilter.initFrameBuffer(mTextureWidth, mTextureHeight);
            mFaceAdjustFilter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        }
        if (mStickerFilter != null) {
            mStickerFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            mStickerFilter.initFrameBuffer(mTextureWidth, mTextureHeight);
            mStickerFilter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        }
        if (mColorFilter != null) {
            mColorFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            mColorFilter.initFrameBuffer(mTextureWidth, mTextureHeight);
            mColorFilter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        }
        if (mDepthBlurFilter != null) {
            mDepthBlurFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            mDepthBlurFilter.initFrameBuffer(mTextureWidth, mTextureHeight);
            mDepthBlurFilter.onDisplaySizeChanged(mTextureWidth, mTextureHeight);
        }
        if (mVignetteFilter != null) {
            mVignetteFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            mVignetteFilter.initFrameBuffer(mTextureWidth, mTextureHeight);
            mVignetteFilter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        }
        if (mDisplayFilter != null) {
            mDisplayFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            mDisplayFilter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        }
        if (mFacePointsFilter != null) {
            mFacePointsFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            mFacePointsFilter.onDisplaySizeChanged(mViewWidth, mViewHeight);
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
