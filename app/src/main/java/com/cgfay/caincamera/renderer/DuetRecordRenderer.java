package com.cgfay.caincamera.renderer;

import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.opengl.EGL14;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.cgfay.caincamera.presenter.RecordPresenter;
import com.cgfay.filter.glfilter.adjust.GLImageMirrorFilter;
import com.cgfay.filter.glfilter.base.GLImageFilter;
import com.cgfay.filter.glfilter.base.GLImageOESInputFilter;
import com.cgfay.filter.glfilter.utils.OpenGLUtils;
import com.cgfay.filter.glfilter.utils.TextureRotationUtils;

import com.cgfay.picker.model.MediaData;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 同框录制渲染器
 */
public class DuetRecordRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "DuetRecordRenderer";

    private GLImageOESInputFilter mInputFilter; // OES输入滤镜
    private GLImageDuetFilter mDuetFilter;  // 颜色滤镜
    private GLImageFilter mImageFilter; // 输出滤镜
    // 顶点坐标缓冲
    private FloatBuffer mVertexBuffer;
    // 纹理坐标缓冲
    private FloatBuffer mTextureBuffer;
    // 预览顶点坐标缓冲
    private FloatBuffer mDisplayVertexBuffer;
    // 预览纹理坐标缓冲
    private FloatBuffer mDisplayTextureBuffer;
    // 输入纹理大小
    protected int mTextureWidth;
    protected int mTextureHeight;
    // 控件视图大小
    protected int mViewWidth;
    protected int mViewHeight;
    // 输入纹理
    private int mInputTexture = OpenGLUtils.GL_NOT_TEXTURE;
    private volatile boolean mNeedToAttach;
    private WeakReference<SurfaceTexture> mWeakSurfaceTexture;
    private float[] mMatrix = new float[16];
    // presenter
    private final WeakReference<RecordPresenter> mWeakPresenter;

    // 同框类型
    private DuetType mDuetType;
    // 同框变换矩阵
    private float[] mMVPMatrix = new float[16];
    // 是否翻转
    private boolean mFlip;
    // 同框视频对象
    private MediaData mDuetVideo;
    // 视频宽度
    private int mVideoWidth;
    // 视频高度
    private int mVideoHeight;
    // 视频输入纹理
    private int mVideoInputTexture;
    // 视频输入纹理
    private GLImageOESInputFilter mVideoInputFilter;
    // 视频顶点坐标缓冲
    private FloatBuffer mDuetVertexBuffer;
    // 视频纹理坐标缓冲
    private FloatBuffer mDuetTextureBuffer;
    // Surface
    private Surface mVideoSurface;
    // 视频SurfaceTexture
    private SurfaceTexture mVideoSurfaceTexture;
    // 视频播放器
    private MediaPlayer mMediaPlayer;

    public DuetRecordRenderer(RecordPresenter presenter) {
        mWeakPresenter = new WeakReference<>(presenter);
        mDuetType = DuetType.DUET_TYPE_NONE;
        mVideoInputTexture = OpenGLUtils.GL_NOT_TEXTURE;
        Matrix.setIdentityM(mMVPMatrix, 0);
    }

    /**
     * 设置同框视频
     */
    public void setDuetVideo(@NonNull MediaData mediaData) {
        mDuetVideo = mediaData;
        mDuetType = DuetType.DUET_TYPE_LEFT_RIGHT;
        if (mediaData.getOrientation() == 90 || mediaData.getOrientation() == 270) {
            mVideoWidth = mediaData.getHeight();
            mVideoHeight = mediaData.getWidth();
        } else {
            mVideoWidth = mediaData.getWidth();
            mVideoHeight = mediaData.getHeight();
        }
        Log.d(TAG, "setDuetVideo - video width: " + mVideoWidth + ", video height: " + mVideoHeight);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
        mDuetVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mDuetTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
        mDisplayVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mDisplayTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);

        GLES30.glDisable(GL10.GL_DITHER);
        GLES30.glClearColor(0,0, 0, 0);
        GLES30.glEnable(GL10.GL_CULL_FACE);
        GLES30.glEnable(GL10.GL_DEPTH_TEST);
        initFilters();
        if (mWeakPresenter.get() != null) {
            mWeakPresenter.get().onBindSharedContext(EGL14.eglGetCurrentContext());
        }
        initMediaPlayer();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mViewWidth = width;
        mViewHeight = height;
        onFilterSizeChanged();
        adjustDisplayCoordinateSize();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mWeakSurfaceTexture == null || mWeakSurfaceTexture.get() == null) {
            return;
        }

        // 更新纹理
        long timeStamp = 0;
        synchronized (this) {
            final SurfaceTexture surfaceTexture = mWeakSurfaceTexture.get();
            updateSurfaceTexture(surfaceTexture);
            timeStamp = surfaceTexture.getTimestamp();
        }

        GLES30.glClearColor(0,0, 0, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        if (mInputFilter == null || mImageFilter == null) {
            return;
        }
        mInputFilter.setTextureTransformMatrix(mMatrix);

        // 将OES纹理绘制到FBO中
        int currentTexture = mInputTexture;
        currentTexture = mInputFilter.drawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer);
        // 绘制同框
        currentTexture = drawDuetTexture(currentTexture);
        // 将最终的结果会是预览
        mImageFilter.drawFrame(currentTexture, mDisplayVertexBuffer, mDisplayTextureBuffer);
        // 录制视频
        if (mWeakPresenter.get() != null) {
            mWeakPresenter.get().onRecordFrameAvailable(currentTexture, timeStamp);
        }
    }

    private void resetInputCoordinateSize() {
        float[] vertexCoord = TextureRotationUtils.CubeVertices;
        float[] textureVertices = TextureRotationUtils.TextureVertices;
        mDuetVertexBuffer.clear();
        mDuetVertexBuffer.put(vertexCoord).position(0);
        mDuetTextureBuffer.clear();
        mDuetTextureBuffer.put(textureVertices).position(0);
    }

    /**
     * 将同框视频绘制到FBO中
     * @return  纹理
     */
    private int drawVideoToFrameBuffer() {
        if (mVideoSurfaceTexture != null) {
            mVideoSurfaceTexture.updateTexImage();
            mVideoSurfaceTexture.getTransformMatrix(mMatrix);
        }
        int videoTexture = mVideoInputTexture;
        if (mVideoInputFilter != null) {
            mVideoInputFilter.setTextureTransformMatrix(mMatrix);
            videoTexture = mVideoInputFilter.drawFrameBuffer(mVideoInputTexture, mDuetVertexBuffer, mDuetTextureBuffer);
        }
        return videoTexture;
    }

    /**
     * 绘制同框纹理
     * @param currentTexture
     * @return
     */
    private int drawDuetTexture(int currentTexture) {
        resetInputCoordinateSize();
        int videoTexture = drawVideoToFrameBuffer();
        if (mDuetFilter != null) {
            mDuetFilter.bindFrameBuffer();
            GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
            switch (mDuetType) {
                // 绘制左右同框
                case DUET_TYPE_LEFT_RIGHT: {
                    drawPreviewLeftRight(currentTexture, mFlip);
                    drawVideoLeftRight(videoTexture, !mFlip);
                    break;
                }

                // 绘制上下同框
                case DUET_TYPE_UP_DOWN: {
                    drawPreviewUpDown(currentTexture, mFlip);
                    drawVideoUpDown(videoTexture, !mFlip);
                    break;
                }

                // 绘制大小同框
                case DUET_TYPE_BIG_SMALL: {
                    if (!mFlip) {
                        drawPreviewBigSmall(currentTexture, false);
                        drawVideoBigSmall(videoTexture, true);
                    } else {
                        drawVideoBigSmall(videoTexture, false);
                        drawPreviewBigSmall(currentTexture, true);
                    }
                    break;
                }

                // 默认
                default: {
                    Matrix.setIdentityM(mMVPMatrix, 0);
                    mDuetFilter.setMVPMatrix(mMVPMatrix);
                    mDuetFilter.onDrawTexture(currentTexture, mVertexBuffer, mTextureBuffer);
                    break;
                }
            }
            currentTexture = mDuetFilter.unBindFrameBuffer();
        }
        return currentTexture;
    }

    /**
     * 左右同框绘制预览纹理
     * @param currentTexture 预览纹理
     * @param drawRight     是否绘制到右边
     */
    private void drawPreviewLeftRight(int currentTexture, boolean drawRight) {
        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.scaleM(mMVPMatrix, 0, 0.5f, 0.5f, 0f);
        Matrix.translateM(mMVPMatrix, 0, drawRight ? 1f : -1f, 0f, 0f);
        mDuetFilter.setDuetType(0);
        mDuetFilter.setOffsetX(0);
        mDuetFilter.setOffsetY(0);
        mDuetFilter.setMVPMatrix(mMVPMatrix);
        mDuetFilter.onDrawTexture(currentTexture, mDuetVertexBuffer, mDuetTextureBuffer);
    }

    /**
     * 左右同框绘制视频纹理
     * @param videoTexture
     * @param drawRight
     */
    private void drawVideoLeftRight(int videoTexture, boolean drawRight) {
        Matrix.setIdentityM(mMVPMatrix, 0);
        final double videoRatio = mVideoWidth * 1.0f / mVideoHeight;
        if (videoRatio <= 9f/16f) {
            // todo 长屏视频需要裁剪处理
            final float scale_x = mTextureWidth * 0.5f / mVideoWidth;
            final float scale_y = mTextureHeight * 0.5f / mVideoHeight;
            final float scale = Math.max(scale_x, scale_y);
            Log.d(TAG, "drawVideoLeftRight: scale x: " + scale_x + ", scale y: " + scale_y);
            Matrix.scaleM(mMVPMatrix, 0, scale, scale, 0f);
        } else {
            // 宽屏视频自适应
            final double scale_x = mTextureWidth * 0.5f / mVideoWidth;
            final double scale_y = mTextureHeight * 0.5f / mVideoHeight;
            final double scale = Math.min(scale_x, scale_y);
            final double width = scale * mVideoWidth;
            final double height = scale * mVideoHeight;
            Matrix.scaleM(mMVPMatrix, 0, (float) (width / mTextureWidth), (float) (height / mTextureHeight), 0f);
        }
        Matrix.translateM(mMVPMatrix, 0, drawRight ? 1f : -1f, 0f, 0f);
        mDuetFilter.setDuetType(0);
        mDuetFilter.setOffsetX(0);
        mDuetFilter.setOffsetY(0);
        mDuetFilter.setMVPMatrix(mMVPMatrix);
        mDuetFilter.onDrawTexture(videoTexture, mDuetVertexBuffer, mDuetTextureBuffer);
    }

    /**
     * 上下同框绘制预览纹理
     * @param currentTexture    预览纹理
     * @param drawUp            是否绘制到上方
     */
    private void drawPreviewUpDown(int currentTexture, boolean drawUp) {
        Matrix.setIdentityM(mMVPMatrix, 0);
        mDuetFilter.setDuetType(1f);
        mDuetFilter.setOffsetX(0);
        mDuetFilter.setOffsetY(drawUp ? -0.25f : 0.25f);
        mDuetFilter.setMVPMatrix(mMVPMatrix);
        mDuetFilter.onDrawTexture(currentTexture, mDuetVertexBuffer, mDuetTextureBuffer);
    }

    /**
     * 上下同框绘制视频纹理
     * @param videoTexture  视频纹理
     * @param drawUp        绘制到上方
     */
    private void drawVideoUpDown(int videoTexture, boolean drawUp) {
        Matrix.setIdentityM(mMVPMatrix, 0);

        // 保持视频比例绘制视频
        final double videoRatio = mVideoWidth * 1.0f / mVideoHeight;
        if (videoRatio <= 9f/16f) {
            final double scale_x = mTextureWidth * 1.0 / mVideoWidth;
            final double scale_y = mTextureHeight * 1.0 / mVideoHeight;
            final double scale = Math.max(scale_x, scale_y);
            final double width = scale * mVideoWidth;
            final double height = scale * mVideoHeight;
            final float maxOffsetY = (float)Math.abs(height - mTextureHeight * 0.5) / mTextureHeight;
            Log.d(TAG, "drawVideoUpDown: height: " + height + ", texture height: " + mTextureHeight
                    + ", width: " + width + ", texture width: " + mTextureWidth
                    + ", max offset y: " + maxOffsetY);
            float offset = 0.25f;
            if (offset >= maxOffsetY) {
                offset = maxOffsetY;
            } else if (offset <= -maxOffsetY) {
                offset = -maxOffsetY;
            }
            Matrix.scaleM(mMVPMatrix, 0, (float) (width / mTextureWidth), (float) (height / mTextureHeight), 0f);
            Matrix.translateM(mMVPMatrix, 0, 0f, drawUp ? 1f : -1f, 0f);
            mDuetFilter.setDuetType(1f);
            mDuetFilter.setOffsetX(0);
            mDuetFilter.setOffsetY(drawUp ? 0 + offset : -maxOffsetY + offset);
        } else {

            // 由于上下只需要半屏，因此计算缩放时，需要乘0.5倍
            final double scale_x = mTextureWidth * 0.5f / mVideoWidth;
            final double scale_y = mTextureHeight * 0.5f / mVideoHeight;
            final double scale = Math.max(scale_x, scale_y);
            final double width = scale * mVideoWidth;
            final double height = scale * mVideoHeight;

            // 计算x轴偏移量，可以左右移动
            final float maxOffsetX = (float) Math.abs(width - mTextureWidth) / mTextureWidth;
            float offset = 0f;
            if (offset > maxOffsetX) {
                offset = maxOffsetX;
            } else if (offset <= -maxOffsetX) {
                offset = -maxOffsetX;
            }

            // 先处理x轴偏移，缩放之后，再做y轴平移，否则最大偏移量可能不一致
            Matrix.translateM(mMVPMatrix, 0, offset, 0, 0f);
            Matrix.scaleM(mMVPMatrix, 0, (float) (width / mTextureWidth), (float) (height / mTextureHeight), 0f);
            Matrix.translateM(mMVPMatrix, 0, 0, drawUp ? 1f : -1f, 0f);

            mDuetFilter.setDuetType(1f);
            mDuetFilter.setOffsetX(0);
            mDuetFilter.setOffsetY(0);
        }
        mDuetFilter.setMVPMatrix(mMVPMatrix);
        mDuetFilter.onDrawTexture(videoTexture, mDuetVertexBuffer, mDuetTextureBuffer);
    }

    /**
     * 画中画模式绘制预览纹理
     * @param currentTexture    预览纹理
     * @param drawSmall         绘制小纹理
     */
    private void drawPreviewBigSmall(int currentTexture, boolean drawSmall) {
        // 绘制左区域
        Matrix.setIdentityM(mMVPMatrix, 0);
        if (drawSmall) {
            float scaleX = 1f / 3f;
            float scaleY = 1f / 3f;
            float left = 0;
            float top = mVideoHeight * 2f/3f;
            float ratioX = (left - 0.5f * (1 - scaleX) * mTextureWidth) / (mTextureWidth * scaleX);
            float ratioY = (top - 0.5f * (1 - scaleY) * mTextureHeight) / (mTextureHeight * scaleY);
            Matrix.scaleM(mMVPMatrix, 0, 0.3f, 0.3f, 0f);
            Matrix.translateM(mMVPMatrix, 0, ratioX * 2f, ratioY * 2f, 0f);
        }
        mDuetFilter.setDuetType(0);
        mDuetFilter.setOffsetX(0);
        mDuetFilter.setOffsetY(0);
        mDuetFilter.setMVPMatrix(mMVPMatrix);
        mDuetFilter.onDrawTexture(currentTexture, mDuetVertexBuffer, mDuetTextureBuffer);
    }

    /**
     *
     * @param videoTexture
     * @param drawSmall
     */
    private void drawVideoBigSmall(int videoTexture, boolean drawSmall) {
        Matrix.setIdentityM(mMVPMatrix, 0);
        if (drawSmall) {
            float scaleX = 1f / 3f;
            float scaleY = 1f / 3f;
            float left = 0;
            float top = mVideoHeight * 2f/3f;
            float ratioX = (left - 0.5f * (1 - scaleX) * mTextureWidth) / (mTextureWidth * scaleX);
            float ratioY = (top - 0.5f * (1 - scaleY) * mTextureHeight) / (mTextureHeight * scaleY);
            Matrix.scaleM(mMVPMatrix, 0, 0.3f, 0.3f, 0f);
            Matrix.translateM(mMVPMatrix, 0, ratioX * 2f, ratioY * 2f, 0f);
        }
        mDuetFilter.setDuetType(0);
        mDuetFilter.setOffsetX(0);
        mDuetFilter.setOffsetY(0);
        mDuetFilter.setMVPMatrix(mMVPMatrix);
        mDuetFilter.onDrawTexture(videoTexture, mDuetVertexBuffer, mDuetTextureBuffer);
    }


    /**
     * 更新输入纹理
     * @param surfaceTexture
     */
    private void updateSurfaceTexture(@NonNull SurfaceTexture surfaceTexture) {
        // 绑定到当前的输入纹理
        if (mNeedToAttach) {
            if (mInputTexture != OpenGLUtils.GL_NOT_TEXTURE) {
                OpenGLUtils.deleteTexture(mInputTexture);
            }
            mInputTexture = OpenGLUtils.createOESTexture();
            surfaceTexture.attachToGLContext(mInputTexture);
            mNeedToAttach = false;
        }
        surfaceTexture.updateTexImage();
        surfaceTexture.getTransformMatrix(mMatrix);
    }

    /**
     * 绑定纹理
     * @param surfaceTexture
     */
    public void bindSurfaceTexture(SurfaceTexture surfaceTexture) {
        synchronized (this) {
            mWeakSurfaceTexture = new WeakReference<>(surfaceTexture);
            mNeedToAttach = true;
        }
    }

    /**
     * 设置纹理大小
     * @param width
     * @param height
     */
    public void setTextureSize(int width, int height) {
        mTextureWidth = width;
        mTextureHeight = height;
        if (mViewWidth != 0 && mViewHeight != 0) {
            onFilterSizeChanged();
            adjustDisplayCoordinateSize();
        }
    }

    /**
     * 初始化滤镜
     */
    private void initFilters() {
        mInputFilter = new GLImageOESInputFilter(mWeakPresenter.get().getActivity());
        mVideoInputFilter = new GLImageOESInputFilter(mWeakPresenter.get().getActivity());
        mDuetFilter = new GLImageDuetFilter(mWeakPresenter.get().getActivity());
        mImageFilter = new GLImageMirrorFilter(mWeakPresenter.get().getActivity());
    }

    /**
     * 更新滤镜纹理大小和显示大小
     */
    private void onFilterSizeChanged() {
        if (mInputFilter != null) {
            mInputFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            mInputFilter.initFrameBuffer(mTextureWidth, mTextureHeight);
            mInputFilter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        }
        if (mVideoInputFilter != null) {
            mVideoInputFilter.onInputSizeChanged(mVideoWidth, mVideoHeight);
            mVideoInputFilter.initFrameBuffer(mVideoWidth, mVideoHeight);
            mVideoInputFilter.onDisplaySizeChanged(mTextureWidth, mTextureHeight);
        }
        if (mDuetFilter != null) {
            mDuetFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            mDuetFilter.initFrameBuffer(mTextureWidth, mTextureHeight);
            mDuetFilter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        }
        if (mImageFilter != null) {
            mImageFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            mImageFilter.onDisplaySizeChanged(mViewWidth, mViewHeight);
        }
    }

    /**
     * 调整由于surface的大小与SurfaceView大小不一致带来的显示问题
     */
    private void adjustDisplayCoordinateSize() {
        float[] textureCoord;
        float[] vertexCoord = TextureRotationUtils.CubeVertices;
        float[] textureVertices = TextureRotationUtils.TextureVertices;
        float ratioMax = Math.max((float) mViewWidth / mTextureWidth,
                (float) mViewHeight / mTextureHeight);
        // 新的宽高
        float imageWidth = mTextureWidth * ratioMax;
        float imageHeight = mTextureHeight * ratioMax;
        // 获取视图跟texture的宽高比
        float ratioWidth = imageWidth / (float) mViewWidth;
        float ratioHeight = imageHeight / (float) mViewHeight;
        float distHorizontal = (1 - 1 / ratioWidth) / 2;
        float distVertical = (1 - 1 / ratioHeight) / 2;
        textureCoord = new float[] {
                addDistance(textureVertices[0], distHorizontal), addDistance(textureVertices[1], distVertical),
                addDistance(textureVertices[2], distHorizontal), addDistance(textureVertices[3], distVertical),
                addDistance(textureVertices[4], distHorizontal), addDistance(textureVertices[5], distVertical),
                addDistance(textureVertices[6], distHorizontal), addDistance(textureVertices[7], distVertical),
        };
        // 更新VertexBuffer 和 TextureBuffer
        mDisplayVertexBuffer.clear();
        mDisplayVertexBuffer.put(vertexCoord).position(0);
        mDisplayTextureBuffer.clear();
        mDisplayTextureBuffer.put(textureCoord).position(0);
    }

    private float addDistance(float coordinate, float distance) {
        return coordinate == 0.0f ? distance : 1 - distance;
    }

    /**
     * 清理一些缓存数据
     */
    public void clear() {
        if (mWeakSurfaceTexture != null) {
            mWeakSurfaceTexture.clear();
        }
    }

    /**
     * 设置同框类型
     */
    public void setDuetType(DuetType type) {
        mDuetType = type;
        mFlip = false;
    }

    /**
     * 翻转
     */
    public void flip() {
        mFlip = !mFlip;
    }

    /**
     * 初始化播放器
     */
    private void initMediaPlayer() {
        if (mDuetVideo == null) {
            return;
        }
        mVideoInputTexture = OpenGLUtils.createOESTexture();
        mVideoSurfaceTexture = new SurfaceTexture(mVideoInputTexture);
        mVideoSurface = new Surface(mVideoSurfaceTexture);
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(mDuetVideo.getPath());
            mMediaPlayer.setSurface(mVideoSurface);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepare();
            mMediaPlayer.seekTo(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始播放
     */
    public void playVideo() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
    }

    /**
     * 暂停播放
     */
    public void stopVideo() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
        }
    }
}
