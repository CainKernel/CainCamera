package com.cgfay.caincamera.core;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.cgfay.caincamera.type.CountDownType;
import com.cgfay.caincamera.utils.AccurateCountDownTimer;
import com.cgfay.caincamera.utils.StringUtils;

/**
 * 倒计时管理器
 * Created by cain.huang on 2017/12/29.
 */

public final class CountDownManager {

    private static CountDownManager mInstance;

    public static CountDownManager getInstance() {
        if (mInstance == null) {
            mInstance = new CountDownManager();
        }
        return mInstance;
    }

    // 精确倒计时
    private AccurateCountDownTimer mCountDownTimer;
    // 倒计时数值
    private long mMaxMillisSeconds = VideoListManager.DURATION_TEN_SECOND;
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

    // 是否停止
    private boolean mFinish = false;

    // 倒计时监听
    private CountDownListener mListener;

    // 倒计时Handler
    private Handler mTimerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            cancelCountDown();
        }
    };

    /**
     * 初始化倒计时
     */
    public void initCountDownTimer() {

        cancelCountDown();

        mCountDownTimer = new AccurateCountDownTimer(mMaxMillisSeconds, mCountDownInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (!mFinish) {
                    // 获取视频总时长
                    int previousDuration = VideoListManager.getInstance().getDuration();
                    // 获取当前分段视频走过的时间
                    mCurrentDuration = mMaxMillisSeconds - millisUntilFinished;
                    // 如果总时长够设定的最大时长，则需要停止计时
                    if (previousDuration + mCurrentDuration >= mMaxMillisSeconds) {
                        mCurrentDuration = mMaxMillisSeconds - previousDuration;
                        mFinish = true;
                    }
                    // 计时回调
                    if (mListener != null) {
                        mListener.onProgressChanged(getVisibleDuration());
                    }
                    // 是否需要结束计时器
                    if (mFinish) {
                        mTimerHandler.sendEmptyMessage(0);
                    }
                }
            }

            @Override
            public void onFinish() {
                mFinish = true;
                if (mListener != null) {
                    mListener.onProgressChanged(getVisibleDuration(true));
                }
            }
        };
    }

    /**
     * 开始倒计时
     */
    public void startTimer() {
        if (mCountDownTimer != null) {
            mCountDownTimer.start();
        }
    }

    /**
     * 停止倒计时
     */
    public void stopTimer() {
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
    public void cancelTimerWithoutSaving() {
        cancelCountDown();
        resetDuration();
        resetLastSecondStop();
        mLastSecondLeftTime = 0;
    }

    /**
     * 取消倒计时
     */
    private void cancelCountDown() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
        // 复位结束标志
        mFinish = false;
    }

    /**
     * 倒计时回调
     */
    public interface CountDownListener {
        // 时间改变回调
        void onProgressChanged(long duration);
    }

    /**
     * 设置倒计时回调
     * @param listener
     */
    public void setCountDownListener(CountDownListener listener) {
        mListener = listener;
    }


    /**
     * 重置当前走过的时长
     */
    public void resetDuration() {
        mCurrentDuration = 0;
    }

    /**
     * 重置最后一秒停止标志
     */
    public void resetLastSecondStop() {
        mLastSecondStop = false;
    }

    // ----------------------------- setter and getter ---------------------------------------------

    /**
     * 设置总时长
     * @param type
     */
    public void setMilliSeconds(CountDownType type) {
        if (type == CountDownType.TenSecond) {
            mMaxMillisSeconds = VideoListManager.DURATION_TEN_SECOND;
        } else if (type == CountDownType.ThreeMinute) {
            mMaxMillisSeconds = VideoListManager.DURATION_THREE_MINUTE;
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
        return mMaxMillisSeconds - VideoListManager.getInstance().getDuration() - mCurrentDuration;
    }

    /**
     * 获取当前实际时长 (跟显示的时长不一定不一样)
     * @return
     */
    public long getRealDuration() {
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
            long time = VideoListManager.getInstance().getDuration()
                    + mCurrentDuration;
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
    public void setProcessLasSecond(boolean enable) {
        mProcessLasSecond = enable;
    }
}
