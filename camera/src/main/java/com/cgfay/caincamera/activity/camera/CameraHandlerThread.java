package com.cgfay.caincamera.activity.camera;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.SurfaceHolder;

import com.cgfay.utilslibrary.CameraInfo;
import com.cgfay.utilslibrary.Size;

/**
 * 相机操作线程队列
 * Created by cain.huang on 2017/11/1.
 */

public class CameraHandlerThread extends HandlerThread {

    private static final String TAG = "CameraHandlerThread";

    private final Handler mHandler;

    public CameraHandlerThread() {
        super(TAG);
        start();
        mHandler = new Handler(getLooper());
    }

    public CameraHandlerThread(String name) {
        super(name);
        start();
        mHandler = new Handler(getLooper());
    }

    /**
     * 销毁线程
     */
    public void destoryThread() {
        releaseCamera();
        mHandler.removeCallbacksAndMessages(null);
        quitSafely();
    }


    /**
     * 检查handler是否可用
     */
    private void checkHandleAvailable() {
        if (mHandler == null) {
            throw new NullPointerException("Handler is not available!");
        }
    }

    /**
     * 等待操作完成
     */
    private void waitUntilReady() {
        try {
            wait();
        } catch (InterruptedException e) {
            Log.w(TAG, "wait was interrupted");
        }
    }

