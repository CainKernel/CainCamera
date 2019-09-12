package com.cgfay.camera.engine.recorder;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.cgfay.camera.engine.listener.OnRecordListener;
import com.cgfay.camera.engine.render.PreviewRenderer;
import com.cgfay.filter.multimedia.MediaEncoder;
import com.cgfay.filter.multimedia.VideoCombiner;
import com.cgfay.uitls.utils.FileUtils;
import com.cgfay.uitls.utils.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 预览录制器
 */
public final class PreviewRecorder {
    // 十秒时长
    public static final int DURATION_TEN_SECOND = 10 * 1000;
    // 三分钟时长
    public static final int DURATION_THREE_MINUTE = 180 * 1000;

    // 录制类型，默认是录制视频
    private RecordType mRecordType;
    // 输出路径
    private String mOutputPath;
    // 录制视频宽度
    private int mRecordWidth;
    // 录制视频高度
    private int mRecordHeight;
    // 是否允许录音
    private boolean mRecordAudio;
    // 是否处于录制状态
    private boolean isRecording;
    // MediaEncoder准备好的数量
    private int mPreparedCount = 0;
    // 开始MediaEncoder的数量
    private int mStartedCount = 0;
    // 释放MediaEncoder的数量
    private int mReleaseCount = 0;

    // 精确倒计时
    private RecordTimer mRecordTimer;
    // 倒计时数值
    private long mMaxMillisSeconds = DURATION_TEN_SECOND;
    // 50毫秒读取一次
    private long mCountDownInterval = 50;

    // 当前走过的时长，有可能跟视频的长度不一致
    // 在最后一秒内点击录制，倒计时走完，但录制的视频立即停止
    // 这样的话，最后一段显示的视频长度跟当前走过的时长并不相等
    private long mCurrentDuration = 0;
    // 记录最后一秒点击时剩余的时长
    private long mLastSecondLeftTime = 0;
    // 记录是否最后点击停止
    private boolean mLastSecondStop = false;
    // 是否需要处理最后一秒的情况
    private boolean mProcessLasSecond = true;
    // 计时器是否停止
    private boolean mTimerFinish = false;

    // 分段视频列表
    private LinkedList<RecordItem> mVideoList = new LinkedList<RecordItem>();

    // 录制监听器
    private OnRecordListener mRecordListener;

    private static class RecordEngineHolder {
        public static PreviewRecorder instance = new PreviewRecorder();
    }

    private PreviewRecorder() {
        reset();
    }

    public static PreviewRecorder getInstance() {
        return RecordEngineHolder.instance;
    }

    /**
     * 重置参数
     */
    public void reset() {
        mRecordType = RecordType.Video;
        mOutputPath = null;
        mRecordWidth = 0;
        mRecordHeight = 0;
        mRecordAudio = false;
        isRecording = false;
        mPreparedCount = 0;
        mStartedCount = 0;
        mReleaseCount = 0;
    }

    /**
     * 设置录制类型
     * @param type
     * @return
     */
    public PreviewRecorder setRecordType(RecordType type) {
        mRecordType = type;
        return this;
    }

    /**
     * 设置输出路径
     * @param path
     * @return
     */
    public PreviewRecorder setOutputPath(String path) {
        mOutputPath = path;
        return this;
    }

    /**
     * 设置录制大小
     * @param width
     * @param height
     * @return
     */
    public PreviewRecorder setRecordSize(int width, int height) {
        mRecordWidth = width;
        mRecordHeight = height;
        return this;
    }

    /**
     * 是否允许录音
     * @param enable
     * @return
     */
    public PreviewRecorder enableAudio(boolean enable) {
        mRecordAudio = enable;
        return this;
    }

