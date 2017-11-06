package com.cgfay.caincamera.core;

import android.os.HandlerThread;
import android.view.Surface;

import com.cgfay.caincamera.filter.base.DisplayFilter;
import com.cgfay.caincamera.gles.EglCore;
import com.cgfay.caincamera.gles.WindowSurface;

import java.lang.ref.WeakReference;

/**
 * 录制线程
 * Created by cain on 2017/11/5.
 */

public class RecorderThread extends HandlerThread {

    private final Object mSync = new Object();

    // 录制视频的宽高
    private int mWidth;
    private int mHeight;

    private WeakReference<EglCore> mWeakEglCore;
    private WindowSurface mRecordSurface;

    // 帧可用
    private final Object mSyncFrameNum = new Object();
    private int mFrameNum = 0;
    private boolean mRequestDraw;

    // 水印filter
    private DisplayFilter mFilter;

    private boolean mIsRecordable;

    private int mCurrentTextureId;

    private RecorderHandler mRecorderHandler;

    public RecorderThread(String name) {
        super(name);
    }

    public void setRecorderHandler(RecorderHandler handler) {
        mRecorderHandler = handler;
    }

    /**
     * 设置录制的大小
     */
    void setRecordingSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    /**
     * 释放资源
     */
    void release() {
        if (mRecorderHandler != null) {
            mRecorderHandler.removeCallbacksAndMessages(null);
            mRecorderHandler = null;
        }

        if (mFilter != null) {
            mFilter.release();
            mFilter = null;
        }

        if (mRecordSurface != null) {
            mRecordSurface.release();
            mRecordSurface = null;
        }

        // 由于是外面传递进来的EglCore，因此这里不需要释放
        if (mWeakEglCore != null) {
            mWeakEglCore.clear();
            mWeakEglCore = null;
        }
    }


    /**
     * 渲染帧
     */
    void renderFrame() {

        synchronized (mSync) {
            mRequestDraw = mFrameNum > 0;
            if (mRequestDraw) {
                mFrameNum--;
            }
        }

        if (mRequestDraw) {
            if (mWeakEglCore != null && mWeakEglCore.get() != null && mCurrentTextureId > 0) {
                mRecordSurface.makeCurrent();
                draw();
                mRecordSurface.swapBuffers();
            }
        }
    }

    /**
     * 绘制
     */
    private void draw() {
        mFilter.drawFrame(mCurrentTextureId);
    }

    /**
     * 初始化滤镜层
     */
    void initFilter() {
        if (mFilter != null) {
            mFilter.release();
            mFilter = null;
        }
        mFilter = new DisplayFilter();
        mFilter.onInputSizeChanged(mWidth, mHeight);
        mFilter.onDisplayChanged(mWidth, mHeight);
    }

    /**
     * 设置录制共享上下文
     * @param eglCore
     * @param textureId
     * @param surface
     * @param isRecordable
     */
    public void setEglContext(EglCore eglCore, int textureId,
                              Surface surface, boolean isRecordable) {
        synchronized (mSync) {
            mWeakEglCore = new WeakReference<EglCore>(eglCore);
            mRecordSurface = new WindowSurface(eglCore, surface, true);
            mCurrentTextureId = textureId;
            mIsRecordable = isRecordable;
            mSync.notifyAll();
            try {
                mSync.wait();
            } catch (InterruptedException e) {

            }

            if (mRecorderHandler != null) {
                mRecorderHandler.sendMessage(mRecorderHandler
                        .obtainMessage(RecorderHandler.MSG_INIT_FILTER));
            }
        }
    }


    /**
     * 添加新的一帧
     */
    public void addNewFrame() {
        synchronized (mSyncFrameNum) {
            mFrameNum++;
            if (mRecorderHandler != null) {
                mRecorderHandler.removeMessages(RecorderHandler.MSG_FRAME_RENDER);
                mRecorderHandler.sendMessageAtFrontOfQueue(mRecorderHandler
                        .obtainMessage(RecorderHandler.MSG_FRAME_RENDER));
            }
        }
    }
}
