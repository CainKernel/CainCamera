package com.cgfay.media.recorder;

import android.util.Log;

import com.cgfay.cavfoundation.AVMediaType;

/**
 * 媒体录制器，支持倍速录制
 * @author CainHuang
 * @date 2019/6/30
 */
public class JAVMediaRecorder implements OnRecordListener {

    private static final String TAG = "JAVMediaRecorder";
    private static final boolean VERBOSE = false;

    public static final int SECOND_IN_US = 1000000;

    // 音频录制器
    private final JAVAudioRecorder mAudioRecorder;
    // 视频录制器
    private final JAVVideoRecorder mVideoRecorder;
    // 是否支持音频录制
    private boolean mAudioEnable = true;
    // 打开的录制器个数
    private int mRecorderCount;

    // 处理时长
    private long mProcessTime = 0;

    // 录制状态回调
    private OnRecordStateListener mRecordStateListener;

    public JAVMediaRecorder(OnRecordStateListener listener) {
        mRecordStateListener = listener;
        mVideoRecorder = new JAVVideoRecorder();
        mVideoRecorder.setOnRecordListener(this);

        mAudioRecorder = new JAVAudioRecorder();
        mAudioRecorder.setOnRecordListener(this);
        mRecorderCount = 0;
    }

    /**
     * 释放资源
     */
    public void release() {
        mVideoRecorder.release();
        mAudioRecorder.release();
    }

    /**
     * 设置是否允许音频录制
     * @param enable
     */
    public void setEnableAudio(boolean enable) {
        mAudioEnable = enable;
    }

    /**
     * 是否允许录制音频
     * @return
     */
    public boolean enableAudio() {
        return mAudioEnable;
    }

    /**
     * 开始录制
     *
     */
    public void startRecord(VideoParams videoParams, AudioParams audioParams) {
        if (VERBOSE) {
            Log.d(TAG, " start record");
        }

        mVideoRecorder.startRecord(videoParams);

        if (mAudioEnable) {
            try {
                mAudioRecorder.prepare(audioParams);
                mAudioRecorder.startRecord();
            } catch (Exception e) {
                Log.e(TAG, "startRecord: " + e.getMessage());
            }
        }
    }

    /**
     * 停止录制
     */
    public void stopRecord() {
        if (VERBOSE) {
            Log.d(TAG, "stop recording");
        }
        long time = System.currentTimeMillis();
        if (mVideoRecorder != null) {
            mVideoRecorder.stopRecord();
        }
        if (mAudioEnable) {
            if (mAudioRecorder != null) {
                mAudioRecorder.stopRecord();
            }
        }
        if (VERBOSE) {
            mProcessTime += (System.currentTimeMillis() - time);
            Log.d(TAG, "sum of init and release time: " + mProcessTime + "ms");
            mProcessTime = 0;
        }
    }

    /**
     * 录制帧可用
     */
    public void frameAvailable(int texture, long timestamp) {
        if (mVideoRecorder != null) {
            mVideoRecorder.frameAvailable(texture, timestamp);
        }
    }

    /**
     * 判断是否正在录制阶段
     * @return
     */
    public boolean isRecording() {
        if (mVideoRecorder != null) {
            return mVideoRecorder.isRecording();
        }
        return false;
    }

    /**
     * 录制开始
     * @param type
     */
    @Override
    public void onRecordStart(AVMediaType type) {
        mRecorderCount++;
        // 允许音频录制，则判断录制器打开的个数大于等于两个，则表示全部都打开了
        if (!mAudioEnable || (mRecorderCount >= 2)) {
            if (mRecordStateListener != null) {
                mRecordStateListener.onRecordStart();
                mRecorderCount = 0;
            }
        }
    }

    /**
     * 正在录制
     * @param type
     * @param duration
     */
    @Override
    public void onRecording(AVMediaType type, long duration) {
        if (type == AVMediaType.AVMediaTypeVideo) {
            if (mRecordStateListener != null) {
                mRecordStateListener.onRecording(duration);
            }
        }
    }

    /**
     * 录制完成
     * @param info
     */
    @Override
    public void onRecordFinish(RecordInfo info) {
        if (mRecordStateListener != null) {
            mRecordStateListener.onRecordFinish(info);
        }
    }
}
