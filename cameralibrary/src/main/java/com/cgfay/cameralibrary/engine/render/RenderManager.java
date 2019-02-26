package com.cgfay.cameralibrary.engine.render;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector3;
import com.cgfay.cameralibrary.engine.camera.CameraParam;
import com.cgfay.cameralibrary.engine.model.ScaleType;
import com.cgfay.filterlibrary.glfilter.base.GLImageDepthBlurFilter;
import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.base.GLImageOESInputFilter;
import com.cgfay.filterlibrary.glfilter.base.GLImageVignetteFilter;
import com.cgfay.filterlibrary.glfilter.beauty.GLImageBeautyFilter;
import com.cgfay.filterlibrary.glfilter.beauty.bean.IBeautify;
import com.cgfay.filterlibrary.glfilter.face.GLImageFaceReshapeFilter;
import com.cgfay.filterlibrary.glfilter.face.GLImageFacePointsFilter;
import com.cgfay.filterlibrary.glfilter.color.GLImageDynamicColorFilter;
import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColor;
import com.cgfay.filterlibrary.glfilter.makeup.GLImageMakeupFilter;
import com.cgfay.filterlibrary.glfilter.makeup.bean.DynamicMakeup;
import com.cgfay.filterlibrary.glfilter.multiframe.GLImageFrameEdgeBlurFilter;
import com.cgfay.filterlibrary.glfilter.stickers.GLImageDynamicStickerFilter;
import com.cgfay.filterlibrary.glfilter.stickers.GestureHelp;
import com.cgfay.filterlibrary.glfilter.stickers.StaticStickerNormalFilter;
import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicSticker;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;
import com.cgfay.filterlibrary.glfilter.utils.TextureRotationUtils;
import com.cgfay.landmarklibrary.LandmarkEngine;

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
    // 用于显示裁剪的纹理顶点缓冲
    private FloatBuffer mDisplayVertexBuffer;
    private FloatBuffer mDisplayTextureBuffer;

    // 视图宽高
    private int mViewWidth, mViewHeight;
    // 输入图像大小
    private int mTextureWidth, mTextureHeight;

    // 相机参数
    private CameraParam mCameraParam;
    // 上下文
    private Context mContext;

    /**
     * 初始化
     */
    public void init(Context context) {
        initBuffers();
        initFilters(context);
        mContext = context;
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
        if (mDisplayVertexBuffer != null) {
            mDisplayVertexBuffer.clear();
            mDisplayVertexBuffer = null;
        }
        if (mDisplayTextureBuffer != null) {
            mDisplayTextureBuffer.clear();
            mDisplayTextureBuffer = null;
        }
    }

    /**
     * 初始化缓冲区
     */
    private void initBuffers() {
        releaseBuffers();
        mDisplayVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mDisplayTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
        mVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
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
        mFilterArrays.put(RenderIndex.BeautyIndex, new GLImageBeautyFilter(context));
        // 彩妆滤镜
        mFilterArrays.put(RenderIndex.MakeupIndex, new GLImageMakeupFilter(context, null));
        // 美型滤镜
        mFilterArrays.put(RenderIndex.FaceAdjustIndex, new GLImageFaceReshapeFilter(context));
        // LUT/颜色滤镜
        mFilterArrays.put(RenderIndex.FilterIndex, null);
        // 贴纸资源滤镜
        mFilterArrays.put(RenderIndex.ResourceIndex, null);
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
     * 是否切换边框模糊
     * @param enableEdgeBlur
     */
    public synchronized void changeEdgeBlurFilter(boolean enableEdgeBlur) {
        if (enableEdgeBlur) {
            mFilterArrays.get(RenderIndex.DisplayIndex).release();
            GLImageFrameEdgeBlurFilter filter = new GLImageFrameEdgeBlurFilter(mContext);
            filter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            filter.onDisplaySizeChanged(mViewWidth, mViewHeight);
            mFilterArrays.put(RenderIndex.DisplayIndex, filter);
        } else {
            mFilterArrays.get(RenderIndex.DisplayIndex).release();
            GLImageFilter filter = new GLImageFilter(mContext);
            filter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            filter.onDisplaySizeChanged(mViewWidth, mViewHeight);
            mFilterArrays.put(RenderIndex.DisplayIndex, filter);
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
        filter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        mFilterArrays.put(RenderIndex.FilterIndex, filter);
    }

    /**
     * 切换动态滤镜
     * @param dynamicMakeup
     */
    public synchronized void changeDynamicMakeup(DynamicMakeup dynamicMakeup) {
        if (mFilterArrays.get(RenderIndex.MakeupIndex) != null) {
            ((GLImageMakeupFilter)mFilterArrays.get(RenderIndex.MakeupIndex)).changeMakeupData(dynamicMakeup);
        } else {
            GLImageMakeupFilter filter = new GLImageMakeupFilter(mContext, dynamicMakeup);
            filter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            filter.initFrameBuffer(mTextureWidth, mTextureHeight);
            filter.onDisplaySizeChanged(mViewWidth, mViewHeight);
            mFilterArrays.put(RenderIndex.MakeupIndex, filter);
        }
    }

    /**
     * 切换动态资源
     * @param color
     */
    public synchronized void changeDynamicResource(DynamicColor color) {
        if (mFilterArrays.get(RenderIndex.ResourceIndex) != null) {
            mFilterArrays.get(RenderIndex.ResourceIndex).release();
            mFilterArrays.put(RenderIndex.ResourceIndex, null);
        }
        if (color == null) {
            return;
        }
        GLImageDynamicColorFilter filter = new GLImageDynamicColorFilter(mContext, color);
        filter.onInputSizeChanged(mTextureWidth, mTextureHeight);
        filter.initFrameBuffer(mTextureWidth, mTextureHeight);
        filter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        mFilterArrays.put(RenderIndex.ResourceIndex, filter);
    }

    /**
     * 切换动态资源
     * @param sticker
     */
    public synchronized void changeDynamicResource(DynamicSticker sticker) {
        // 释放旧滤镜
        if (mFilterArrays.get(RenderIndex.ResourceIndex) != null) {
            mFilterArrays.get(RenderIndex.ResourceIndex).release();
            mFilterArrays.put(RenderIndex.ResourceIndex, null);
        }
        if (sticker == null) {
            return;
        }
        GLImageDynamicStickerFilter filter = new GLImageDynamicStickerFilter(mContext, sticker);
        // 设置输入输入大小，初始化fbo等
        filter.onInputSizeChanged(mTextureWidth, mTextureHeight);
        filter.initFrameBuffer(mTextureWidth, mTextureHeight);
        filter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        mFilterArrays.put(RenderIndex.ResourceIndex, filter);
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
            ((GLImageOESInputFilter)mFilterArrays.get(RenderIndex.CameraIndex)).setTextureTransformMatrix(mMatrix);
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
                currentTexture = mFilterArrays.get(RenderIndex.BeautyIndex).drawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer);
            }

            // 彩妆滤镜
            if (mFilterArrays.get(RenderIndex.MakeupIndex) != null) {
                currentTexture = mFilterArrays.get(RenderIndex.MakeupIndex).drawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer);
            }

            // 美型滤镜
            if (mFilterArrays.get(RenderIndex.FaceAdjustIndex) != null) {
                if (mFilterArrays.get(RenderIndex.FaceAdjustIndex) instanceof IBeautify) {
                    ((IBeautify) mFilterArrays.get(RenderIndex.FaceAdjustIndex)).onBeauty(mCameraParam.beauty);
                }
                currentTexture = mFilterArrays.get(RenderIndex.FaceAdjustIndex).drawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer);
            }

            // 绘制颜色滤镜
            if (mFilterArrays.get(RenderIndex.FilterIndex) != null) {
                currentTexture = mFilterArrays.get(RenderIndex.FilterIndex).drawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer);
            }

            // 资源滤镜，可以是贴纸、滤镜甚至是彩妆类型
            if (mFilterArrays.get(RenderIndex.ResourceIndex) != null) {
                currentTexture = mFilterArrays.get(RenderIndex.ResourceIndex).drawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer);
            }

            // 景深
            if (mFilterArrays.get(RenderIndex.DepthBlurIndex) != null) {
                mFilterArrays.get(RenderIndex.DepthBlurIndex).setFilterEnable(mCameraParam.enableDepthBlur);
                currentTexture = mFilterArrays.get(RenderIndex.DepthBlurIndex).drawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer);
            }

            // 暗角
            if (mFilterArrays.get(RenderIndex.VignetteIndex) != null) {
                mFilterArrays.get(RenderIndex.VignetteIndex).setFilterEnable(mCameraParam.enableVignette);
                currentTexture = mFilterArrays.get(RenderIndex.VignetteIndex).drawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer);
            }
        }

        // 显示输出，需要调整视口大小
        mFilterArrays.get(RenderIndex.DisplayIndex).drawFrame(currentTexture, mDisplayVertexBuffer, mDisplayTextureBuffer);

        return currentTexture;
    }

    /**
     * 绘制调试用的人脸关键点
     * @param mCurrentTexture
     */
    public void drawFacePoint(int mCurrentTexture) {
        if (mFilterArrays.get(RenderIndex.FacePointIndex) != null) {
            if (mCameraParam.drawFacePoints && LandmarkEngine.getInstance().hasFace()) {
                mFilterArrays.get(RenderIndex.FacePointIndex).drawFrame(mCurrentTexture, mDisplayVertexBuffer, mDisplayTextureBuffer);
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
                // 到显示之前都需要创建FBO，这里限定是防止创建多余的FBO，节省GPU资源
                if (i < RenderIndex.DisplayIndex) {
                    mFilterArrays.get(i).initFrameBuffer(mTextureWidth, mTextureHeight);
                }
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
        mDisplayVertexBuffer.clear();
        mDisplayVertexBuffer.put(vertexCoord).position(0);
        mDisplayTextureBuffer.clear();
        mDisplayTextureBuffer.put(textureCoord).position(0);
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

    public static final Vector3 tempVec=new Vector3();
    public StaticStickerNormalFilter touchDown(MotionEvent e) {

        if (mFilterArrays.get(RenderIndex.ResourceIndex) != null) {
          GLImageFilter  glImageFilter = mFilterArrays.get(RenderIndex.ResourceIndex);
          if(glImageFilter instanceof GLImageDynamicStickerFilter) {
              GLImageDynamicStickerFilter glImageDynamicStickerFilter= (GLImageDynamicStickerFilter) glImageFilter;
              tempVec.set(e.getX(), e.getY(), 0);
              StaticStickerNormalFilter staticStickerNormalFilter=GestureHelp.hit(tempVec,glImageDynamicStickerFilter.getmFilters());
              if(staticStickerNormalFilter!=null){
                  Log.d("touchSticker","找到贴纸");
              }else{
                  Log.d("touchSticker","没有贴纸");
              }
              return staticStickerNormalFilter;
          }
        }

        return null;

    }
}
