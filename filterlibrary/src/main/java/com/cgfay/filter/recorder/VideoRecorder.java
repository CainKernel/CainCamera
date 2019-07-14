package com.cgfay.filter.recorder;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.cgfay.filter.gles.EglCore;
import com.cgfay.filter.gles.WindowSurface;
import com.cgfay.filter.glfilter.base.GLImageFilter;
import com.cgfay.filter.glfilter.utils.OpenGLUtils;
import com.cgfay.filter.glfilter.utils.TextureRotationUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.FloatBuffer;

/**
 * 视频录制器
 * @author CainHuang
 * @date 2019/6/30
 */
public class VideoRecorder implements Runnable, VideoEncoder.OnEncodingListener {

    private static final String TAG = "VideoRecorder";
    private static final boolean VERBOSE = true;

    // 开始录制
    private static final int MSG_START_RECORDING = 0;
    // 停止录制
    private static final int MSG_STOP_RECORDING = 1;
    // 录制帧可用
    private static final int MSG_FRAME_AVAILABLE = 2;
    // 退出录制
    private static final int MSG_QUIT = 3;

    // 录制用的OpenGL上下文和EGLSurface
    private WindowSurface mInputWindowSurface;
    private EglCore mEglCore;
    private GLImageFilter mImageFilter;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;

    // 视频编码器
    private VideoEncoder mVideoEncoder;

    // 录制Handler;
    private volatile RecordHandler mHandler;

    // 录制状态锁
    private final Object mReadyFence = new Object();
    private boolean mReady;
    private boolean mRunning;

    // 录制监听器
    private OnRecordListener mRecordListener;

    /**
     * 设置录制监听器
     * @param listener
     */
    public void setOnRecordListner(OnRecordListener listener) {
        mRecordListener = listener;
    }

    /**
     * 开始录制
     * @param params 录制参数
     */
    public void startRecord(VideoParams params) {
        if (VERBOSE) {
            Log.d(TAG, "VideoRecorder: startRecord()");
        }
        synchronized (mReadyFence) {
            if (mRunning) {
                Log.w(TAG, "VideoRecorder thread already running");
                return;
            }
            mRunning = true;
            new Thread(this, "VideoRecorder").start();
            while (!mReady) {
                try {
                    mReadyFence.wait();
                } catch (InterruptedException ie) {
                    // ignore
                }
            }
        }

        mHandler.sendMessage(mHandler.obtainMessage(MSG_START_RECORDING, params));
    }

