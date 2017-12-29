package com.cgfay.caincamera.core;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cgfay.caincamera.type.CountDownType;
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

    // 计时器
    private CountDownTimer mCountDownTimer;
    // 倒计时数值
    private long mMaxMillisSeconds = VideoListManager.DURATION_TEN_SECOND;
    // 50毫秒读取一次
    private long mCountDownInterval = 50;

    // 当前计算的时长
    private long mCurrentDuration = 0;

    // 是否需要处理最后一秒的情况
    private boolean mProcessLasSecond = true;

    // 需要等待计时器停止表示，用于最后一秒内点击停止录制的情况
    private boolean mNeedWaitFinish = false;
    // 最后一秒剩余时间(ms)
    private long mLastSecondLeft = 0;

    // 倒计时监听
    private CountDownListener mListener;

    // 倒计时Handler
    private Handler mTimerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            if (mCountDownTimer != null) {
                mCountDownTimer.cancel();
                mCountDownTimer = null;
            }
        }
    };

    /**
     * 初始化倒计时
     */
    public void initCountDownTimer() {

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }

        mCountDownTimer = new CountDownTimer(mMaxMillisSeconds, mCountDownInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                int previousDuration = VideoListManager.getInstance().getDuration();
                // 获取当前分段视频走过的时间
                mCurrentDuration = mMaxMillisSeconds - millisUntilFinished;
                // 如果总时长够设定的最大时长，则需要停止计时
                boolean needToFinish = false;
                if (previousDuration + mCurrentDuration >= mMaxMillisSeconds) {
                    mCurrentDuration = mMaxMillisSeconds - previousDuration;
                    needToFinish = true;
                }
                // 计时回调
                if (mListener != null) {
                    mListener.onProgressChanged(getVisibleDuration());
                }
                // 是否需要结束计时器
                if (needToFinish) {
                    mTimerHandler.sendEmptyMessage(0);
                }
            }

            @Override
            public void onFinish() {
                if (mListener != null) {
                    Log.d("ShutterButton", "countdown timer finish");
                    mListener.onProgressChanged(getVisibleDuration());
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
        // 判断是否需要处理最后一秒的情况
        if (mProcessLasSecond) {
            // 判断是否是最后一秒点击停止的
            long leftTime = getAvailableTime();
            // 如果在下一次计时器回调之前剩余时间小于1秒
            if (leftTime + mCountDownInterval < 1000) {
                mNeedWaitFinish = true;
                mLastSecondLeft = leftTime;
                return;
            }
        } else {
            mLastSecondLeft = 0;
        }
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
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
     * 重置时长
     */
    public void resetDuration() {
        mCurrentDuration = 0;
    }

    /**
     * 重置最后一秒记录的时长
     */
    public void resetLastSecondLeft() {
        mLastSecondLeft = 0;
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
    public long getCurrentDuration() {
        return mCurrentDuration;
    }

    /**
     * 获取显示的时长
     */
    public long getVisibleDuration() {
        long time = VideoListManager.getInstance().getDuration()
                + mCurrentDuration + mLastSecondLeft;
        if (time > mMaxMillisSeconds) {
            time = mMaxMillisSeconds;
        }
        return time;
    }

    /**
     * 获取显示的时间文本
     * @return
     */
    public String getVisibleDurationString() {
        return StringUtils.generateMillisTime((int) getVisibleDuration());
    }
}
