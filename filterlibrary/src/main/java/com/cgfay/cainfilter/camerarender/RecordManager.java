package com.cgfay.cainfilter.camerarender;

import android.opengl.EGLContext;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.cgfay.cainfilter.multimedia.EncoderManager;
import com.cgfay.cainfilter.multimedia.MediaEncoder;

import java.lang.ref.WeakReference;

/**
 * 录制管理器
 * Created by cain.huang on 2017/12/7.
 */

public final class RecordManager {

    private static final String TAG = "RecordManager";
    private static final boolean VERBOSE = false;

    public static final int RECORD_WIDTH = 540;
    public static final int RECORD_HEIGHT = 960;

    private static RecordManager mInstance;

    // 初始化录制器
    static final int MSG_INIT_RECORDER = 0;
    // 开始录制
    static final int MSG_START_RECORDING = 1;
    // 帧可用
    static final int MSG_FRAME_AVAILABLE = 2;
    // 渲染帧
    static final int MSG_DRAW_FRAME = 3;
    // 停止录制
    static final int MSG_STOP_RECORDING = 4;
    // 暂停录制
    static final int MSG_PAUSE_RECORDING = 5;
    // 继续录制
    static final int MSG_CONTINUE_RECORDING = 6;
    // 设置帧率
    static final int MSG_FRAME_RATE = 7;
    // 是否允许录制高清视频
    static final int MSG_HIGHTDEFINITION = 8;
    // 是否允许录制
    static final int MSG_ENABLE_AUDIO = 9;
    // 退出
    static final int MSG_QUIT = 10;
    // 设置渲染Texture的宽高
    static final int MSG_SET_TEXTURE_SIZE = 11;
    // 设置预览大小
    static final int MSG_SET_DISPLAY_SIZE = 12;

    // 录制线程
    private RecordThread mRecordThread;

    private String mOutputPath;

    public static RecordManager getInstance() {
        if (mInstance == null) {
            mInstance = new RecordManager();
        }
        return mInstance;
    }

    private RecordManager() {}

    /**
     * 初始化录制线程
     */
    synchronized public void initThread() {
        if (mRecordThread == null) {
            mRecordThread = new RecordThread();
            mRecordThread.start();
            mRecordThread.waitUntilReady();
        }
    }

    /**
     * 销毁线程
     */
    synchronized public void destoryThread() {
        if (mRecordThread != null) {
            Handler handler = mRecordThread.getHandler();
            if (handler != null) {
                handler.sendMessage(handler.obtainMessage(MSG_QUIT));
            }
            mRecordThread = null;
        }
    }