    /**
     * 停止录制
     */
    public void stopRecord() {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_STOP_RECORDING));
            mHandler.sendMessage(mHandler.obtainMessage(MSG_QUIT));
        }
    }

    /**
     * 释放所有资源
     */
    public void release() {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_QUIT));
        }
    }

    /**
     * 判断是否正在录制
     * @return
     */
    public boolean isRecording() {
        synchronized (mReadyFence) {
            return mRunning;
        }
    }

    /**
     * 录制帧可用状态
     * @param texture
     * @param timestamp
     */
    public void frameAvailable(int texture, long timestamp) {
        synchronized (mReadyFence) {
            if (!mReady) {
                return;
            }
        }
        // 时间戳为0时，不可用
        if (timestamp == 0) {
            return;
        }

        if (mHandler != null) {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_FRAME_AVAILABLE,
                    (int) (timestamp >> 32), (int) timestamp, texture));
        }
    }

    // 编码回调
    @Override
    public void onEncoding(long duration) {
        if (mRecordListener != null) {
            mRecordListener.onRecording(MediaType.VIDEO, duration);
        }
    }


    @Override
    public void run() {
        Looper.prepare();
        synchronized (mReadyFence) {
            mHandler = new RecordHandler(this);
            mReady = true;
            mReadyFence.notify();
        }
        Looper.loop();

        Log.d(TAG, "Video record thread exiting");
        synchronized (mReadyFence) {
            mReady = mRunning = false;
            mHandler = null;
        }
    }

    /**
     * 录制Handler
     */
    private static class RecordHandler extends Handler {

        private WeakReference<VideoRecorder> mWeakRecorder;

        public RecordHandler(VideoRecorder encoder) {
            mWeakRecorder = new WeakReference<VideoRecorder>(encoder);
        }

        @Override
        public void handleMessage(Message inputMessage) {
            int what = inputMessage.what;
            Object obj = inputMessage.obj;

            VideoRecorder encoder = mWeakRecorder.get();
            if (encoder == null) {
                Log.w(TAG, "RecordHandler.handleMessage: encoder is null");
                return;
            }

            switch (what) {
                case MSG_START_RECORDING: {
                    encoder.onStartRecord((VideoParams) obj);
                    break;
                }

                case MSG_STOP_RECORDING: {
                    encoder.onStopRecord();
                    break;
                }

                case MSG_FRAME_AVAILABLE: {
                    long timestamp = (((long) inputMessage.arg1) << 32) |
                            (((long) inputMessage.arg2) & 0xffffffffL);
                    encoder.onRecordFrameAvailable((int)obj, timestamp);
                    break;
                }

                case MSG_QUIT: {
                    Looper.myLooper().quit();
                    break;
                }

                default:
                    throw new RuntimeException("Unhandled msg what=" + what);
            }
        }
    }

    /**
     * 开始录制
     * @param params
     */
    private void onStartRecord(@NonNull VideoParams params) {
        if (VERBOSE) {
            Log.d(TAG, "onStartRecord " + params);
        }
        mVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
        try {
            mVideoEncoder = new VideoEncoder(params, this);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        // 创建EGL上下文和Surface
        mEglCore = new EglCore(params.getEglContext(), EglCore.FLAG_RECORDABLE);
        mInputWindowSurface = new WindowSurface(mEglCore, mVideoEncoder.getInputSurface(), true);
        mInputWindowSurface.makeCurrent();
        // 创建录制用的滤镜
        mImageFilter = new GLImageFilter(null);
        mImageFilter.onInputSizeChanged(params.getVideoWidth(), params.getVideoHeight());
        mImageFilter.onDisplaySizeChanged(params.getVideoWidth(), params.getVideoHeight());
        // 录制开始回调
        if (mRecordListener != null) {
            mRecordListener.onRecordStart(MediaType.VIDEO);
        }
    }

    /**
     * 停止录制
     */
    private void onStopRecord() {
        if (mVideoEncoder == null) {
            return;
        }
        if (VERBOSE) {
            Log.d(TAG, "onStopRecord");
        }
        mVideoEncoder.drainEncoder(true);
        mVideoEncoder.release();
        if (mImageFilter != null) {
            mImageFilter.release();
            mImageFilter = null;
        }
        if (mInputWindowSurface != null) {
            mInputWindowSurface.release();
            mInputWindowSurface = null;
        }
        if (mEglCore != null) {
            mEglCore.release();
            mEglCore = null;
        }

        // 录制完成回调
        if (mRecordListener != null) {
            mRecordListener.onRecordFinish(new RecordInfo(mVideoEncoder.getVideoParams().getVideoPath(),
                    mVideoEncoder.getDuration(), MediaType.VIDEO));
        }
        mVideoEncoder = null;
    }

    /**
     * 录制帧可用
     * @param texture
     * @param timestampNanos
     */
    private void onRecordFrameAvailable(int texture, long timestampNanos) {
        if (VERBOSE) {
            Log.d(TAG, "onRecordFrameAvailable");
        }
        if (mVideoEncoder == null) {
            return;
        }
        mInputWindowSurface.makeCurrent();
        mImageFilter.drawFrame(texture, mVertexBuffer, mTextureBuffer);
        mInputWindowSurface.setPresentationTime(timestampNanos);
        mInputWindowSurface.swapBuffers();
        mVideoEncoder.drainEncoder(false);
    }

}
