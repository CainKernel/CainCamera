package com.cgfay.caincamera.activity;

import android.os.Handler;
import android.util.Log;

/**
 * 自定义倒计时
 * Created by cain.huang on 2017/12/28.
 */

public class CustomCountDownTimer {

    private static final String TAG = "CustomCountDownTimer";
    private static final boolean VERBOSE = false;

    // 时长(毫秒)
    private long mMillisInFuture;

    // 计算间隔(毫秒)
    private long mCountDownInterval;

    // 开始停止倒计时标志
    private boolean mTimerStart;

    // 是否释放倒计时
    private boolean mRelease = false;

    public CustomCountDownTimer(long pMillisInFuture, long countDownInterval) {
        mMillisInFuture = pMillisInFuture;
        mCountDownInterval = countDownInterval;
        mTimerStart = false;
        Initialize();
    }

    /**
     * 初始化
     */
    public void Initialize() {
        final Handler handler = new Handler();
        if (VERBOSE) {
            Log.d(TAG, "starting");
        }
        final Runnable counter = new Runnable() {

            public void run() {

                // 释放资源
                if (mRelease) {
                    mRelease = false;
                    handler.removeCallbacksAndMessages(null);
                    return;
                }

                // 开始倒计时
                long sec = mMillisInFuture / 1000;
                if (mTimerStart) {
                    if (mMillisInFuture <= 0) {
                        if (VERBOSE) {
                            Log.d(TAG, "done");
                        }
                        handler.removeCallbacksAndMessages(null);
                    } else {
                        if (VERBOSE) {
                            Log.d(TAG, Long.toString(sec) + " seconds remain");
                        }
                        mMillisInFuture -= mCountDownInterval;
                        handler.postDelayed(this, mCountDownInterval);
                    }
                } else {
                    if (VERBOSE) {
                        Log.d(TAG, Long.toString(sec) + " seconds remain and timer has stopped!");
                    }
                    handler.postDelayed(this, mCountDownInterval);
                }
            }
        };
        handler.postDelayed(counter, mCountDownInterval);
    }

    /**
     * 停止计时
     */
    public void stopCountDown() {
        mTimerStart = false;
    }

    /**
     * 开始计时
     */
    public void startCountDown() {
        mTimerStart = true;
    }


    /**
     * 获取当前时间
     * @return
     */
    public long getCurrentTime() {
        return mMillisInFuture;
    }

    /**
     * 释放资源
     */
    public void release() {
        mRelease = true;
    }

}
