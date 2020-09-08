package com.cgfay.cavfoundation.capture;

import android.opengl.EGLContext;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.cgfay.cavfoundation.codec.CAVVideoInfo;
import com.cgfay.filter.gles.EglCore;
import com.cgfay.filter.gles.WindowSurface;
import com.cgfay.filter.glfilter.base.GLImageFilter;
import com.cgfay.filter.glfilter.utils.OpenGLUtils;
import com.cgfay.filter.glfilter.utils.TextureRotationUtils;

import java.lang.ref.WeakReference;
import java.nio.FloatBuffer;

/**
 * 录制视频预览Layer
 */
public class CAVCaptureVideoPreviewLayer implements Runnable {

    private final String TAG = getClass().getSimpleName();

    // 初始化
    private static final int MSG_INIT = 0;
    // 停止
    private static final int MSG_STOP = 1;
    // 渲染一帧
    private static final int MSG_RENDER_FRAME = 2;
    // 退出
    private static final int MSG_QUIT = 3;

    private final Object mSync = new Object();

    private WindowSurface mWindowSurface;
    private EglCore mEglCore;
    private GLImageFilter mImageFilter;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;

    private VideoProcessHandler mHandler;

    private boolean mReady;
    private boolean mRunning;
    private int mDrawFrameIndex;
    private long mFirstTime;

    private final WeakReference<CAVCaptureRecorder> mWeakRecorder;

    private CAVVideoInfo mVideoInfo;

    public CAVCaptureVideoPreviewLayer(CAVCaptureRecorder recorder) {
        mWeakRecorder = new WeakReference<>(recorder);
    }

    public void setVideoInfo(@NonNull CAVVideoInfo videoInfo) {
        mVideoInfo = videoInfo;
    }

