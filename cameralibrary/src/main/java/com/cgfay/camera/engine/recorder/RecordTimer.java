package com.cgfay.camera.engine.recorder;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

/**
 * 录制计时器
 * Created by cain.huang on 2018/1/2.
 */

public abstract class RecordTimer {

    //  倒计时(毫秒)
    private final long mMillisInFuture;

    // 回调时间(毫秒)
    private final long mCountdownInterval;

    // 停止的时间
    private long mStopTimeInFuture;

    // 计算回调次数
    private int mTickCounter;
    // 记录开始时间
    private long mStartTime;

    public RecordTimer(long millisInFuture, long countDownInterval) {
        mMillisInFuture = millisInFuture;
        mCountdownInterval = countDownInterval;
        mTickCounter = 0;
    }

    /**
     * 取消计时
     */
    public final void cancel() {
        mHandler.removeMessages(MSG);
    }

    /**
     * 开始计时
     * @return
     */
    public synchronized final RecordTimer start() {
        if (mMillisInFuture <= 0) {
            onFinish();
            return this;
        }

        mStartTime = SystemClock.elapsedRealtime();
        mStopTimeInFuture = mStartTime + mMillisInFuture;

        mHandler.sendMessage(mHandler.obtainMessage(MSG));
        return this;
    }

    /**
     * 计时间隔回调
     * @param millisUntilFinished
     */
    public abstract void onTick(long millisUntilFinished);

    /**
     * 完成计时
     */
    public abstract void onFinish();

    private static final int MSG = 1;

    // handler回调
    @SuppressWarnings("HandlerLeak")
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            synchronized (RecordTimer.this) {
                // 获取精确倒计时
                final long millisLeft = mStopTimeInFuture
                        - SystemClock.elapsedRealtime();

                if (millisLeft <= 0) {
                    onFinish();
                } else if (millisLeft < mCountdownInterval) {
                    // no tick, just delay until done
                    sendMessageDelayed(obtainMessage(MSG), millisLeft);
                } else {
                    long lastTickStart = SystemClock.elapsedRealtime();
                    onTick(millisLeft);
                    // 计算延时
                    long now = SystemClock.elapsedRealtime();
                    long extraDelay = now - mStartTime - mTickCounter
                            * mCountdownInterval;
                    mTickCounter++;
                    long delay = lastTickStart + mCountdownInterval - now
                            - extraDelay;
                    // 如果回调时间超过了延时技术，则跳过下一次回调
                    while (delay < 0) {
                        delay += mCountdownInterval;
                    }
                    // 下一次的回调延时
                    sendMessageDelayed(obtainMessage(MSG), delay);
                }
            }
        }
    };
}
