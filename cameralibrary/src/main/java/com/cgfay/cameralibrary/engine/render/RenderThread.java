package com.cgfay.cameralibrary.engine.render;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES30;
import android.os.HandlerThread;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.badlogic.gdx.math.Vector3;
import com.cgfay.cameralibrary.engine.camera.CameraEngine;
import com.cgfay.cameralibrary.engine.camera.CameraParam;
import com.cgfay.cameralibrary.engine.recorder.HardcodeEncoder;
import com.cgfay.filterlibrary.gles.EglCore;
import com.cgfay.filterlibrary.gles.WindowSurface;
import com.cgfay.filterlibrary.glfilter.color.bean.DynamicColor;
import com.cgfay.filterlibrary.glfilter.makeup.bean.DynamicMakeup;
import com.cgfay.filterlibrary.glfilter.stickers.StaticStickerNormalFilter;
import com.cgfay.filterlibrary.glfilter.stickers.bean.DynamicSticker;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;

import java.nio.ByteBuffer;

/**
 * 渲染线程
 * Created by cain on 2017/11/4.
 */

class RenderThread extends HandlerThread implements SurfaceTexture.OnFrameAvailableListener,
        Camera.PreviewCallback {

    private static final String TAG = "RenderThread";
    private static final boolean VERBOSE = false;

    // 操作锁
    private final Object mSynOperation = new Object();
    // 更新帧的锁
    private final Object mSyncFrameNum = new Object();
    private final Object mSyncFence = new Object();

    private boolean isPreviewing = false;       // 是否预览状态
    private boolean isRecording = false;        // 是否录制状态
    private boolean isRecordingPause = false;   // 是否处于暂停录制状态

    // EGL共享上下文
    private EglCore mEglCore;
    // 预览用的EGLSurface
    private WindowSurface mDisplaySurface;

    private int mInputTexture;
    private int mCurrentTexture;
    private SurfaceTexture mSurfaceTexture;

    // 矩阵
    private final float[] mMatrix = new float[16];

    // 预览回调
    private byte[] mPreviewBuffer;
    // 输入图像大小
    private int mTextureWidth, mTextureHeight;

    // 可用帧
    private int mFrameNum = 0;

    // 渲染Handler回调
    private RenderHandler mRenderHandler;

    // 计算帧率
    private FrameRateMeter mFrameRateMeter;

    // 上下文
    private Context mContext;

    // 预览参数
    private CameraParam mCameraParam;

    // 渲染管理器
    private RenderManager mRenderManager;

    public RenderThread(Context context, String name) {
        super(name);
        mContext = context;
        mCameraParam = CameraParam.getInstance();
        mRenderManager = RenderManager.getInstance();
        mFrameRateMeter = new FrameRateMeter();
    }

    /**
     * 设置预览Handler回调
     * @param handler
     */
    public void setRenderHandler(RenderHandler handler) {
        mRenderHandler = handler;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }

    private long time = 0;
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        synchronized (mSynOperation) {
            if (isPreviewing || isRecording) {
                mRenderHandler.sendMessage(mRenderHandler
                        .obtainMessage(RenderHandler.MSG_PREVIEW_CALLBACK, data));
            }
        }
        if (mPreviewBuffer != null) {
            camera.addCallbackBuffer(mPreviewBuffer);
        }
        // 计算fps
        if (mRenderHandler != null && mCameraParam.showFps) {
            mRenderHandler.sendEmptyMessage(RenderHandler.MSG_CALCULATE_FPS);
        }
        if (VERBOSE) {
            Log.d("onPreviewFrame", "update time = " + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();
        }
    }

    /**
     * 预览回调
     * @param data
     */
    void onPreviewCallback(byte[] data) {
        if (mCameraParam.cameraCallback != null) {
            mCameraParam.cameraCallback.onPreviewCallback(data);
        }
    }

    /**
     * Surface创建
     * @param holder
     */
    void surfaceCreated(SurfaceHolder holder) {
        mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
        mDisplaySurface = new WindowSurface(mEglCore, holder.getSurface(), false);
        mDisplaySurface.makeCurrent();

        GLES30.glDisable(GLES30.GL_DEPTH_TEST);
        GLES30.glDisable(GLES30.GL_CULL_FACE);

        // 渲染器初始化
        mRenderManager.init(mContext);

        mInputTexture = OpenGLUtils.createOESTexture();
        mSurfaceTexture = new SurfaceTexture(mInputTexture);
        mSurfaceTexture.setOnFrameAvailableListener(this);

        // 打开相机
        openCamera();

    }

    /**
     * Surface改变
     * @param width
     * @param height
     */
    void surfaceChanged(int width, int height) {
        mRenderManager.setDisplaySize(width, height);
        startPreview();
    }

    /**
     * Surface销毁
     */
    void surfaceDestroyed() {
        mRenderManager.release();
        releaseCamera();
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        if (mDisplaySurface != null) {
            mDisplaySurface.release();
            mDisplaySurface = null;
        }
        if (mEglCore != null) {
            mEglCore.release();
            mEglCore = null;
        }
    }

    /**
     * 绘制帧
     */
    void drawFrame() {
        if (mSurfaceTexture == null || mDisplaySurface == null) {
            return;
        }
        // 当记录的请求帧数不为时，更新画面
        while (mFrameNum > 0) {
            mSurfaceTexture.updateTexImage();
            --mFrameNum;

            // 切换渲染上下文
            mDisplaySurface.makeCurrent();
            mSurfaceTexture.getTransformMatrix(mMatrix);

            // 绘制渲染
            mCurrentTexture = mRenderManager.drawFrame(mInputTexture, mMatrix);

            // 是否绘制人脸关键点
            mRenderManager.drawFacePoint(mCurrentTexture);

            // 执行拍照
            if (mCameraParam.isTakePicture) {
                synchronized (mSyncFence) {
                    ByteBuffer buffer = mDisplaySurface.getCurrentFrame();
                    mCameraParam.captureCallback.onCapture(buffer,
                            mDisplaySurface.getWidth(), mDisplaySurface.getHeight());
                    mCameraParam.isTakePicture = false;
                }
            }

            // 显示到屏幕
            mDisplaySurface.swapBuffers();

            // 是否处于录制状态
            if (isRecording && !isRecordingPause) {
                HardcodeEncoder.getInstance().frameAvailable();
                HardcodeEncoder.getInstance()
                        .drawRecorderFrame(mCurrentTexture, mSurfaceTexture.getTimestamp());
            }
        }
    }

    /**
     * 拍照
     */
    void takePicture() {
        synchronized (mSyncFence) {
            ByteBuffer buffer = mDisplaySurface.getCurrentFrame();
            mCameraParam.captureCallback.onCapture(buffer,
                    mDisplaySurface.getWidth(), mDisplaySurface.getHeight());
            mCameraParam.isTakePicture = false;
        }
    }

    /**
     * 计算fps
     */
    void calculateFps() {
        // 帧率回调
        if ((mCameraParam).fpsCallback != null) {
            mFrameRateMeter.drawFrameCount();
            (mCameraParam).fpsCallback.onFpsCallback(mFrameRateMeter.getFPS());
        }
    }

    /**
     * 计算imageView 的宽高
     */
    private void calculateImageSize() {
        if (mCameraParam.orientation == 90 || mCameraParam.orientation == 270) {
            mTextureWidth = mCameraParam.previewHeight;
            mTextureHeight = mCameraParam.previewWidth;
        } else {
            mTextureWidth = mCameraParam.previewWidth;
            mTextureHeight = mCameraParam.previewHeight;
        }
        mRenderManager.setTextureSize(mTextureWidth, mTextureHeight);
    }

    /**
     * 切换边框模糊
     * @param enableEdgeBlur
     */
    void changeEdgeBlurFilter(boolean enableEdgeBlur) {
        synchronized (mSynOperation) {
            mRenderManager.changeEdgeBlurFilter(enableEdgeBlur);
        }
    }

    /**
     * 切换动态滤镜
     * @param color
     */
    void changeDynamicFilter(DynamicColor color) {
        synchronized (mSynOperation) {
            mRenderManager.changeDynamicFilter(color);
        }
    }

    /**
     * 切换动态彩妆
     * @param makeup
     */
    void changeDynamicMakeup(DynamicMakeup makeup) {
        synchronized (mSynOperation) {
            mRenderManager.changeDynamicMakeup(makeup);
        }
    }

    /**
     * 切换动态资源
     * @param color
     */
    void changeDynamicResource(DynamicColor color) {
        synchronized (mSynOperation) {
            mRenderManager.changeDynamicResource(color);
        }
    }

    /**
     * 切换动态资源
     * @param sticker
     */
    void changeDynamicResource(DynamicSticker sticker) {
        synchronized (mSynOperation) {
            mRenderManager.changeDynamicResource(sticker);
        }
    }

    /**
     * 开始录制
     */
    void startRecording() {
        if (mEglCore != null) {
            // 设置渲染Texture 的宽高
            HardcodeEncoder.getInstance().setTextureSize(mTextureWidth, mTextureHeight);
            // 这里将EGLContext传递到录制线程共享。
            // 由于EGLContext是当前线程手动创建，也就是OpenGLES的main thread
            // 这里需要传自己手动创建的EglContext
            HardcodeEncoder.getInstance().startRecording(mContext, mEglCore.getEGLContext());
        }
        isRecording = true;
    }

    /**
     * 停止录制
     */
    void stopRecording() {
        HardcodeEncoder.getInstance().stopRecording();
        isRecording = false;
    }

    /**
     * 请求刷新
     */
    public void requestRender() {
        synchronized (mSyncFrameNum) {
            if (isPreviewing) {
                ++mFrameNum;
                if (mRenderHandler != null) {
                    mRenderHandler.removeMessages(RenderHandler.MSG_RENDER);
                    mRenderHandler.sendMessage(mRenderHandler
                            .obtainMessage(RenderHandler.MSG_RENDER));
                }
            }
        }
    }


    // --------------------------------- 相机操作逻辑 ----------------------------------------------
    /**
     * 打开相机
     */
    void openCamera() {
        releaseCamera();
        CameraEngine.getInstance().openCamera(mContext);
        CameraEngine.getInstance().setPreviewSurface(mSurfaceTexture);
        calculateImageSize();
        mPreviewBuffer = new byte[mTextureWidth * mTextureHeight * 3/ 2];
        CameraEngine.getInstance().setPreviewCallbackWithBuffer(this, mPreviewBuffer);
        // 相机打开回调
        if (mCameraParam.cameraCallback != null) {
            mCameraParam.cameraCallback.onCameraOpened();
        }
    }

    /**
     * 切换相机
     */
    void switchCamera() {
        mCameraParam.backCamera = !mCameraParam.backCamera;
        if (mCameraParam.backCamera) {
            mCameraParam.cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else {
            mCameraParam.cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        openCamera();
        startPreview();
    }

    /**
     * 开始预览
     */
    private void startPreview() {
        CameraEngine.getInstance().startPreview();
        isPreviewing = true;
    }

    /**
     * 释放相机
     */
    private void releaseCamera() {
        isPreviewing = false;
        CameraEngine.getInstance().releaseCamera();
    }


    public StaticStickerNormalFilter touchDown(MotionEvent e) {
        synchronized (mSyncFrameNum) {
            if (mRenderManager != null) {
               return mRenderManager.touchDown(e);
            }
        }
        return null;
    }
}
