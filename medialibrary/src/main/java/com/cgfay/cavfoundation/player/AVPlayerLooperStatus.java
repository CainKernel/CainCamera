package com.cgfay.cavfoundation.player;

/**
 * These constants are returned by the AVPlayerLooper status property to indicate whether it can successfully accomplish looping playback.
 */
public enum AVPlayerLooperStatus {
    AVPlayerLooperStatusUnknown(0),
    AVPlayerLooperStatusReady(1),
    AVPlayerLooperStatusFailed(2),
    AVPlayerLooperStatusCancelled(3),
    ;

    int value;
    AVPlayerLooperStatus(int value) {
        this.value = value;
    }
}