    /**
     *  开始录制
     */
    public void startRecord() {
        if (mRecordWidth <= 0 || mRecordHeight <= 0) {
            throw new IllegalArgumentException("Video's width or height must not be zero");
        }

        if (TextUtils.isEmpty(mOutputPath)) {
            throw new IllegalArgumentException("Video output path must not be empty");
        }

        // 初始化录制器
        HardcodeEncoder.getInstance()
                .setOutputPath(mOutputPath)
                .enableAudioRecord(mRecordAudio)
                .initRecorder(mRecordWidth, mRecordHeight, mEncoderListener);

        // 初始化计时器
        initTimer();
    }

    /**
     * 停止录制
     */
    public void stopRecord() {
        stopRecord(true);
    }

    /**
     * 停止录制
     * @param stopTimer 是否停止计时器
     */
    public void stopRecord(boolean stopTimer) {
        // 停止录制
        PreviewRenderer.getInstance().stopRecording();
        // 停止倒计时
        if (stopTimer) {
            stopTimer();
        }
    }

    /**
     * 销毁录制器
     */
    public void destroyRecorder() {
        HardcodeEncoder.getInstance().destroyRecorder();
    }

    /**
     * 删除一段已记录的时长
     */
    public void deleteRecordDuration() {
        resetDuration();
        resetLastSecondStop();
    }

    /**
     * 取消录制
     */
    public void cancelRecord() {
        HardcodeEncoder.getInstance().stopRecord();
        cancelTimerWithoutSaving();
    }

    /**
     * 设置录制监听器
     * @param listener
     * @return
     */
    public PreviewRecorder setOnRecordListener(OnRecordListener listener) {
        mRecordListener = listener;
        return this;
    }

    /**
     * 判断是否正在录制
     * @return
     */
    public boolean isRecording() {
        return isRecording;
    }

    /**
     * 获取录制的总时长
     * @return
     */
    public int getDuration() {
        int duration = 0;
        if (mVideoList != null) {
            for (RecordItem recordItem : mVideoList) {
                duration += recordItem.getDuration();
            }
        }
        return duration;
    }

    /**
     * 添加分段视频
     * @param path      视频路径
     * @param duration  视频时长
     */
    private void addSubVideo(String path, int duration) {
        if (mVideoList == null) {
            mVideoList = new LinkedList<RecordItem>();
        }
        RecordItem recordItem = new RecordItem();
        recordItem.mediaPath = path;
        recordItem.duration = duration;
        mVideoList.add(recordItem);
    }

    /**
     * 移除当前分段视频
     */
    public void removeLastSubVideo() {
        RecordItem recordItem = mVideoList.get(mVideoList.size() - 1);
        mVideoList.remove(recordItem);
        if (recordItem != null) {
            recordItem.delete();
            mVideoList.remove(recordItem);
        }
    }

    /**
     * 删除所有分段视频
     */
    public void removeAllSubVideo() {
        if (mVideoList != null) {
            for (RecordItem part : mVideoList) {
                part.delete();
            }
            mVideoList.clear();
        }
    }

    /**
     * 获取分段视频路径
     * @return
     */
    public List<String> getSubVideoPathList() {
        if (mVideoList == null || mVideoList.isEmpty()) {
            return new ArrayList<String>();
        }
        List<String> mediaPaths = new ArrayList<String>();
        for (int i = 0; i < mVideoList.size(); i++) {
            mediaPaths.add(i, mVideoList.get(i).getMediaPath());
        }
        return mediaPaths;
    }

    /**
     * 获取分段视频数量
     * @return
     */
    public int getNumberOfSubVideo() {
        return mVideoList.size();
    }


