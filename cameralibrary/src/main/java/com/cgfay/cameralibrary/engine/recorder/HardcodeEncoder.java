package com.cgfay.cameralibrary.engine.recorder;

import android.content.Context;
import android.opengl.EGLContext;
import android.opengl.GLES30;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.cgfay.filterlibrary.gles.EglCore;
import com.cgfay.filterlibrary.gles.WindowSurface;
import com.cgfay.filterlibrary.glfilter.base.GLImageFilter;
import com.cgfay.filterlibrary.glfilter.utils.OpenGLUtils;
import com.cgfay.filterlibrary.glfilter.utils.TextureRotationUtils;
import com.cgfay.filterlibrary.multimedia.MediaAudioEncoder;
import com.cgfay.filterlibrary.multimedia.MediaEncoder;
import com.cgfay.filterlibrary.multimedia.MediaMuxerWrapper;
import com.cgfay.filterlibrary.multimedia.MediaVideoEncoder;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.FloatBuffer;

/**
 * 硬编码录制器
 * Created by cain.huang on 2017/12/7.
 */

public final class HardcodeEncoder {

    private static final String TAG = "HardcodeEncoder";
    private static final boolean VERBOSE = false;

    private final Object mReadyFence = new Object();

    // 初始化录制器
    static final int MSG_INIT_RECORDER = 0;
    // 帧可用
    static final int MSG_FRAME_AVAILABLE = 1;
    // 渲染帧
    static final int MSG_DRAW_FRAME = 2;
    // 停止录制
    static final int MSG_STOP_RECORDING = 3;
    // 暂停录制
    static final int MSG_PAUSE_RECORDING = 4;
    // 继续录制
    static final int MSG_CONTINUE_RECORDING = 5;
    // 是否允许录制
    static final int MSG_ENABLE_AUDIO = 6;
    // 退出
    static final int MSG_QUIT = 7;
    // 设置渲染纹理尺寸
    static final int MSG_SET_TEXTURE_SIZE = 8;

    // 输出路径
    private String mOutputPath;

    // 录制线程
    private RecordThread mRecordThread;

    private static class HardcodeEncoderHolder {
        public static HardcodeEncoder instance = new HardcodeEncoder();
    }

    private HardcodeEncoder() {}

    public static HardcodeEncoder getInstance() {
        return HardcodeEncoderHolder.instance;
    }

    /**
     * 准备录制器
     */
    public HardcodeEncoder preparedRecorder() {
        synchronized (mReadyFence) {
            if (mRecordThread == null) {
                mRecordThread = new RecordThread(this);
                mRecordThread.start();
                mRecordThread.waitUntilReady();
            }
        }
        return this;
    }

    /**
     * 销毁录制器
     */
    public void destroyRecorder() {
        synchronized (mReadyFence) {
            if (mRecordThread != null) {
                Handler handler = mRecordThread.getHandler();
                if (handler != null) {
                    handler.sendMessage(handler.obtainMessage(MSG_QUIT));
                }
                mRecordThread = null;
            }
        }
    }

    /**
     * 初始化录制器，此时耗时大约200ms左右，不能放在跟渲染线程同一个Looper里面
     * @param width
     * @param height
     * @param listener
     */
    public void initRecorder(int width, int height, MediaEncoder.MediaEncoderListener listener) {
        Handler handler = mRecordThread.getHandler();
        if (handler != null) {
            handler.sendMessage(handler.obtainMessage(MSG_INIT_RECORDER, width, height, listener));
        }
    }

    /**
     * 设置渲染Texture的宽高
     * @param width
     * @param height
     */
    public void setTextureSize(int width, int height) {
        Handler handler = mRecordThread.getHandler();
        if (handler != null) {
            handler.sendMessage(handler.obtainMessage(MSG_SET_TEXTURE_SIZE, width, height));
        }
    }

