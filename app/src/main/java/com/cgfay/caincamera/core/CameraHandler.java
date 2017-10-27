package com.cgfay.caincamera.core;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.SurfaceHolder;

import com.cgfay.caincamera.utils.CameraUtils;

import java.lang.ref.WeakReference;

/**
 * Created by cain.huang on 2017/10/20.
 */

public class CameraHandler extends Handler {

    static final int MSG_INIT = 0x00;
    static final int MSG_OPEN_CAMERA = 0x01;
    static final int MSG_START_PREVIEW = 0x02;
    static final int MSG_SWITCH_CAMERA = 0x03;
    static final int MSG_RELEASER_CAMERA = 0x04;
    static final int MSG_STOP_PREVIEW = 0x05;
    static final int MSG_ADD_PREVIEW_CALLBACK = 0x06;

    static final int MSG_SWITCH_CAMERA_WITH_CALLBACK = 0x10;
    static final int MSG_REOPEN_CAMERA = 0x11;
    static final int MSG_SET_RATIO = 0x12;


    private WeakReference<CameraThread> mWeakCameraThread;

    private Camera.PreviewCallback mPreviewCallback;
    private byte[] mPreviewBuffer;


    public CameraHandler(Looper looper, CameraThread cameraThread) {
        super(looper);
        mWeakCameraThread = new WeakReference<CameraThread>(cameraThread);
    }

    @Override
    public void handleMessage(Message msg) {
        if (mWeakCameraThread == null || mWeakCameraThread.get() == null) {
            return;
        }
        CameraThread cameraThread = mWeakCameraThread.get();
        switch (msg.what) {
            // 初始化
            case MSG_INIT:
                init();
                break;

            // 打开相机
            case MSG_OPEN_CAMERA:
                openCamera(msg.arg1, msg.arg2);
                break;

            // 开始预览
            case MSG_START_PREVIEW:
                if (msg.obj instanceof SurfaceHolder) {
                    startPreviewDisplay((SurfaceHolder) msg.obj);
                } else if (msg.obj instanceof SurfaceTexture) {
                    startPreviewTexture((SurfaceTexture) msg.obj);
                }
                break;

            // 切换相机
            case MSG_SWITCH_CAMERA:
                if (msg.obj != null && msg.obj instanceof SurfaceHolder) {
                    switchCamera(msg.arg1, (SurfaceHolder) msg.obj);
                } else {
                    switchCamera(msg.arg1);
                }
                break;

            // 切换相机
            case MSG_SWITCH_CAMERA_WITH_CALLBACK:
                switchCameraWithCallbacks(msg.arg1);
                break;

            //  重新打开相机
            case MSG_REOPEN_CAMERA:
                reOpenCamera();
                break;

            // 释放相机
            case MSG_RELEASER_CAMERA:
                releaseCamera();
                break;

            // 停止预览
            case MSG_STOP_PREVIEW:
                stopPreview();
                break;

            // 添加预览回调
            case MSG_ADD_PREVIEW_CALLBACK:
                handlePreviewCallbacks();
                break;

            // 设置长宽比
            case MSG_SET_RATIO:
                setCurrentAspectRatio((AspectRatioType)msg.obj);
                break;
        }
    }

    /**
     * 初始化
     */
    private void init() {

    }

    /**
     * 打开相机
     * @param cameraId
     * @param expectFps
     */
    private void openCamera(int cameraId, int expectFps) {
        if (cameraId == -1) {
            CameraUtils.openCamera(expectFps);
        } else {
            CameraUtils.openCamera(cameraId, expectFps);
        }
    }

    /**
     * 开始预览
     * @param holder
     */
    private void startPreviewDisplay(SurfaceHolder holder) {
        CameraUtils.startPreviewDisplay(holder);
    }

    /**
     * 开始预览
     * @param texture
     */
    public void startPreviewTexture(SurfaceTexture texture) {
        CameraUtils.startPreviewTexture(texture);
    }

    /**
     * 切换相机
     * @param cameraId
     * @param holder
     */
    private void switchCamera(int cameraId, SurfaceHolder holder) {
        CameraUtils.switchCamera(cameraId, holder);
    }

    /**
     * 切换相机
     * @param cameraId
     */
    private void switchCamera(int cameraId) {
        CameraUtils.switchCamera(cameraId);
    }

    /**
     * 切换相机
     * @param cameraId
     */
    private void switchCameraWithCallbacks(int cameraId) {
        CameraUtils.switchCamera(cameraId, mPreviewCallback, mPreviewBuffer);
    }

    /**
     * 重新打开相机
     */
    private void reOpenCamera() {
        CameraUtils.reOpenCamera();
    }
    /**
     * 释放相机
     */
    private void releaseCamera() {
        CameraUtils.releaseCamera();
    }

    /**
     * 停止预览
     */
    private void stopPreview() {
        CameraUtils.stopPreview();
    }

    /**
     * 处理添加预览回调
     */
    private void handlePreviewCallbacks() {
        CameraUtils.addPreviewCallbacks(mPreviewCallback, mPreviewBuffer);
    }

    /**
     * 添加预览回调
     * @param callback
     * @param previewBuffer
     */
    public void addPreviewCallbacks(Camera.PreviewCallback callback, byte[] previewBuffer) {
        mPreviewCallback = callback;
        mPreviewBuffer = previewBuffer;
        sendMessage(obtainMessage(MSG_ADD_PREVIEW_CALLBACK));
    }

    /**
     * 设置长宽比
     * @param type
     */
    private void setCurrentAspectRatio(AspectRatioType type) {
        CameraUtils.setCurrentAspectRatio(type);
    }

    /**
     * 获取相机线程
     * @return
     */
    public CameraThread getCameraThread() {
        return mWeakCameraThread == null ? null : mWeakCameraThread.get();
    }

}
