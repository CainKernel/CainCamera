package com.cgfay.caincamera.core;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.HandlerThread;
import android.view.SurfaceHolder;

/**
 * 相机线程
 * Created by cain.huang on 2017/10/20.
 */
public class CameraThread {

    private static final String TAG = "CameraThread";
    private static CameraThread mInstance;

    private HandlerThread mHandlerThread;
    private CameraHandler mCameraHandler;

    private final Object mSynOperation = new Object();


    public static synchronized CameraThread getInstance() {
        if (mInstance != null) {
            mInstance = new CameraThread();
        }
        return mInstance;
    }

    public CameraThread() {
        mHandlerThread = new HandlerThread("CameraThread");
        mHandlerThread.start();
        mCameraHandler = new CameraHandler(mHandlerThread.getLooper(), this);
        mCameraHandler.sendEmptyMessage(CameraHandler.MSG_INIT);
    }

    /**
     *  打开前置摄像头相机
     * @param expectFps
     */
    public void openCamera(int expectFps) {
        synchronized (mSynOperation) {
            if (mCameraHandler != null) {
                mCameraHandler.sendMessage(mCameraHandler
                        .obtainMessage(CameraHandler.MSG_OPEN_CAMERA,
                                -1, expectFps));
            }
        }
    }

    /**
     * 打开相机
     * @param cameraId
     * @param expectFps
     */
    public void openCamera(int cameraId, int expectFps) {
        synchronized (mSynOperation) {
            if (mCameraHandler != null) {
                mCameraHandler.sendMessage(mCameraHandler
                        .obtainMessage(CameraHandler.MSG_OPEN_CAMERA, cameraId, expectFps));
            }
        }
    }

    /**
     * 开始预览
     * @param holder
     */
    public void startPreviewDisplay(SurfaceHolder holder) {
        synchronized (mSynOperation) {
            if (mCameraHandler != null) {
                mCameraHandler.sendMessage(mCameraHandler
                        .obtainMessage(CameraHandler.MSG_START_PREVIEW, holder));
            }
        }
    }

    /**
     * 开始预览
     * @param texture
     */
    public void startPreviewTexture(SurfaceTexture texture) {
        synchronized (mSynOperation) {
            if (mCameraHandler != null) {
                mCameraHandler.sendMessage(mCameraHandler
                        .obtainMessage(CameraHandler.MSG_START_PREVIEW, texture));
            }
        }
    }

    /**
     * 切换相机s
     * @param cameraID
     * @param holder
     */
    public void switchCamera(int cameraID, SurfaceHolder holder) {
        synchronized (mSynOperation) {
            if (mCameraHandler != null) {
                mCameraHandler.sendMessage(mCameraHandler
                        .obtainMessage(CameraHandler.MSG_SWITCH_CAMERA,
                                cameraID, 0/* unused */, holder));
            }
        }
    }

    /**
     * 切换相机
     * @param cameraId
     */
    public void switchCamera(int cameraId) {
        synchronized (mSynOperation) {
            if (mCameraHandler != null) {
                mCameraHandler.sendMessage(mCameraHandler
                        .obtainMessage(CameraHandler.MSG_SWITCH_CAMERA,
                                cameraId, 0/* unused */, null));
            }
        }
    }

    /**
     * 切换相机
     * @param cameraId
     * @param callback
     * @param buffers
     */
    public void switchCamera(int cameraId, Camera.PreviewCallback callback, byte[] buffers) {
        addPreviewCallbacks(callback, buffers);
        synchronized (mSynOperation) {
            if (mCameraHandler != null) {
                mCameraHandler.sendMessage(mCameraHandler
                        .obtainMessage(CameraHandler.MSG_SWITCH_CAMERA_WITH_CALLBACK,
                                cameraId, 0 /* unused */, null));
            }
        }
    }

    /**
     * 重新打开相机
     */
    public void reOpenCamera() {
        synchronized (mSynOperation) {
            if (mCameraHandler != null) {
                mCameraHandler
                        .sendMessage(mCameraHandler.obtainMessage(CameraHandler.MSG_REOPEN_CAMERA));
            }
        }
    }

    /**
     * 释放相机
     */
    public void releaseCamera() {
        synchronized (mSynOperation) {
            if (mCameraHandler != null) {
                mCameraHandler.sendMessage(mCameraHandler
                        .obtainMessage(CameraHandler.MSG_RELEASER_CAMERA));
            }
        }
    }

    /**
     * 停止预览
     */
    public void stopPreview() {
        synchronized (mSynOperation) {
            if (mCameraHandler != null) {
                mCameraHandler.sendMessage(mCameraHandler
                        .obtainMessage(CameraHandler.MSG_STOP_PREVIEW));
            }
        }
    }

    /**
     * 添加预览回调
     * @param callback
     * @param previewBuffer
     */
    public void addPreviewCallbacks(Camera.PreviewCallback callback, byte[] previewBuffer) {
        synchronized (mSynOperation) {
            if (mCameraHandler != null) {
                mCameraHandler.addPreviewCallbacks(callback, previewBuffer);
            }
        }
    }

    /**
     * 返回handler
     * @return
     */
    public CameraHandler getCameraHandler() {
        return mCameraHandler;
    }
}
