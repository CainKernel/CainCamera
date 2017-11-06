package com.cgfay.caincamera.core;

import android.view.Surface;

import com.cgfay.caincamera.gles.EglCore;

/**
 * 视频录制管理器
 * Created by cain.huang on 2017/11/3.
 */

public final class RecorderManager {

    private static final String TAG = "RecorderManager";

    private static RecorderManager mInstance;

    private RecorderThread mRecorderThread;
    private RecorderHandler mRecorderHandler;

    // 操作锁
    private final Object mSynOperation = new Object();

    public static RecorderManager getInstance() {
        if (mInstance == null) {
            mInstance = new RecorderManager();
        }
        return mInstance;
    }

    private RecorderManager() {

    }

    /**
     * 创建录制线程
     */
    synchronized public void create() {
        mRecorderThread = new RecorderThread("RecorderThread");
        mRecorderThread.start();
        mRecorderHandler = new RecorderHandler(mRecorderThread.getLooper(), mRecorderThread);
        mRecorderThread.setRecorderHandler(mRecorderHandler);
    }

    /**
     * 销毁录制线程
     */
    synchronized public void release() {
        // Handler不存在时，需要销毁当前线程，否则可能会出现重新打开不了的情况
        if (mRecorderHandler == null) {
            if (mRecorderThread != null) {
                mRecorderThread.quitSafely();
                try {
                    mRecorderThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mRecorderThread = null;
            }
            return;
        }
        mRecorderHandler.sendEmptyMessage(RenderHandler.MSG_DESTROY);
        mRecorderThread.quitSafely();
        try {
            mRecorderThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mRecorderThread = null;
        mRecorderHandler = null;
    }


    /**
     * 设置录制视频的宽高
     * @param width
     * @param height
     */
    public void setRecorderSize(int width, int height) {
        if (mRecorderHandler != null) {
            synchronized (mSynOperation) {
                mRecorderHandler.sendMessage(mRecorderHandler
                        .obtainMessage(RecorderHandler.MSG_RECORDING_SIZE, width, height));
            }
        }
    }

    /**
     * 设置录制共享上下文
     * @param eglCore
     * @param textureId
     * @param surface
     * @param isRecordable
     */
    public void setEglContext(final EglCore eglCore, final int textureId,
                              final Surface surface, final boolean isRecordable) {
        if (mRecorderHandler != null) {
            synchronized (mSynOperation) {
                mRecorderHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mRecorderThread.setEglContext(eglCore, textureId, surface, isRecordable);
                    }
                });
            }
        }
    }


    /**
     * 开始录制
     */
    public void startRecording() {

    }

    /**
     * 发送渲染指令
     */
    public void sendDraw() {
        if (mRecorderHandler != null) {
            synchronized (mSynOperation) {
                mRecorderHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mRecorderThread.addNewFrame();
                    }
                });
            }
        }
    }

    /**
     * 停止录制
     */
    public void stopRecording() {

    }

}