    /**
     * 初始化录制器，此时耗时大约200ms左右，不能放在跟渲染线程同一个Looper里面
     * @param width
     * @param height
     */
    public void initRecorder(int width, int height) {
        initRecorder(width, height, null);
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
     * 设置预览大小
     * @param width
     * @param height
     */
    public void setDisplaySize(int width, int height) {
        Handler handler = mRecordThread.getHandler();
        if (handler != null) {
            handler.sendMessage(handler.obtainMessage(MSG_SET_DISPLAY_SIZE, width, height));
        }
    }

    /**
     * 开始录制
     * @param sharedContext EGLContext上下文包装类
     */
    public void startRecording(EGLContext sharedContext) {
        Handler handler = mRecordThread.getHandler();
        if (handler != null) {
            handler.sendMessage(handler.obtainMessage(MSG_START_RECORDING, sharedContext));
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
    synchronized public void drawRecorderFrame(int texture, long timeStamp) {
        Handler handler = mRecordThread.getHandler();
        if (handler != null) {
            handler.sendMessage(handler
                    .obtainMessage(MSG_DRAW_FRAME, texture, 0 /* unused */, timeStamp));
        }
    }

    /**
     * 停止录制
     */
    synchronized public void stopRecording() {
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
    public void pauseRecording() {
        Handler handler = mRecordThread.getHandler();
        if (handler != null) {
            handler.sendMessage(handler.obtainMessage(MSG_PAUSE_RECORDING));
        }
    }

    /**
     * 继续录制
     */
    public void continueRecording() {
        Handler handler = mRecordThread.getHandler();
        if (handler != null) {
            handler.sendMessage(handler.obtainMessage(MSG_CONTINUE_RECORDING));
        }
    }


    /**
     * 设置帧率
     * @param frameRate
     */
    public void setFrameRate(int frameRate) {
        Handler handler = mRecordThread.getHandler();
        if (handler != null) {
            handler.sendMessage(handler.obtainMessage(MSG_FRAME_RATE, frameRate));
        }
    }


    /**
     * 是否允许录制高清视频
     * @param enable
     */
    public void enableHighDefinition(boolean enable) {
        Handler handler = mRecordThread.getHandler();
        if (handler != null) {
            handler.sendMessage(handler.obtainMessage(MSG_HIGHTDEFINITION, enable));
        }
    }

    /**
     * 是否允许录音
     * @param enable
     */
    public void setEnableAudioRecording(boolean enable) {
        Handler handler = mRecordThread.getHandler();
        if (handler != null) {
            handler.sendMessage(handler.obtainMessage(MSG_ENABLE_AUDIO, enable));
        }
    }

    /**
     * 获取输出路径
     * @return
     */
    public String getOutputPath() {
        return mOutputPath;
    }

    /**
     * 设置输出路径
     * @param path
     */
    public void setOutputPath(String path) {
        mOutputPath = path;
        EncoderManager.getInstance().setOutputPath(path);
    }

    /**
     * 录制线程
     */
    private static class RecordThread extends Thread {

        // 录制线程Handler回调
        private RecordHandler mHandler;

        private Object mReadyFence = new Object();
        private boolean mReady;


        @Override
        public void run() {
            Looper.prepare();
            synchronized (mReadyFence) {
                mHandler = new RecordHandler(this);
                mReady = true;
                mReadyFence.notify();
            }
            Looper.loop();
            if (VERBOSE) {
                Log.d(TAG, "Record thread exiting");
            }

            synchronized (mReadyFence) {
                // 释放资源
                EncoderManager.getInstance().release();
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
                EncoderManager.getInstance().initRecorder(width, height, listener);
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
                EncoderManager.getInstance().setTextureSize(width, height);
            }
        }

        /**
         * 设置预览大小
         * @param width
         * @param height
         */
        void setDisplaySize(int width, int height) {
            if (VERBOSE) {
                Log.d(TAG, "setDisplaySize");
            }
            synchronized (mReadyFence) {
                EncoderManager.getInstance().setDisplaySize(width, height);
            }
        }

        /**
         * 开始录制
         * @param eglContext EGLContext上下文包装类
         */
        void startRecording(EGLContext eglContext) {
            if (VERBOSE) {
                Log.d(TAG, " start recording");
            }
            synchronized (mReadyFence) {
                EncoderManager.getInstance().startRecording(eglContext);
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
                EncoderManager.getInstance().frameAvailable();
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
                EncoderManager.getInstance().drawRecorderFrame(currentTexture, timeStamp);
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
                EncoderManager.getInstance().stopRecording();
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
                EncoderManager.getInstance().pauseRecording();
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
                EncoderManager.getInstance().continueRecording();
            }
        }


        /**
         * 设置帧率
         * @param frameRate
         */
        void setFrameRate(int frameRate) {
            if (VERBOSE) {
                Log.d(TAG, "set frame rate: " + frameRate);
            }
            synchronized (mReadyFence) {
                EncoderManager.getInstance().setFrameRate(frameRate);
            }
        }


        /**
         * 是否允许录制高清视频
         * @param enable
         */
        void enableHighDefinition(boolean enable) {
            if (VERBOSE) {
                Log.d(TAG, "enable highDefinition ? " + enable);
            }

            synchronized (mReadyFence) {
                EncoderManager.getInstance().enableHighDefinition(enable);
            }
        }

        /**
         * 是否允许录音
         * @param enable
         */
        void setEnableAudioRecording(boolean enable) {
            if (VERBOSE) {
                Log.d(TAG, "enable audio recording ? " + enable);
            }
            synchronized (mReadyFence) {
                EncoderManager.getInstance().setEnableAudioRecording(enable);
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

                // 开始录制
                case MSG_START_RECORDING:
                    thread.startRecording((EGLContext) msg.obj);
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

                // 设置帧率
                case MSG_FRAME_RATE:
                    thread.setFrameRate((Integer) msg.obj);
                    break;

                // 是否允许高清录制
                case MSG_HIGHTDEFINITION:
                    thread.enableHighDefinition((Boolean) msg.obj);
                    break;

                // 是否允许录音
                case MSG_ENABLE_AUDIO:
                    thread.setEnableAudioRecording((Boolean) msg.obj);
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

                // 设置预览的大小
                case MSG_SET_DISPLAY_SIZE:
                    thread.setDisplaySize(msg.arg1, msg.arg2);
                    break;

                default:
                    throw new RuntimeException("Unhandled msg what = " + what);
            }
        }
    }

}