    // --------------------------------------- 计时器操作 ------------------------------------------
    /**
     * 初始化倒计时
     */
    private void initTimer() {

        cancelCountDown();

        mRecordTimer = new RecordTimer(mMaxMillisSeconds, mCountDownInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (!mTimerFinish) {
                    // 获取视频总时长
                    int previousDuration = getDuration();
                    // 获取当前分段视频走过的时间
                    mCurrentDuration = mMaxMillisSeconds - millisUntilFinished;
                    // 如果总时长够设定的最大时长，则需要停止计时
                    if (previousDuration + mCurrentDuration >= mMaxMillisSeconds) {
                        mCurrentDuration = mMaxMillisSeconds - previousDuration;
                        mTimerFinish = true;
                    }
                    // 计时回调
                    if (mRecordListener != null) {
                        mRecordListener.onRecordProgressChanged(getVisibleDuration());
                    }
                    // 是否需要结束计时器
                    if (mTimerFinish) {
                        mTimerHandler.sendEmptyMessage(0);
                    }
                }
            }

            @Override
            public void onFinish() {
                mTimerFinish = true;
                if (mRecordListener != null) {
                    mRecordListener.onRecordProgressChanged(getVisibleDuration(true));
                }
            }
        };
    }

    /**
     * 开始倒计时
     */
    private void startTimer() {
        if (mRecordTimer != null) {
            mRecordTimer.start();
        }
    }

    /**
     * 停止倒计时
     */
    private void stopTimer() {
        // 重置最后一秒停止标志
        mLastSecondStop = false;
        // 判断是否需要处理最后一秒的情况
        if (mProcessLasSecond) {
            // 如果在下一次计时器回调之前剩余时间小于1秒，则表示是最后一秒内点击了停止
            if (getAvailableTime() + mCountDownInterval < 1000) {
                mLastSecondStop = true;
                mLastSecondLeftTime = getAvailableTime();
            }
        }
        // 如果不是最后一秒，则立即停止
        if (!mLastSecondStop) {
            cancelCountDown();
        }
    }

    /**
     * 取消倒计时，不保存走过的时长、停止标志、剩余时间等
     */
    private void cancelTimerWithoutSaving() {
        cancelCountDown();
        resetDuration();
        resetLastSecondStop();
        mLastSecondLeftTime = 0;
    }

    /**
     * 取消倒计时
     */
    private void cancelCountDown() {
        if (mRecordTimer != null) {
            mRecordTimer.cancel();
            mRecordTimer = null;
        }
        // 复位结束标志
        mTimerFinish = false;
    }

    /**
     * 重置当前走过的时长
     */
    private void resetDuration() {
        mCurrentDuration = 0;
    }

    /**
     * 重置最后一秒停止标志
     */
    private void resetLastSecondStop() {
        mLastSecondStop = false;
    }

    /**
     * 设置总时长
     * @param type
     */
    public void setMilliSeconds(CountDownType type) {
        if (type == CountDownType.TenSecond) {
            mMaxMillisSeconds = DURATION_TEN_SECOND;
        } else if (type == CountDownType.ThreeMinute) {
            mMaxMillisSeconds = DURATION_THREE_MINUTE;
        }
    }

    /**
     * 获取总时长
     * @return
     */
    public long getMaxMilliSeconds() {
        return mMaxMillisSeconds;
    }

    /**
     * 设置刷新间隔
     * @param interval
     */
    public void setCountDownInterval(long interval) {
        mCountDownInterval = interval;
    }

    /**
     * 获取刷新间隔
     * @return
     */
    public long getCountDownInterval() {
        return mCountDownInterval;
    }

    /**
     * 获取剩余时间
     * @return
     */
    private long getAvailableTime() {
        return mMaxMillisSeconds - getDuration() - mCurrentDuration;
    }

    /**
     * 获取当前实际时长 (跟显示的时长不一定不一样)
     * @return
     */
    private long getRealDuration() {
        // 如果是最后一秒内点击，则计时器走过的时长要比视频录制的时长短一些，需要减去多余的时长
        if (mLastSecondLeftTime > 0) {
            long realTime = mCurrentDuration - mLastSecondLeftTime;
            mLastSecondLeftTime = 0;
            return realTime;
        }
        return mCurrentDuration;
    }

    /**
     * 获取显示的时长
     */
    public long getVisibleDuration() {
        return getVisibleDuration( false);
    }

    /**
     * 获取显示的时长
     * @param finish    是否完成
     * @return
     */
    private long getVisibleDuration(boolean finish) {
        if (finish) {
            return mMaxMillisSeconds;
        } else {
            long time = getDuration() + mCurrentDuration;
            if (time > mMaxMillisSeconds) {
                time = mMaxMillisSeconds;
            }
            return time;
        }
    }

    /**
     * 获取显示的时间文本
     * @return
     */
    public String getVisibleDurationString() {
        return StringUtils.generateMillisTime((int) getVisibleDuration());
    }

    /**
     * 是否最后一秒内停止了
     * @return
     */
    public boolean isLastSecondStop() {
        return mLastSecondStop;
    }

    /**
     * 是否处理最后一秒的情况(不再停止，但记录时长)
     * @param enable
     */
    public void setProcessLastSecond(boolean enable) {
        mProcessLasSecond = enable;
    }

    // 倒计时Handler
    @SuppressWarnings("HandlerLeak")
    private Handler mTimerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            cancelCountDown();
        }
    };

    /**
     * 录制编码监听器
     */
    private MediaEncoder.MediaEncoderListener mEncoderListener = new MediaEncoder.MediaEncoderListener() {

        @Override
        public void onPrepared(MediaEncoder encoder) {
            mPreparedCount++;
            // 不允许录音、允许录制音频并且准备好两个MediaEncoder，就可以开始录制了，如果是GIF，则没有音频
            if (!mRecordAudio || (mRecordAudio && mPreparedCount == 2) || mRecordType == RecordType.Gif) { // 录制GIF，没有音频
                // 准备完成，开始录制
                PreviewRenderer.getInstance().startRecording();
                // 重置
                mPreparedCount = 0;
            }
        }

        @Override
        public void onStarted(MediaEncoder encoder) {
            mStartedCount++;
            // 不允许音频录制、允许录制音频并且开始了两个MediaEncoder，就处于录制状态了，如果是GIF，则没有音频
            if (!mRecordAudio || (mRecordAudio && mStartedCount == 2) || mRecordType == RecordType.Gif) {
                isRecording = true;
                // 重置状态
                mStartedCount = 0;
                // 开始倒计时
                startTimer();
                // 录制开始回调
                if (mRecordListener != null) {
                    mRecordListener.onRecordStarted();
                }

            }
        }

        @Override
        public void onStopped(MediaEncoder encoder) {
        }

        @Override
        public void onReleased(MediaEncoder encoder) { // 复用器释放完成
            mReleaseCount++;
            // 不允许音频录制、允许录制音频并且释放了两个MediaEncoder，就完全释放掉了，如果是GIF，则没有音频
            if (!mRecordAudio || (mRecordAudio && mReleaseCount == 2) || mRecordType == RecordType.Gif) {
                // 录制完成跳转预览页面
                String outputPath = HardcodeEncoder.getInstance().getOutputPath();
                // 添加分段视频，存在时长为0的情况，也就是取消倒计时但不保存时长的情况
                if (getRealDuration() > 0) {
                    addSubVideo(outputPath, (int) getRealDuration());
                } else { // 移除多余的视频
                    FileUtils.deleteFile(outputPath);
                }
                // 重置当前走过的时长
                resetDuration();
                // 处于非录制状态
                isRecording = false;
                // 重置释放状态
                mReleaseCount = 0;

                // 录制完成回调
                if (mRecordListener != null) {
                    mRecordListener.onRecordFinish();
                }

            }
        }
    };

    /**
     * 合并视频
     */
    public void combineVideo(final String path, final VideoCombiner.CombineListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                new VideoCombiner(getSubVideoPathList(), path, listener).combineVideo();
            }
        }).start();
    }

    /**
     * 设置录制类型
     */
    public enum CountDownType {
        TenSecond,
        ThreeMinute,
    }

    /**
     * 录制类型
     */
    public enum RecordType {
        Gif,
        Video
    }
}
