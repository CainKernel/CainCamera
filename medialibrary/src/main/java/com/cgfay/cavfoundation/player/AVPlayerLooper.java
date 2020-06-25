package com.cgfay.cavfoundation.player;

import androidx.annotation.NonNull;

import com.cgfay.coremedia.AVTimeRange;

import java.util.List;

/**
 * AVPlayerLooper is a helper object that repeatedly plays an AVPlayerItem with an AVQueuePlayer
 */
public class AVPlayerLooper {

    private AVPlayerLooperStatus mStatus;

    /**
     * Number of times the specified AVPlayerItem has been played
     */
    private int mLoopCount;

    private List<AVPlayerItem> mLoopingPlayerItems;

    public static AVPlayerLooper playerLooperWithPlayer(@NonNull AVQueuePlayer player, AVPlayerItem itemToLoop, AVTimeRange loopRange) {
        AVPlayerLooper looper = new AVPlayerLooper();

        return looper;
    }

    public static AVPlayerLooper playerLooperWithPlayer(AVQueuePlayer player, AVPlayerItem itemToLoop) {
        AVPlayerLooper looper = new AVPlayerLooper();

        return looper;
    }

    public void initWidthPlayer(AVQueuePlayer player, AVPlayerItem itemToLoop, AVTimeRange loopRange) {

    }

    public void disableLooping() {

    }

    public AVPlayerLooperStatus getStatus() {
        return mStatus;
    }

    public int getLoopCount() {
        return mLoopCount;
    }

    public List<AVPlayerItem> getLoopingPlayerItems() {
        return mLoopingPlayerItems;
    }
}
