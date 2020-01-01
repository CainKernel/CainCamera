package com.cgfay.camera.camera;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.lifecycle.LifecycleOwner;

import java.util.concurrent.Executor;

/**
 * CameraX库封装处理
 */
public class CameraXController implements ICameraController {

    private static final String TAG = "CameraXController";
    // 16:9的默认宽高(理想值)，CameraX的预览方式与Camera1不一致，设置的预览宽高需要是实际的预览宽高
    private static final int DEFAULT_16_9_WIDTH = 720;
    private static final int DEFAULT_16_9_HEIGHT = 1280;

    // 预览宽度
    private int mPreviewWidth = DEFAULT_16_9_WIDTH;
    // 预览高度
    private int mPreviewHeight = DEFAULT_16_9_HEIGHT;
    // 预览角度
    private int mRotation;

    // 生命周期对象(Fragment/Activity)
    private final LifecycleOwner mLifecycleOwner;

    // 是否打开前置摄像头
    private boolean mFacingFront;

    // 预览配置
    private Preview mPreview;

    // 预览帧
    private Executor mExecutor;
    private ImageAnalysis mPreviewAnalyzer;
    // 预览回调
    private PreviewCallback mPreviewCallback;
    // SurfaceTexture准备监听器
    private OnSurfaceTextureListener mSurfaceTextureListener;
    // 纹理更新监听器
    private OnFrameAvailableListener mFrameAvailableListener;
    // 相机数据输出的SurfaceTexture
    private SurfaceTexture mOutputTexture;
    private HandlerThread mOutputThread;

    public CameraXController(@NonNull LifecycleOwner lifecycleOwner, @NonNull Executor executor) {
        Log.d(TAG, "CameraXController: created!");
        mLifecycleOwner = lifecycleOwner;
        mFacingFront = true;
        mExecutor = executor;
        mRotation = 90;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void openCamera() {
        CameraX.unbindAll();
        initCameraConfig();
        CameraX.bindToLifecycle(mLifecycleOwner, mPreview, mPreviewAnalyzer);
    }

    /**
     * 初始化相机配置
     */
    private void initCameraConfig() {
        // 预览画面
        PreviewConfig.Builder previewBuilder = new PreviewConfig.Builder()
                .setLensFacing(mFacingFront ? CameraX.LensFacing.FRONT : CameraX.LensFacing.BACK)
                .setTargetResolution(new Size(mPreviewWidth, mPreviewHeight));
        mPreview = new Preview(previewBuilder.build());
        mPreview.setOnPreviewOutputUpdateListener(output -> {
            releaseSurfaceTexture();
            mOutputTexture = output.getSurfaceTexture();
            if (Build.VERSION.SDK_INT >= 21) {
                if (mOutputThread != null) {
                    mOutputThread.quit();
                    mOutputThread = null;
                }
                mOutputThread = new HandlerThread("FrameAvailableThread");
                mOutputThread.start();
                mOutputTexture.setOnFrameAvailableListener(surfaceTexture -> {
                    if (mFrameAvailableListener != null) {
                        mFrameAvailableListener.onFrameAvailable(surfaceTexture);
                    }
                }, new Handler(mOutputThread.getLooper()));
            } else {
                mOutputTexture.setOnFrameAvailableListener(surfaceTexture -> {
                    if (mFrameAvailableListener != null) {
                        mFrameAvailableListener.onFrameAvailable(surfaceTexture);
                    }
                });
            }
            if (mSurfaceTextureListener != null) {
                mSurfaceTextureListener.onSurfaceTexturePrepared(mOutputTexture);
            }
        });

        // 预览帧回调
        ImageAnalysisConfig analyBuilder = new ImageAnalysisConfig.Builder()
                .setLensFacing(mFacingFront ? CameraX.LensFacing.FRONT : CameraX.LensFacing.BACK)
                .setTargetResolution(new Size(mPreviewWidth, mPreviewHeight))
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .build();
        mPreviewAnalyzer = new ImageAnalysis(analyBuilder);
        mPreviewAnalyzer.setAnalyzer(mExecutor, new PreviewCallbackAnalyzer(this, mPreviewCallback));
    }

    /**
     * 释放输出的SurfaceTexture，防止内存泄露
     */
    private void releaseSurfaceTexture() {
        if (mOutputTexture != null) {
            mOutputTexture.release();
            mOutputTexture = null;
        }
        if (mOutputThread != null) {
            mOutputThread.quitSafely();
            mOutputThread = null;
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void closeCamera() {
        try {
            CameraX.getCameraWithLensFacing(mFacingFront ? CameraX.LensFacing.FRONT : CameraX.LensFacing.BACK);
            CameraX.unbindAll();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            releaseSurfaceTexture();
        }
    }

    @Override
    public void setOnSurfaceTextureListener(OnSurfaceTextureListener listener) {
        mSurfaceTextureListener = listener;
    }

    @Override
    public void setPreviewCallback(PreviewCallback callback) {
        mPreviewCallback = callback;
    }

    @Override
    public void setOnFrameAvailableListener(OnFrameAvailableListener listener) {
        mFrameAvailableListener = listener;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void switchCamera() {
        closeCamera();
        boolean front = isFront();
        setFront(!front);
        openCamera();
    }

    @Override
    public void setFront(boolean front) {
        mFacingFront = front;
    }

    @Override
    public boolean isFront() {
        return mFacingFront;
    }

    public void setOrientation(int rotation) {
        mRotation = rotation;
    }

    @Override
    public int getOrientation() {
        return mRotation;
    }

    @Override
    public int getPreviewWidth() {
        if (mRotation == 90 || mRotation == 270) {
            return mPreviewHeight;
        }
        return mPreviewWidth;
    }

    @Override
    public int getPreviewHeight() {
        if (mRotation == 90 || mRotation == 270) {
            return mPreviewWidth;
        }
        return mPreviewHeight;
    }

    @Override
    public boolean canAutoFocus() {
        return false;
    }

    @Override
    public void setFocusArea(Rect rect) {

    }

    @Override
    public Rect getFocusArea(float x, float y, int width, int height, int focusSize) {
        return null;
    }

    @Override
    public boolean isSupportFlashLight(boolean front) {
        return false;
    }

    @Override
    public void setFlashLight(boolean on) {

    }
}
