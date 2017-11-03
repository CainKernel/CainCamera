package com.cgfay.caincamera.core;

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
 * 使用枚举方式实现单例
 * 在surfaceCreated中创建线程实例，在surfaceDestroyed中销毁线程，
 * 主要是为了防止线程未释放，会导致退出后的内存呈现锯齿状，内存泄漏。
 * Created by cain on 2017/7/9.
 */

public class DrawerManager implements SurfaceTexture.OnFrameAvailableListener,
        Camera.PreviewCallback {

    private static final String TAG = "DrawerManager";

    private static DrawerManager mInstance;

    private RenderHandler mRenderHandler;
    private HandlerThread mHandlerThread;

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

    // 录制视频用的EGLSurface
    private WindowSurface mRecordWindowSurface;
    private VideoEncoderCore mVideoEncoder;

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

    // 是否允许绘制人脸关键点
    private boolean enableDrawPoints = false;
    private int mRecordBitrate;
    private static final int FRAME_RATE = 25;
    private static final int BPP = 4;

    // 关键点绘制（调试用）
    private FacePointsDrawer mFacePointsDrawer;

    // 预览回调缓存，解决previewCallback回调内存抖动问题
    private byte[] mPreviewBuffer;

    public static DrawerManager getInstance() {
        if (mInstance == null) {
            mInstance = new DrawerManager();
        }
        return mInstance;
    }

    private DrawerManager() {

    }

    public void surfaceCreated(SurfaceHolder holder) {
        create();
        if (mRenderHandler != null) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(RenderHandler.MSG_SURFACE_CREATED, holder));
        }
    }

    public void surfacrChanged(int width, int height) {
        if (mRenderHandler != null) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(RenderHandler.MSG_SURFACE_CHANGED, width, height));
        }
        startPreview();
    }

    public void surfaceDestroyed() {
        stopPreview();
        if (mRenderHandler != null) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(RenderHandler.MSG_SURFACE_DESTROYED));
        }
        destory();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
    }

    /**
     * 创建HandlerThread和Handler
     */
    private void create() {
        mHandlerThread = new HandlerThread("CameraRenderThread");
        mHandlerThread.start();
        mRenderHandler = new RenderHandler(mHandlerThread.getLooper(), this);
        mRenderHandler.sendEmptyMessage(RenderHandler.MSG_INIT);
    }


    /**
     * 开始预览
     */
    public void startPreview() {
        if (mRenderHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(RenderHandler.MSG_START_PREVIEW));
            synchronized (mSyncIsLooping) {
                isPreviewing = true;
            }
        }
    }

    /**
     * 停止预览
     */
    public void stopPreview() {
        if (mRenderHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(RenderHandler.MSG_STOP_PREVIEW));
            synchronized (mSyncIsLooping) {
                isPreviewing = false;
            }
        }
    }

    /**
     * 改变Filter类型
     */
    public void changeFilterType(FilterType type) {
        if (mRenderHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(RenderHandler.MSG_FILTER_TYPE, type));
        }
    }

    /**
     * 改变滤镜组类型
     * @param type
     */
    public void changeFilterGroup(FilterGroupType type) {
        if (mRenderHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(RenderHandler.MSG_FILTER_GROUP, type));
        }
    }

    /**
     * 切换相机
     */
    public void switchCamera() {
        if (mRenderHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(RenderHandler.MSG_SWITCH_CAMERA));

        }
        // 开始预览
        startPreview();
    }

    /**
     * 开始录制
     */
    public void startRecording() {
        if (mRenderHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(RenderHandler.MSG_START_RECORDING));
        }
    }

    /**
     * 停止录制
     */
    public void stopRecording() {
        if (mRenderHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            mRenderHandler.sendEmptyMessage(RenderHandler.MSG_STOP_RECORDING);
            synchronized (mSyncIsLooping) {
                isRecording = false;
            }
        }
    }

    /**
     * 拍照
     */
    public void takePicture() {
        if (mRenderHandler == null) {
            return;
        }
        synchronized (mSynOperation) {
            // 发送拍照命令
            mRenderHandler.sendMessage(mRenderHandler
                    .obtainMessage(RenderHandler.MSG_TAKE_PICTURE));
        }
    }

    /**
     * 销毁当前持有的Looper 和 Handler
     */
    public void destory() {
        // Handler不存在时，需要销毁当前线程，否则可能会出现重新打开不了的情况
        if (mRenderHandler == null) {
            if (mHandlerThread != null) {
                mHandlerThread.quitSafely();
                try {
                    mHandlerThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mHandlerThread = null;
            }
            return;
        }
        mRenderHandler.sendEmptyMessage(RenderHandler.MSG_DESTROY);
        mHandlerThread.quitSafely();
        try {
            mHandlerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mHandlerThread = null;
        mRenderHandler = null;
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

    // ------------------------------- 内部方法 ------------------------------------------
    void onSurfaceCreated(SurfaceHolder holder) {
        mEglCore = new EglCore(null, EglCore.FLAG_RECORDABLE);
        mDisplaySurface = new WindowSurface(mEglCore, holder.getSurface(), false);
        mDisplaySurface.makeCurrent();
        mCameraTextureId = GlUtil.createTextureOES();
        mCameraTexture = new SurfaceTexture(mCameraTextureId);
        mCameraTexture.setOnFrameAvailableListener(DrawerManager.this);
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

    void onSurfaceChanged(int width, int height) {
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

    void onSurfaceDestoryed() {
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
            if (enableDrawPoints) {
                mFacePointsDrawer = new FacePointsDrawer();
            }
        }
    }

    /**
     * 预览回调
     * @param data
     */
    void internalPreviewCallback(byte[] data) {
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
    void internalStartPreview() {
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
    void internalStopPreview() {
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
     * 滤镜或视图发生变化时调用
     */
    private void onFilterChanged() {
        RenderManager.getInstance().onFilterChanged();
    }

    /**
     * 更新filter
     * @param type Filter类型
     */
    void internalChangeFilter(FilterType type) {
        RenderManager.getInstance().changeFilter(type);
    }

    /**
     * 切换滤镜组
     * @param type
     */
    void internalChangeFilterGroup(FilterGroupType type) {
        synchronized (mSyncIsLooping) {
            RenderManager.getInstance().changeFilterGroup(type);
        }
    }

    /**
     * 绘制帧
     */
    void internalDrawFrame() {
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
            mRecordWindowSurface.makeCurrent();
            mVideoEncoder.drainEncoder(false);
            draw();
            mRecordWindowSurface.setPresentationTime(mCameraTexture.getTimestamp());
            mRecordWindowSurface.swapBuffers();
        }
    }

    /**
     * 添加检测回调
     */
    public void addDetectorCallback() {
        FaceManager.getInstance().getFaceDetector()
                .addDetectorCallback(new DetectorCallback() {
                    @Override
                    public void onTrackingFinish(boolean hasFaces) {
                        // 如果有人脸并且允许绘制关键点，则添加数据
                        if (hasFaces) {
                            if (enableDrawPoints && mFacePointsDrawer != null) {
                                mFacePointsDrawer.addPoints(FaceManager.getInstance()
                                        .getFaceDetector().getFacePoints());
                            }
                        } else {
                            if (enableDrawPoints && mFacePointsDrawer != null) {
                                mFacePointsDrawer.addPoints(null);
                            }
                        }
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
        if (enableDrawPoints && mFacePointsDrawer != null) {
            mFacePointsDrawer.drawPoints();
        }
    }


    /**
     * 拍照
     */
    void internalTakePicture() {
        isTakePicture = true;
    }

    /**
     * 开始录制
     */
    void internalStartRecording() {
        File file = new File(ParamsManager.VideoPath
                + "CainCamera_" + System.currentTimeMillis() + ".mp4");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        // 计算帧率
        mRecordBitrate = mViewWidth * mViewHeight * FRAME_RATE / BPP;
        try {
            mVideoEncoder = new VideoEncoderCore(mViewWidth, mViewHeight,
                    mRecordBitrate, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRecordWindowSurface = new WindowSurface(mEglCore,
                mVideoEncoder.getInputSurface(), true);
        isRecording = true;
    }

    /**
     * 停止录制
     */
    void internalStopRecording() {
        synchronized (mSyncIsLooping) {
            mVideoEncoder.drainEncoder(true);
        }
        isRecording = false;
        // 录制完成需要释放资源
        if (mVideoEncoder != null) {
            mVideoEncoder.release();
            mVideoEncoder = null;
        }
        if (mRecordWindowSurface != null) {
            mRecordWindowSurface.release();
            mRecordWindowSurface = null;
        }
    }

    /**
     * 切换相机
     */
    void internalSwitchCamera() {
        CameraUtils.switchCamera(1 - CameraUtils.getCameraID(), mCameraTexture,
                DrawerManager.this, mPreviewBuffer);
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
