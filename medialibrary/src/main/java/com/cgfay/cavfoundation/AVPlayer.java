package com.cgfay.cavfoundation;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cgfay.coremedia.AVTime;

/**
 * 视频播放器
 */
public class AVPlayer {

    /**
     * 播放状态
     */
    private AVPlayerStatus mStatus;

    /**
     * 播放速度
     */
    private float mRate;

    /**
     * 当前播放item
     */
    private AVPlayerItem mCurrentItem;

    /**
     * 播放完一个item的时候需要做啥处理
     */
    private AVPlayerActionAtItemEnd mActionAtItemEnd;

    /**
     * 播放音量
     */
    private float mVolume;

    /**
     * 静音
     */
    private boolean mMute;

    private AVPlayer() {
        mStatus = AVPlayerStatus.AVPlayerStatusUnknown;
        mRate = 1.0f;
        mCurrentItem = null;
        mActionAtItemEnd = AVPlayerActionAtItemEnd.AVPlayerActionAtItemEndNone;
        mVolume = 1.0f;
        mMute = false;
    }


    public static AVPlayer playerWithUri(@NonNull Context context, @NonNull Uri uri) {
        AVPlayer player = new AVPlayer();
        AVPlayerItem item = new AVPlayerItem(context, uri);
        player.mCurrentItem = item;
        return player;
    }

    public static AVPlayer playerWithPlayerItem(@Nullable AVPlayerItem item) {
        AVPlayer player = new AVPlayer();
        player.mCurrentItem = item;
        return player;
    }

    /**
     * 播放
     */
    public void play() {

    }

    /**
     * 暂停
     */
    public void pause() {

    }

    /**
     * 替换当前播放play item
     */
    public void replaceCurrentItemWithPlayerItem(@Nullable AVPlayerItem item) {
        mCurrentItem = item;
    }

    /**
     * 获取当前时间
     */
    public AVTime getCurrentTime() {
        if (mCurrentItem != null) {
            return mCurrentItem.getCurrentTime();
        }
        return AVTime.kAVTimeZero;
    }

    /**
     *
     * @param time
     */
    public void seekToTime(@NonNull AVTime time) {

    }

    /**
     * 设置播放频率
     * @param rate
     * @param itemTime
     */
    public void setRate(float rate, @NonNull AVTime itemTime, @NonNull AVTime hostClockTime) {

    }

}
