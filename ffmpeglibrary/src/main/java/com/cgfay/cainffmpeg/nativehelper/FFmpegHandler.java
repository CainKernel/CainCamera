package com.cgfay.cainffmpeg.nativehelper;

/**
 * FFmpeg控制
 * Created by cain.huang on 2018/1/3.
 */

public final class FFmpegHandler {

    // 录制状态
    private static final int RECORDING_FAILED = -1;         // 失败
    private static final int RECORDING_PREPARED = 1;        // 已经准备好
    private static final int RECORDING_STARTED = 2;         // 已经开始
    private static final int RECORDING_STOPPED = 3;         // 已经停止
    private static final int RECORDING_RELEASED = 4;        // 已经释放

    private FFmpegHandler(){}

    private static RecordStateListener mRecordStateListener;

    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("encoder");
    }

    /**
     * 初始化录制器
     * @param videoPath
     * @param previewWidth
     * @param previewHeight
     * @param videoWidth
     * @param videoHeight
     * @param frameRate
     * @param bitRate
     * @param enableAudio
     * @param audioBitRate
     * @param audioSampleRate
     * @return
     */
    public static native int initMediaRecorder(String videoPath, int previewWidth, int previewHeight,
                                               int videoWidth, int videoHeight, int frameRate,
                                               int bitRate, boolean enableAudio,
                                               int audioBitRate, int audioSampleRate);

    /**
     * 开始录制
     */
    public static native void startRecord();

    /**
     * 发送需要编码的yuv数据
     * @param data
     * @return
     */
    public static native int encodeYUVFrame(byte[] data);

    /**
     * 发送需要编码的PCM数据
     * @param data
     * @return
     */
    public static native int encodePCMFrame(byte[] data, int len);

    /**
     * 发送停止命令
     */
    public static native void stopRecord();


    /**
     * 释放资源
     */
    public static native void nativeRelease();

    /**
     * native层通知录制停止
     * @param what
     */
    public static synchronized void notifyRecord(int what) {
        switch (what) {
            // 准备好了
            case RECORDING_PREPARED:
                if (mRecordStateListener != null) {
                    mRecordStateListener.onRecordPrepared();
                }
                break;

            // 录制已经开始
            case RECORDING_STARTED:
                if (mRecordStateListener != null) {
                    mRecordStateListener.onRecordStarted();
                }
                break;

            // 录制完全停止
            case RECORDING_STOPPED:
                // 释放资源
                nativeRelease();
                if (mRecordStateListener != null) {
                    mRecordStateListener.onRecordStopped();
                }
                break;

            // 录制器完全释放
            case RECORDING_RELEASED:
                if (mRecordStateListener != null) {
                    mRecordStateListener.onRecordReleased();
                }
                break;

            // 录制出错
            case RECORDING_FAILED:
                if (mRecordStateListener != null) {
                    mRecordStateListener.onRecordError();
                }
                break;


            default:
                    throw new IllegalStateException("uncaught notify state: " + what);
        }
    }

    /**
     * 录制监听接口
     */
    public interface RecordStateListener {
        void onRecordError();
        void onRecordPrepared();
        void onRecordStarted();
        void onRecordStopped();
        void onRecordReleased();
    }

    /**
     * 添加录制回调监听
     * @param listener
     */
    public static void addRecordStateListener(RecordStateListener listener) {
        mRecordStateListener = listener;
    }
}
