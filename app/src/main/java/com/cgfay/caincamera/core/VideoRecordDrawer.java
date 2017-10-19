package com.cgfay.caincamera.core;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Surface;

import com.cgfay.caincamera.filter.base.DisplayFilter;
import com.cgfay.caincamera.gles.EglCore;
import com.cgfay.caincamera.gles.WindowSurface;
import com.cgfay.caincamera.multimedia.RecordDrawer;

import java.lang.ref.WeakReference;

/**
 * 视频录制绘制器
 * Created by cain on 2017/10/19.
 */

public class VideoRecordDrawer implements RecordDrawer {

    private static final boolean VERBOSE = false;
    private static final String TAG = "VideoRecordDrawer";

    private final Object mSync = new Object();

    private HandlerThread mHandlerThread;
    private RecordRenderHandler mRenderHandler;

    // 录制视频的宽高
    private int mWidth;
    private int mHeight;

    private WeakReference<EglCore> mWeakEglCore;
    private WindowSurface mRecordSurface;

    private boolean mIsRecordable;

    private int mCurrentTextureId;

    /**
     * 构造函数
     * @param width
     * @param height
     */
    public VideoRecordDrawer(String name, int width, int height) {
        mWidth = width;
        mHeight = height;
        create(!TextUtils.isEmpty(name) ? name : TAG);
    }

    /**
     * 创建新线程
     * @param name
     */
    private void create(String name) {
        mHandlerThread = new HandlerThread(name);
        mHandlerThread.start();
        mRenderHandler = new RecordRenderHandler(mHandlerThread.getLooper());
        mRenderHandler.sendEmptyMessage(RecordRenderHandler.MSG_INIT);
    }

    /**
     * 销毁当前持有的Looper 和 Handler
     */
    public void release() {
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
        mRenderHandler.sendEmptyMessage(RecordRenderHandler.MSG_DESTROY);
        mHandlerThread.quitSafely();
        try {
            mHandlerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mHandlerThread = null;
        mRenderHandler = null;
    }


    /**
     * 设置绘制共享上下文
     * @param eglCore
     * @param textureId
     * @param surface
     * @param isRecordable
     */
    public void setEglContext(EglCore eglCore, final int textureId,
                              final Surface surface, boolean isRecordable) {
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
            if (mRenderHandler != null) {
                mRenderHandler.sendMessage(mRenderHandler
                        .obtainMessage(RecordRenderHandler.MSG_SET_CONTEXT));
            }
        }
    }

    /**
     * 发送绘制消息
     */
    public void sendDraw() {
        if (mRenderHandler != null) {
            mRenderHandler.addNewRecordFrame();
        }
    }

    // 录制渲染handler
    private class RecordRenderHandler extends Handler {

        static final int MSG_INIT = 0x01;

        static final int MSG_DESTROY = 0x02;

        static final int MSG_SET_CONTEXT = 0x03;

        static final int MSG_FRAME_RENDER = 0x10;

        // 帧可用
        private final Object mSyncFrameNum = new Object();
        private int mFrameNum = 0;
        private boolean mRequestDraw;

        // 水印filter
        private DisplayFilter mFilter;

        public RecordRenderHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 初始化
                case MSG_INIT:
                    init();
                    break;

                // 设置EGLContext上下文
                case MSG_SET_CONTEXT:
                    setContext();
                    break;

                // 录制渲染
                case MSG_FRAME_RENDER:
                    renderFrame();
                    break;

                // 销毁
                case MSG_DESTROY:
                    release();
                    break;
            }
        }

        /**
         * 初始化
         */
        private void init() {

        }


        /**
         * 设置OpenGLES共享上下文
         */
        private void setContext() {
            if (mFilter != null) {
                mFilter.release();
                mFilter = null;
            }
            mFilter = new DisplayFilter();
            mFilter.onInputSizeChanged(mWidth, mHeight);
            mFilter.onDisplayChanged(mWidth, mHeight);
        }

        /**
         * 渲染帧
         */
        private void renderFrame() {

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
         * 添加等待渲染的新帧
         */
        public void addNewRecordFrame() {
            synchronized (mSyncFrameNum) {
                mFrameNum++;
                removeMessages(MSG_FRAME_RENDER);
                sendMessageAtFrontOfQueue(obtainMessage(MSG_FRAME_RENDER));
            }
        }


        /**
         * 释放资源
         */
        private void release() {
            if (mFilter != null) {
                mFilter.release();
                mFilter = null;
            }
        }
    }

}
