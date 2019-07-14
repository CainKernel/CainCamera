package com.cgfay.filter.recorder;

import android.util.Log;

import com.cgfay.filter.ndkfilter.ImageFilter;
import com.cgfay.uitls.utils.FileUtils;

import java.io.IOException;

/**
 * 媒体录制器，支持倍速录制
 * @author CainHuang
 * @date 2019/6/30
 */
public class MediaRecorder implements OnRecordListener {

    private static final String TAG = "MediaRecorder";
    private static final boolean VERBOSE = false;

    public static final int SECOND_IN_US = 1000000;

    // 音频录制器
    private final AudioRecorder mAudioRecorder;
    // 视频录制器
    private final VideoRecorder mVideoRecorder;
    // 是否支持音频录制
    private boolean mAudioEnable = true;
    // 打开的录制器个数
    private int mRecorderCount;

    // 处理时长
    private long mProcessTime = 0;

    // 录制状态回调
    private OnRecordStateListener mRecordStateListener;

    public MediaRecorder(OnRecordStateListener listener) {
        mRecordStateListener = listener;
        mVideoRecorder = new VideoRecorder();
        mVideoRecorder.setOnRecordListner(this);

        mAudioRecorder = new AudioRecorder();
        mAudioRecorder.setOnRecordListner(this);
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
            } catch (IOException e) {
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
    public void onRecordStart(MediaType type) {
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
    public void onRecording(MediaType type, long duration) {
        if (type == MediaType.VIDEO) {
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