    @Override
    public void run() {
        Looper.prepare();
        synchronized (mSync) {
            mHandler = new VideoProcessHandler(this);
            mReady = true;
            mSync.notify();
        }
        Looper.loop();
        Log.d(TAG, "Video processor thread exiting...");
        synchronized (mSync) {
            mReady = mRunning = false;
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    /**
     * 开始处理
     * @param eglContext
     */
    public void start(@NonNull EGLContext eglContext) {
        Log.d(TAG, "start: ");
        synchronized (mSync) {
            if (mRunning) {
                return;
            }
            mRunning = true;
            new Thread(this, TAG).start();
            while (!mReady) {
                try {
                    mSync.wait();
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
        // 初始化
        if (mHandler != null) {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_INIT, eglContext));
        }
    }

    /**
     * 停止处理
     */
    public void stop() {
        Log.d(TAG, "stop: ");
        if (mHandler != null) {
            mHandler.sendEmptyMessage(MSG_STOP);
            mHandler.sendEmptyMessage(MSG_QUIT);
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        Log.d(TAG, "release: ");
        if (mHandler != null) {
            mHandler.sendEmptyMessage(MSG_QUIT);
        }
    }

    /**
     * 渲染一个视频帧
     */
    public void renderFrame(int texture, long timestamp) {
        if (!isProcessing()) {
            return;
        }
        Log.d(TAG, "renderFrame: " + timestamp);
        synchronized (mSync) {
            if (!mReady) {
                return;
            }
        }
        if (mHandler != null) {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_RENDER_FRAME,
                    (int) (timestamp >> 32), (int) timestamp, texture));
        }
    }

    /**
     * 是否正在处理
     */
    public boolean isProcessing() {
        synchronized (mSync) {
            return mRunning && mReady;
        }
    }

    /**
     * 初始化处理器
     * @param eglContext    EGLContext
     */
    private void onInitProcess(@NonNull EGLContext eglContext) {
        CAVCaptureRecorder recorder = mWeakRecorder.get();
        if (recorder == null) {
            return;
        }
        Surface surface = recorder.getInputSurface();
        if (surface == null) {
            return;
        }
        Log.d(TAG, "onInitProcess: ");
        mVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
        mEglCore = new EglCore(eglContext, EglCore.FLAG_RECORDABLE);
        mWindowSurface = new WindowSurface(mEglCore, surface, true);
        mWindowSurface.makeCurrent();
        if (mVideoInfo != null) {
            mImageFilter = new GLImageFilter(null);
            mImageFilter.onInputSizeChanged(mVideoInfo.getWidth(), mVideoInfo.getHeight());
            mImageFilter.onDisplaySizeChanged(mVideoInfo.getWidth(), mVideoInfo.getHeight());
        }
        mDrawFrameIndex = 0;
        mFirstTime = 0;
    }

    /**
     * 停止处理
     */
    private void onStopProcess() {
        Log.d(TAG, "onStopProcess: ");
        if (mImageFilter != null) {
            mImageFilter.release();
            mImageFilter = null;
        }
        if (mWindowSurface != null) {
            mWindowSurface.release();
            mWindowSurface = null;
        }
        if (mEglCore != null) {
            mEglCore.release();
            mEglCore = null;
        }
    }

    /**
     * 渲染一帧视频
     * @param texture           绘制纹理
     * @param timestampNanos    时钟
     */
    private void onRenderFrame(int texture, long timestampNanos) {
        CAVCaptureRecorder recorder = mWeakRecorder.get();
        if (recorder == null) {
            return;
        }
        Log.d(TAG, "onRenderFrame: " + timestampNanos);
        float speed = recorder.getSpeed();
        // 倍速录制
        if (speed > 1.0f) {
            int interval = (int) speed;
            if (mDrawFrameIndex % interval == 0) {
                drawFrame(texture, getPTS(timestampNanos, speed));
            }
        } else {
            drawFrame(texture, getPTS(timestampNanos, speed));
        }
        mDrawFrameIndex++;
    }

    /**
     * 绘制一帧数据
     * @param texture
     * @param timestampNanos
     */
    private void drawFrame(int texture, long timestampNanos) {
        CAVCaptureRecorder recorder = mWeakRecorder.get();
        if (recorder == null) {
            return;
        }
        if (mWindowSurface == null || mImageFilter == null) {
            return;
        }
        mWindowSurface.makeCurrent();
        mImageFilter.drawFrame(texture, mVertexBuffer, mTextureBuffer);
        mWindowSurface.setPresentationTime(timestampNanos);
        mWindowSurface.swapBuffers();
        recorder.updateRecordFrame(timestampNanos);
    }

    /**
     * 计算时间戳
     * @return 当前的时间戳
     */
    private long getPTS(long timestampNanos, float speed) {
        if (mFirstTime <= 0) {
            mFirstTime = timestampNanos;
        }
        return (long) ((timestampNanos - mFirstTime) / speed);
    }

    /**
     * 处理视频帧渲染逻辑
     */
    private static class VideoProcessHandler extends Handler {

        private final WeakReference<CAVCaptureVideoPreviewLayer> mWeakProcessor;

        public VideoProcessHandler(CAVCaptureVideoPreviewLayer processor) {
            mWeakProcessor = new WeakReference<>(processor);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            CAVCaptureVideoPreviewLayer processor = mWeakProcessor.get();
            if (processor == null) {
                return;
            }
            switch (msg.what) {
                case MSG_INIT: {
                    EGLContext eglContext = (EGLContext) msg.obj;
                    processor.onInitProcess(eglContext);
                    break;
                }

                case MSG_STOP: {
                    processor.onStopProcess();
                    break;
                }

                case MSG_RENDER_FRAME: {
                    long timestamp = (((long) msg.arg1) << 32) |
                            (((long) msg.arg2) & 0xffffffffL);
                    processor.onRenderFrame((int)msg.obj, timestamp);
                    break;
                }

                case MSG_QUIT: {
                    Looper.myLooper().quit();
                    break;
                }
            }
        }
    }
}
