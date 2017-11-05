package com.cgfay.caincamera.core;

import android.view.SurfaceHolder;

import com.cgfay.caincamera.type.FilterGroupType;
import com.cgfay.caincamera.type.FilterType;


/**
 * 绘制管理器
 * Created by cain on 2017/7/9.
 */

public class DrawerManager {

    private static final String TAG = "DrawerManager";

    private static DrawerManager mInstance;

    private RenderHandler mRenderHandler;
    private RenderThread mRenderThread;

    // 操作锁
    private final Object mSynOperation = new Object();

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

    /**
     * 创建HandlerThread和Handler
     */
    synchronized private void create() {
        mRenderThread = new RenderThread("RenderThread");
        mRenderThread.start();
        mRenderHandler = new RenderHandler(mRenderThread.getLooper(), mRenderThread);
        // 绑定Handler
        mRenderThread.setRenderHandler(mRenderHandler);
    }


    /**
     * 销毁当前持有的Looper 和 Handler
     */
    synchronized private void destory() {
        // Handler不存在时，需要销毁当前线程，否则可能会出现重新打开不了的情况
        if (mRenderHandler == null) {
            if (mRenderThread != null) {
                mRenderThread.quitSafely();
                try {
                    mRenderThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mRenderThread = null;
            }
            return;
        }
        mRenderHandler.sendEmptyMessage(RenderHandler.MSG_DESTROY);
        mRenderThread.quitSafely();
        try {
            mRenderThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mRenderThread = null;
        mRenderHandler = null;
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
}
