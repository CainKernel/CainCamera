package com.cgfay.caincamera.core;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES30;
import android.os.HandlerThread;
import android.util.Log;
import android.view.SurfaceHolder;

import com.cgfay.caincamera.bean.CameraInfo;
import com.cgfay.caincamera.bean.Size;
import com.cgfay.caincamera.facedetector.DetectorCallback;
import com.cgfay.caincamera.facedetector.FaceManager;
import com.cgfay.caincamera.gles.EglCore;
import com.cgfay.caincamera.gles.WindowSurface;
import com.cgfay.caincamera.multimedia.VideoEncoderCore;
import com.cgfay.caincamera.type.FilterGroupType;
import com.cgfay.caincamera.type.FilterType;
import com.cgfay.caincamera.utils.CameraUtils;
import com.cgfay.caincamera.utils.GlUtil;

import java.io.File;
import java.io.IOException;

/**
 * 渲染线程
 * Created by cain on 2017/11/4.
 */

public class RenderThread extends HandlerThread implements SurfaceTexture.OnFrameAvailableListener,
        Camera.PreviewCallback {

    private static final String TAG = "RenderThread";

    // 操作锁
    private final Object mSynOperation = new Object();
    // Looping锁
    private final Object mSyncIsLooping = new Object();

    private boolean isPreviewing = false;   // 是否预览状态
    private boolean isRecording = false;    // 是否录制状态

    // EGL共享上下文
    private EglCore mEglCore;
    // EGLSurface
    private WindowSurface mDisplaySurface;

    // CameraTexture对应的Id
    private int mCameraTextureId;
    private SurfaceTexture mCameraTexture;

    // 矩阵
    private final float[] mMatrix = new float[16];

    // 视图宽高
    private int mViewWidth, mViewHeight;
    // 预览图片大小
    private int mImageWidth, mImageHeight;

    // 是否处于检测阶段
    private boolean isFaceDetecting = false;

    // 更新帧的锁
    private final Object mSyncFrameNum = new Object();
    private final Object mSyncTexture = new Object();
    // 可用帧
    private int mFrameNum = 0;
    // 拍照
    private boolean isTakePicture = false;

    // 关键点绘制（调试用）
    private FacePointsDrawer mFacePointsDrawer;

    // 预览回调缓存，解决previewCallback回调内存抖动问题
    private byte[] mPreviewBuffer;

    private RenderHandler mRenderHandler;

    public RenderThread(String name) {
        super(name);
    }

    public void setRenderHandler(RenderHandler handler) {
        mRenderHandler = handler;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }

    private long time = 0;
    @Override
    public void onPreviewFrame(final byte[] data, Camera camera) {
        addNewFrame();
        if (mRenderHandler != null) {
            synchronized (mSynOperation) {
                if (isPreviewing || isRecording) {
                    mRenderHandler.sendMessage(mRenderHandler
                            .obtainMessage(RenderHandler.MSG_PREVIEW_CALLBACK, data));
                }
            }
        }
        if (mPreviewBuffer != null) {
            camera.addCallbackBuffer(mPreviewBuffer);
        }
        Log.d("onPreviewFrame", "update time = " + (System.currentTimeMillis() - time));
        time = System.currentTimeMillis();
    }

    void surfaceCreated(SurfaceHolder holder) {
        mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
        mDisplaySurface = new WindowSurface(mEglCore, holder.getSurface(), false);
        mDisplaySurface.makeCurrent();
        mCameraTextureId = GlUtil.createTextureOES();
        mCameraTexture = new SurfaceTexture(mCameraTextureId);
        mCameraTexture.setOnFrameAvailableListener(this);
        // 打开相机
        CameraUtils.openCamera(CameraUtils.DESIRED_PREVIEW_FPS);
        // 设置预览Surface
        CameraUtils.setPreviewSurface(mCameraTexture);
        calculateImageSize();

        // 渲染初始化
        RenderManager.getInstance().init();
        RenderManager.getInstance().onInputSizeChanged(mImageWidth, mImageHeight);

        // 禁用深度测试和背面绘制
        GLES30.glDisable(GLES30.GL_DEPTH_TEST);
        GLES30.glDisable(GLES30.GL_CULL_FACE);
        // 添加预览回调以及回调buffer，用于人脸检测
        initPreviewCallback();
        // 初始化人脸检测工具
        initFaceDetection();
    }

    void surfaceChanged(int width, int height) {
        mViewWidth = width;
        mViewHeight = height;
        onFilterChanged();
        RenderManager.getInstance().adjustViewSize();
        RenderManager.getInstance().updateTextureBuffer();
        RenderManager.getInstance().onDisplaySizeChanged(mViewWidth, mViewHeight);
        // 开始预览
        CameraUtils.startPreview();
        // 渲染视图变化
        RenderManager.getInstance().onDisplaySizeChanged(mViewWidth, mViewHeight);
        isPreviewing = true;
    }

    void surfaceDestoryed() {
        isPreviewing = false;
        CameraUtils.releaseCamera();
        FaceManager.getInstance().destory();
        if (mCameraTexture != null) {
            mCameraTexture.release();
            mCameraTexture = null;
        }
        if (mDisplaySurface != null) {
            mDisplaySurface.release();
            mDisplaySurface = null;
        }
        if (mEglCore != null) {
            mEglCore.release();
            mEglCore = null;
        }
        if (mFacePointsDrawer != null) {
            mFacePointsDrawer.release();
            mFacePointsDrawer = null;
        }
        RenderManager.getInstance().release();
    }

    /**
     * 初始化预览回调
     * 备注：在某些设备上，需要在setPreviewTexture之后，startPreview之前添加回调才能使得onPreviewFrame回调正常
     */
    private void initPreviewCallback() {
        Size previewSize = CameraUtils.getPreviewSize();
        int size = previewSize.getWidth() * previewSize.getHeight() * 3 / 2;
        mPreviewBuffer = new byte[size];
        CameraUtils.setPreviewCallbackWithBuffer(this, mPreviewBuffer);
    }

    /**
     * 初始化人脸检测工具
     */
    private void initFaceDetection() {
        FaceManager.getInstance().createHandleThread();
        if (ParamsManager.canFaceTrack) {
            FaceManager.getInstance().initFaceConfig(mImageWidth, mImageHeight);
            FaceManager.getInstance().getFaceDetector().setBackCamera(CameraUtils.getCameraID()
                    == Camera.CameraInfo.CAMERA_FACING_BACK);
            addDetectorCallback();
            if (ParamsManager.enableDrawingPoints) {
                mFacePointsDrawer = new FacePointsDrawer();
            }
        }
    }

    /**
     * 预览回调
     * @param data
     */
    void onPreviewCallback(byte[] data) {
        if (isFaceDetecting)
            return;
        isFaceDetecting = true;
        if (ParamsManager.canFaceTrack) {
            synchronized (mSyncIsLooping) {
                FaceManager.getInstance().faceDetecting(data, mImageHeight, mImageWidth);
            }
        }
        isFaceDetecting = false;
    }


    /**
     * 开始预览
     */
    void startPreview() {
        isPreviewing = true;
        if (mCameraTexture != null) {
            RenderManager.getInstance().updateTextureBuffer();
            CameraUtils.setPreviewSurface(mCameraTexture);
            initPreviewCallback();
            CameraUtils.startPreview();
        }

    }

    /**
     * 停止预览
     */
    void stopPreview() {
        isPreviewing = false;
        CameraUtils.stopPreview();
    }

    /**
     * 计算imageView 的宽高
     */
    private void calculateImageSize() {
        Size size = CameraUtils.getPreviewSize();
        CameraInfo info = CameraUtils.getCameraInfo();
        if (info != null) {
            if (info.getOrientation() == 90 || info.getOrientation() == 270) {
                mImageWidth = size.getHeight();
                mImageHeight = size.getWidth();
            } else {
                mImageWidth = size.getWidth();
                mImageHeight = size.getHeight();
            }
        }
    }


    /**
     * 设置对焦区域
     * @param rect
     */
    void setFocusAres(Rect rect) {
        CameraUtils.setFocusArea(rect, null);
    }

    /**
     * 滤镜或视图发生变化时调用
     */
    private void onFilterChanged() {
        RenderManager.getInstance().onFilterChanged();
    }

    /**
     * 更新filter
     * @param type Filter类型
     */
    void changeFilter(FilterType type) {
        RenderManager.getInstance().changeFilter(type);
    }

    /**
     * 切换滤镜组
     * @param type
     */
    void changeFilterGroup(FilterGroupType type) {
        synchronized (mSyncIsLooping) {
            RenderManager.getInstance().changeFilterGroup(type);
        }
    }

    /**
     * 绘制帧
     */
    void drawFrame() {
        // 如果存在新的帧，则更新帧
        synchronized (mSyncFrameNum) {
            synchronized (mSyncTexture) {
                if (mCameraTexture != null) {
                    while (mFrameNum != 0) {
                        mCameraTexture.updateTexImage();
                        --mFrameNum;
                    }
                } else {
                    return;
                }
            }
        }
        // 切换渲染上下文
        mDisplaySurface.makeCurrent();
        mCameraTexture.getTransformMatrix(mMatrix);
        RenderManager.getInstance().setTextureTransformMatirx(mMatrix);
        // 绘制
        draw();
        // 拍照状态
        if (isTakePicture) {
            isTakePicture = false;
            File file = new File(ParamsManager.ImagePath + "CainCamera_"
                    + System.currentTimeMillis() + ".jpeg");
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try {
                mDisplaySurface.saveFrame(file);
            } catch (IOException e) {
                Log.w(TAG, "saceFrame error: " + e.toString());
            }
        }
        mDisplaySurface.swapBuffers();

        // 是否处于录制状态
        if (isRecording) {
            RecorderManager.getInstance().drawRecorderFrame(mCameraTexture.getTimestamp());
        }
    }

    /**
     * 添加检测回调
     */
    private void addDetectorCallback() {
        FaceManager.getInstance().getFaceDetector()
                .addDetectorCallback(new DetectorCallback() {
                    @Override
                    public void onTrackingFinish(boolean hasFaces) {
                        // 如果有人脸并且允许绘制关键点，则添加数据
                        if (hasFaces) {
                            if (ParamsManager.enableDrawingPoints && mFacePointsDrawer != null) {
                                mFacePointsDrawer.addPoints(FaceManager.getInstance()
                                        .getFaceDetector().getFacePoints());
                            }
                        } else {
                            if (ParamsManager.enableDrawingPoints && mFacePointsDrawer != null) {
                                mFacePointsDrawer.addPoints(null);
                            }
                        }
                        // 强制刷新
                        addNewFrame();
                    }
                });
    }

    /**
     * 绘制图像数据到FBO
     */
    private void draw() {
        // 绘制
        RenderManager.getInstance().drawFrame(mCameraTextureId);

        // 是否绘制点
        if (ParamsManager.enableDrawingPoints && mFacePointsDrawer != null) {
            mFacePointsDrawer.drawPoints();
        }
    }


    /**
     * 拍照
     */
    void takePicture() {
        isTakePicture = true;
    }

    /**
     * 开始录制
     */
    void startRecording() {
        RecorderManager.getInstance().startRecording(mEglCore, mViewWidth, mViewHeight);
        isRecording = true;
    }

    /**
     * 停止录制
     */
    void stopRecording() {
        RecorderManager.getInstance().stopRecording();
        isRecording = false;
    }

    /**
     * 切换相机
     */
    void switchCamera() {
        CameraUtils.switchCamera(1 - CameraUtils.getCameraID(), mCameraTexture,
                this, mPreviewBuffer);
    }

    /**
     * 添加新的一帧
     */
    private void addNewFrame() {
        synchronized (mSyncFrameNum) {
            if (isPreviewing) {
                ++mFrameNum;
                if (mRenderHandler != null) {
                    mRenderHandler.removeMessages(RenderHandler.MSG_FRAME);
                    mRenderHandler.sendMessageAtFrontOfQueue(mRenderHandler
                            .obtainMessage(RenderHandler.MSG_FRAME));
                }
            }
        }
    }
}