    /**
     * 打开相机
     */
    synchronized public void openCamera() {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalOpenCamera();
                notifyCameraOpened();
            }
        });
        waitUntilReady();
    }

    /**
     * 打开相机
     * @param expectFps 期望帧率
     */
    synchronized public void openCamera(final int expectFps) {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalOpenCamera(expectFps);
                notifyCameraOpened();
            }
        });
        waitUntilReady();
    }

    /**
     * 打开相机
     * @param cameraId  相机Id
     * @param expectFps 期望帧率
     */
    synchronized public void openCamera(final int cameraId, final int expectFps) {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalOpenCamera(cameraId, expectFps);
                notifyCameraOpened();
            }
        });
        waitUntilReady();
    }

    /**
     * 打开相机
     * @param cameraId      相机Id
     * @param expectFps     期望帧率
     * @param expectWidth   期望宽度
     * @param expectHeight  期望高度
     */
    synchronized public void openCamera(final int cameraId, final int expectFps,
                                        final int expectWidth, final int expectHeight) {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalOpenCamera(cameraId, expectFps, expectWidth, expectHeight);
                notifyCameraOpened();
            }
        });
        waitUntilReady();
    }


    /**
     * 通知相机已打开，主要的作用是，如果在打开之后要立即获得mCamera实例，则需要添加wait()-notify()
     * wait() - notify() 不是必须的
     */
    synchronized private void notifyCameraOpened() {
        notify();
    }


    /**
     * 重新打开相机
     */
    synchronized public void reopenCamera() {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalReopenCamera();
                notifyCameraOpened();
            }
        });
        waitUntilReady();
    }

    /**
     * 重新打开相机
     * @param expectFps 期望帧率
     */
    synchronized public void reopenCamera(final int expectFps) {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalReopenCamera(expectFps);
                notifyCameraOpened();
            }
        });
        waitUntilReady();
    }


    /**
     * 重新打开相机
     * @param expectFps     期望帧率
     * @param expectWidth   期望宽度
     * @param expectHeight  期望高度
     */
    synchronized public void reopenCamera(final int expectFps,
                                          final int expectWidth, final int expectHeight) {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalReopenCamera(expectFps, expectWidth, expectHeight);
                notifyCameraOpened();
            }
        });
        waitUntilReady();
    }

    /**
     * 设置预览Surface
     * @param holder SurfaceHolder
     */
    public void setPreviewSurface(final SurfaceHolder holder) {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalPreviewSurface(holder);
            }
        });
    }

    /**
     * 设置预览Surface
     * @param texture   SurfaceTexture
     */
    public void setPreviewSurface(final SurfaceTexture texture) {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalPreviewSurface(texture);
            }
        });
    }

    /**
     * 设置预览回调
     * @param callback  回调
     * @param buffer    缓冲
     */
    public void setPreviewCallbackWithBuffer(final Camera.PreviewCallback callback,
                                             final byte[] buffer) {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalPreviewCallbackWithBuffer(callback, buffer);
            }
        });
    }

    /**
     * 开始预览
     */
    public void startPreview() {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalStartPreview();
            }
        });
    }

    /**
     * 停止预览
     */
    public void stopPreview() {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalStopPreview();
            }
        });
    }

    /**
     * 切换相机
     * @param cameraId 相机Id
     */
    public void switchCamera(final int cameraId) {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalSwitchCamera(cameraId);
            }
        });
    }

    /**
     * 切换相机
     * @param cameraId  相机Id
     * @param expectFps 期望帧率
     */
    public void switchCamera(final int cameraId, final int expectFps) {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalSwitchCamera(cameraId, expectFps);
            }
        });
    }

    /**
     * 切换相机
     * @param cameraId      相机Id
     * @param expectFps     期望帧率
     * @param expectWidth   期望宽度
     * @param expectHeight  期望高度
     */
    public void switchCamera(final int cameraId, final int expectFps, final int expectWidth, final int expectHeight) {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalSwitchCamera(cameraId, expectFps, expectWidth, expectHeight);
            }
        });
    }

    /**
     * 切换相机
     * @param cameraId
     * @param holder
     * @param callback
     * @param buffer
     */
    public void switchCameraAndPreview(final int cameraId, final SurfaceHolder holder,
                             final Camera.PreviewCallback callback, final byte[] buffer) {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalSwitchCameraAndPreview(cameraId, holder, callback, buffer);
            }
        });
    }

    /**
     * 切换相机
     * @param cameraId
     * @param texture
     * @param callback
     * @param buffer
     */
    public void switchCameraAndPreview(final int cameraId, final SurfaceTexture texture,
                             final Camera.PreviewCallback callback, final byte[] buffer) {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalSwitchCameraAndPreview(cameraId, texture, callback, buffer);
            }
        });
    }



    /**
     * 释放相机
     */
    synchronized public void releaseCamera() {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalReleaseCamera();
                notifyCameraReleased();
            }
        });
        waitUntilReady();
    }

    /**
     * 通知销毁成功
     */
    synchronized private void notifyCameraReleased() {
        notify();
    }

    /**
     * 拍照
     * @param shutterCallback
     * @param rawCallback
     * @param pictureCallback
     */
    public void takePicture(final Camera.ShutterCallback shutterCallback,
                            final Camera.PictureCallback rawCallback,
                            final Camera.PictureCallback pictureCallback) {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalTakePicture(shutterCallback, rawCallback, pictureCallback);
            }
        });
    }

    /**
     * 计算预览角度
     * @param activity
     */
    synchronized public void calculatePreviewOrientation(final Activity activity) {
        checkHandleAvailable();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                internalCalculatePreviewOrientation(activity);
                notifyPreviewOrientationCalculated();
            }
        });

        try {
            wait();
        } catch (InterruptedException e) {
            Log.w(TAG, "wait was interrupted");
        }
    }

    /**
     * 通知计算预览角度完成
     */
    synchronized private void notifyPreviewOrientationCalculated() {
        notify();
    }



    // ------------------------------- 内部方法 -----------------------------
    /**
     * 打开相机
     */
    private void internalOpenCamera() {
        internalOpenCamera(CameraManager.getInstance().DESIRED_PREVIEW_FPS);
    }

    /**
     * 打开相机
     * @param expectFps 期望的帧率
     */
    private void internalOpenCamera(int expectFps) {
        CameraManager.getInstance().openCamera(expectFps);
    }

    /**
     * 打开相机
     * @param cameraId 相机Id
     * @param expectFps 期望帧率
     */
    private void internalOpenCamera(int cameraId, int expectFps) {
        CameraManager.getInstance().openCamera(cameraId, expectFps);
    }

    /**
     * 打开相机
     * @param cameraId      相机帧率
     * @param expectFps     期望帧率
     * @param expectWidth   期望宽度
     * @param expectHeight  期望高度
     */
    private void internalOpenCamera(int cameraId, int expectFps,
                                    int expectWidth, int expectHeight) {
        CameraManager.getInstance().openCamera(cameraId, expectFps, expectWidth, expectHeight);
    }

    /**
     * 重新打开相机
     */
    private void internalReopenCamera() {
        CameraManager.getInstance().reopenCamera();
    }

    /**
     * 重新打开相机
     * @param expectFps 期望帧率
     */
    private void internalReopenCamera(int expectFps) {
        CameraManager.getInstance().reopenCamera();
    }

    /**
     * 重新打开相机
     * @param expectFps 期望帧率
     * @param expectWidth   期望宽度
     * @param expectHeight  期望高度
     */
    public void internalReopenCamera(int expectFps, int expectWidth, int expectHeight) {
        CameraManager.getInstance().reopenCamera(expectFps, expectWidth, expectHeight);
    }

    /**
     * 预览Surface
     * @param holder
     */
    private void internalPreviewSurface(SurfaceHolder holder) {
        CameraManager.getInstance().setPreviewSurface(holder);
    }

    /**
     * 预览Surface
     * @param texture
     */
    private void internalPreviewSurface(SurfaceTexture texture) {
        CameraManager.getInstance().setPreviewSurface(texture);
    }

    /**
     * 设置预览回调
     * @param callback  预览回调
     * @param buffer    预览回调
     */
    private void internalPreviewCallbackWithBuffer(Camera.PreviewCallback callback, byte[] buffer) {
        CameraManager.getInstance().setPreviewCallbackWithBuffer(callback, buffer);
    }

    /**
     * 开始预览
     */
    private void internalStartPreview() {
        CameraManager.getInstance().startPreview();
    }

    /**
     * 停止预览
     */
    private void internalStopPreview() {
        CameraManager.getInstance().stopPreview();
    }

    /**
     * 切换相机
     * @param cameraId  相机的Id
     */
    private void internalSwitchCamera(int cameraId) {
        CameraManager.getInstance().switchCamera(cameraId);
    }

    /**
     * 切换相机
     * @param cameraId 相机Id
     * @param expectFps 期望帧率
     */
    private void internalSwitchCamera(int cameraId, int expectFps) {
        CameraManager.getInstance().switchCamera(cameraId, expectFps);
    }

    /**
     * 切换相机
     * @param cameraId      相机Id
     * @param expectFps     期望帧率
     * @param expectWidth   期望宽度
     * @param expectHeight  期望高度
     */
    private void internalSwitchCamera(int cameraId, int expectFps,
                                      int expectWidth, int expectHeight) {
        CameraManager.getInstance().switchCamera(cameraId, expectFps, expectWidth, expectHeight);
    }

    /**
     * 切换相机并预览
     * @param cameraId  相机的Id
     * @param holder    绑定的SurfaceHolder
     * @param callback  预览回调
     * @param buffer    缓冲buffer
     */
    private void internalSwitchCameraAndPreview(int cameraId, SurfaceHolder holder,
                                                Camera.PreviewCallback callback, byte[] buffer) {
        CameraManager.getInstance().switchCameraAndPreview(cameraId, holder, callback, buffer);
    }

    /**
     * 切换相机并预览
     * @param cameraId  相机Id
     * @param texture   绑定的SurfaceTexture
     * @param callback  预览回调
     * @param buffer    缓冲buffer
     */
    private void internalSwitchCameraAndPreview(int cameraId, SurfaceTexture texture,
                                      Camera.PreviewCallback callback, byte[] buffer) {
        CameraManager.getInstance().switchCameraAndPreview(cameraId, texture, callback, buffer);
    }

    /**
     * 释放相机
     */
    private void internalReleaseCamera() {
        CameraManager.getInstance().releaseCamera();
    }

    /**
     * 拍照
     * @param shutterCallback
     * @param rawCallback
     * @param pictureCallback
     */
    private void internalTakePicture(Camera.ShutterCallback shutterCallback,
                                     Camera.PictureCallback rawCallback,
                                     Camera.PictureCallback pictureCallback) {
        CameraManager.getInstance().takePicture(shutterCallback, rawCallback, pictureCallback);
    }

    /**
     * 计算预览角度
     * @param activity
     */
    private void internalCalculatePreviewOrientation(Activity activity) {
        CameraManager.getInstance().calculateCameraPreviewOrientation(activity);
    }



    // ------------------------------------- setter and getter -------------------------------------

    /**
     * 获取回调
     * @return
     */
    public Handler getHandler() {
        return mHandler;
    }

    /**
     * 获取相机Id
     * @return
     */
    public int getCameraId() {
        return CameraManager.getInstance().getCameraID();
    }

    /**
     * 获取照片的大小
     * @return
     */
    public Size getPictureSize() {
        return CameraManager.getInstance().getPictureSize();
    }

    /**
     * 获取当前预览的大小
     * @return
     */
    public Size getPreviewSize() {
        return CameraManager.getInstance().getPreviewSize();
    }

    /**
     * 获取相机信息
     * @return
     */
    public CameraInfo getCameraInfo() {
        return CameraManager.getInstance().getCameraInfo();
    }

    /**
     * 获取当前预览角度
     * @return
     */
    public int getPreviewOrientation() {
        return CameraManager.getInstance().getPreviewOrientation();
    }

    /**
     * 获取帧率(FPS 千秒值)
     * @return
     */
    public int getCameraPreviewThousandFps() {
        return CameraManager.getInstance().getCameraPreviewThousandFps();
    }

    /**
     * 获取当前的长宽比
     * @return
     */
    public float getCurrentRatio() {
        return CameraManager.getInstance().getCurrentRatio();
    }

}