    /**
     * 开始录制
     * @param sharedContext EGLContext上下文包装类
     */
    public void startRecording(final Context context, final EGLContext sharedContext) {
        Handler handler = mRecordThread.getHandler();
        if (handler != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mRecordThread.startRecording(context, (EGLContext) sharedContext);
                }
            });
        }
    }


    /**
     * 帧可用
     */
    public void frameAvailable() {
        Handler handler = mRecordThread.getHandler();
        if (handler != null) {
            handler.sendMessage(handler.obtainMessage(MSG_FRAME_AVAILABLE));
        }
    }

    /**
     * 发送渲染指令
     * @param texture 当前Texture
     * @param timeStamp 时间戳
     */
    public void drawRecorderFrame(int texture, long timeStamp) {
        Handler handler = mRecordThread.getHandler();
        if (handler != null) {
            handler.sendMessage(handler
                    .obtainMessage(MSG_DRAW_FRAME, texture, 0 /* unused */, timeStamp));
        }
    }

    /**
     * 停止录制
     */
    public void stopRecording() {
        if (mRecordThread == null) {
            return;
        }
        Handler handler = mRecordThread.getHandler();
        if (handler != null) {
            handler.sendMessage(handler.obtainMessage(MSG_STOP_RECORDING));
        }
    }


    /**
     * 暂停录制
     */
    public void pauseRecord() {
        Handler handler = mRecordThread.getHandler();
        if (handler != null) {
            handler.sendMessage(handler.obtainMessage(MSG_PAUSE_RECORDING));
        }
    }

    /**
     * 继续录制
     */
    public void continueRecord() {
        Handler handler = mRecordThread.getHandler();
        if (handler != null) {
            handler.sendMessage(handler.obtainMessage(MSG_CONTINUE_RECORDING));
        }
    }

    /**
     * 是否允许录音
     * @param enable
     */
    public HardcodeEncoder enableAudioRecord(boolean enable) {
        Handler handler = mRecordThread.getHandler();
        if (handler != null) {
            handler.sendMessage(handler.obtainMessage(MSG_ENABLE_AUDIO, enable));
        }
        return this;
    }

    /**
     * 设置输出路径
     * @param path
     */
    public HardcodeEncoder setOutputPath(String path) {
        mOutputPath = path;
        return this;
    }

    /**
     * 获取输出路径
     * @return
     */
    public String getOutputPath() {
        return mOutputPath;
    }

    /**
     * 录制线程
     */
    private static class RecordThread extends Thread {

        private final Object mReadyFence = new Object();
        private boolean mReady;

        // 输入纹理大小
        private int mTextureWidth, mTextureHeight;
        // 录制视频大小
        private int mVideoWidth, mVideoHeight;

        private EglCore mEglCore;
        // 录制视频用的EGLSurface
        private WindowSurface mRecordWindowSurface;
        // 录制的Filter
        private GLImageFilter mRecordFilter;
        private FloatBuffer mVertexBuffer;
        private FloatBuffer mTextureBuffer;
        // 复用器管理器
        private MediaMuxerWrapper mMuxerManager;

        // 是否允许录音
        private boolean enableAudio = true;

        // 是否处于录制状态
        private boolean isRecording = false;

        // MediaCodec 初始化和释放所需要的时间总和
        // 根据试验的结果，大部分手机在初始化和释放阶段的时间总和都要300ms ~ 800ms左右
        // 第一次初始化普遍时间比较长，新出的红米5(2G内存)在第一次初始化1624ms，后面则是600ms左右
        private long mProcessTime = 0;

        // 录制线程Handler回调
        private RecordHandler mHandler;

        private WeakReference<HardcodeEncoder> mWeakRecorder;

        RecordThread(HardcodeEncoder manager) {
            mWeakRecorder = new WeakReference<>(manager);
        }

        @Override
        public void run() {
            Looper.prepare();
            synchronized (mReadyFence) {
                mVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
                mTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
                mHandler = new RecordHandler(this);
                mReady = true;
                mReadyFence.notify();
            }
            Looper.loop();
            if (VERBOSE) {
                Log.d(TAG, "Record thread exiting");
            }

            synchronized (mReadyFence) {
                release();
                mReady = false;
                mHandler = null;
            }
        }

        /**
         * 等待线程结束
         */
        void waitUntilReady() {
            synchronized (mReadyFence) {
                while (!mReady) {
                    try {
                        mReadyFence.wait();
                    } catch (InterruptedException ie) {

                    }
                }
            }
        }

        /**
         * 初始化录制器
         * @param width
         * @param height
         * @param listener
         */
        void initRecorder(int width, int height, MediaEncoder.MediaEncoderListener listener) {
            if (VERBOSE) {
                Log.d(TAG, "init recorder");
            }

            synchronized (mReadyFence) {
                long time = System.currentTimeMillis();
                mVideoWidth = width;
                mVideoHeight = height;

                String filePath = mWeakRecorder.get().getOutputPath();

                // 如果路径为空，则生成默认的路径
                if (TextUtils.isEmpty(filePath)) {
                    throw new IllegalArgumentException("filePath Must no be empty");
                }
                File file = new File(filePath);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                try {
                    mMuxerManager = new MediaMuxerWrapper(file.getAbsolutePath());
                    new MediaVideoEncoder(mMuxerManager, listener, mVideoWidth, mVideoHeight);
                    if (enableAudio) {
                        new MediaAudioEncoder(mMuxerManager, listener);
                    }

                    mMuxerManager.prepare();
                } catch (IOException e) {
                    Log.e(TAG, "startRecording:", e);
                }
                mProcessTime += (System.currentTimeMillis() - time);
            }
        }

        /**
         * 设置渲染Texture的宽高
         * @param width
         * @param height
         */
        void setTextureSize(int width, int height) {
            if (VERBOSE) {
                Log.d(TAG, "setTextureSize");
            }
            synchronized (mReadyFence) {
                mTextureWidth = width;
                mTextureHeight = height;
            }
        }

        /**
         * 开始录制
         * @param eglContext EGLContext上下文包装类
         */
        void startRecording(Context context, EGLContext eglContext) {
            if (VERBOSE) {
                Log.d(TAG, " start recording");
            }
            synchronized (mReadyFence) {
                if (mMuxerManager.getVideoEncoder() == null) {
                    return;
                }
                // 释放之前的Egl
                if (mRecordWindowSurface != null) {
                    mRecordWindowSurface.releaseEglSurface();
                }
                if (mEglCore != null) {
                    mEglCore.release();
                }
                // 重新创建一个EglContext 和 Window Surface
                mEglCore = new EglCore(eglContext, EglCore.FLAG_RECORDABLE);
                if (mRecordWindowSurface != null) {
                    mRecordWindowSurface.recreate(mEglCore);
                } else {
                    mRecordWindowSurface = new WindowSurface(mEglCore,
                            ((MediaVideoEncoder) mMuxerManager.getVideoEncoder()).getInputSurface(),
                            true);
                }
                mRecordWindowSurface.makeCurrent();
                initRecordingFilter(context);
                if (mMuxerManager != null) {
                    mMuxerManager.startRecording();
                }
                isRecording = true;
            }
        }


        /**
         * 帧可用
         */
        void frameAvailable() {
            if (VERBOSE) {
                Log.d(TAG, "frame available");
            }
            synchronized (mReadyFence) {
                if (mMuxerManager != null && mMuxerManager.getVideoEncoder() != null && isRecording) {
                    mMuxerManager.getVideoEncoder().frameAvailableSoon();
                }
            }
        }

        /**
         * 发送渲染指令
         * @param currentTexture 当前Texture
         * @param timeStamp 时间戳
         */
        void drawRecordingFrame(int currentTexture, long timeStamp) {
            if (VERBOSE) {
                Log.d(TAG, "draw recording frame");
            }
            synchronized (mReadyFence) {
                if (mRecordWindowSurface != null) {
                    mRecordWindowSurface.makeCurrent();
                    drawRecordingFrame(currentTexture);
                    mRecordWindowSurface.setPresentationTime(timeStamp);
                    mRecordWindowSurface.swapBuffers();
                }
            }
        }

        /**
         * 停止录制
         */
        void stopRecording() {
            if (VERBOSE) {
                Log.d(TAG, "stop recording");
            }
            synchronized (mReadyFence) {
                long time = System.currentTimeMillis();
                isRecording = false;
                if (mMuxerManager != null) {
                    mMuxerManager.stopRecording();
                    mMuxerManager = null;
                }
                if (mRecordWindowSurface != null) {
                    mRecordWindowSurface.release();
                    mRecordWindowSurface = null;
                }
                // 释放资源
                releaseRecordingFilter();

                if (VERBOSE) {
                    mProcessTime += (System.currentTimeMillis() - time);
                    Log.d(TAG, "sum of init and release time: " + mProcessTime + "ms");
                    mProcessTime = 0;
                }
            }
        }


        /**
         * 暂停录制
         */
        void pauseRecording() {
            if (VERBOSE) {
                Log.d(TAG, "pause recording");
            }
            synchronized (mReadyFence) {
                if (mMuxerManager != null && isRecording) {
                    mMuxerManager.pauseRecording();
                }
            }
        }

        /**
         * 继续录制
         */
        void continueRecording() {
            if (VERBOSE) {
                Log.d(TAG, "continue recording");
            }
            synchronized (mReadyFence) {
                if (mMuxerManager != null && isRecording) {
                    mMuxerManager.continueRecording();
                }
            }
        }

        /**
         * 初始化录制的Filter
         * TODO 录制视频大小跟渲染大小、显示大小拆分成不同的大小
         */
        private void initRecordingFilter(Context context) {
            if (mRecordFilter == null) {
                mRecordFilter = new GLImageFilter(context);
            }
            mRecordFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
            mRecordFilter.onDisplaySizeChanged(mVideoWidth, mVideoHeight);
        }

        /**
         * 渲染录制的帧
         */
        private void drawRecordingFrame(int textureId) {
            if (mRecordFilter != null) {
                GLES30.glViewport(0, 0, mVideoWidth, mVideoHeight);
                mRecordFilter.drawFrame(textureId, mVertexBuffer, mTextureBuffer);
            }
        }

        /**
         * 释放录制的Filter资源
         */
        private void releaseRecordingFilter() {
            if (mRecordFilter != null) {
                mRecordFilter.release();
                mRecordFilter = null;
            }
        }

        /**
         * 销毁资源
         */
        public void release() {
            // 停止录制
            stopRecording();
            if (mEglCore != null) {
                mEglCore.release();
                mEglCore = null;
            }
            if (mRecordWindowSurface != null) {
                mRecordWindowSurface.release();
                mRecordWindowSurface = null;
            }
            if (mVertexBuffer != null) {
                mVertexBuffer.clear();
                mVertexBuffer = null;
            }
            if (mTextureBuffer != null) {
                mTextureBuffer.clear();
                mTextureBuffer = null;
            }
        }

        /**
         * 是否允许录音
         * @param enable
         */
        void enableAudioRecording(boolean enable) {
            if (VERBOSE) {
                Log.d(TAG, "enable audio recording ? " + enable);
            }
            synchronized (mReadyFence) {
                enableAudio = enable;
            }
        }

        /**
         * 获取Handler
         */
        public RecordHandler getHandler() {
            return mHandler;
        }
    }

    /**
     * 录制线程Handler回调
     */
    private static class RecordHandler extends Handler {

        private WeakReference<RecordThread> mWeakRecordThread;

        public RecordHandler(RecordThread thread) {
            mWeakRecordThread = new WeakReference<RecordThread>(thread);
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            RecordThread thread = mWeakRecordThread.get();
            if (thread == null) {
                Log.w(TAG, "RecordHandler.handleMessage: encoder is null");
                return;
            }

            switch (what) {
                // 初始化录制器
                case MSG_INIT_RECORDER:
                    thread.initRecorder(msg.arg1, msg.arg2,
                            (MediaEncoder.MediaEncoderListener) msg.obj);
                    break;

                // 帧可用
                case MSG_FRAME_AVAILABLE:
                    thread.frameAvailable();
                    break;

                // 渲染帧
                case MSG_DRAW_FRAME:
                    thread.drawRecordingFrame(msg.arg1, (Long) msg.obj);
                    break;

                // 停止录制
                case MSG_STOP_RECORDING:
                    thread.stopRecording();
                    break;

                // 暂停录制
                case MSG_PAUSE_RECORDING:
                    thread.pauseRecording();
                    break;

                // 继续录制
                case MSG_CONTINUE_RECORDING:
                    thread.continueRecording();
                    break;

                // 是否允许录音
                case MSG_ENABLE_AUDIO:
                    thread.enableAudioRecording((Boolean) msg.obj);
                    break;

                // 退出线程
                case MSG_QUIT:
                    removeCallbacksAndMessages(null);
                    Looper.myLooper().quit();
                    break;

                // 设置渲染Texture的宽高
                case MSG_SET_TEXTURE_SIZE:
                    thread.setTextureSize(msg.arg1, msg.arg2);
                    break;

                default:
                    throw new RuntimeException("Unhandled msg what = " + what);
            }
        }
    }

}
