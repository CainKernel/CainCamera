package com.cgfay.cavfoundation;

/**
 * 播放器状态
 */
public enum AVPlayerItemStatus {
    AVPlayerItemStatusUnknown(0),
    AVPlayerItemStatusReadyToPlay(1),
    AVPlayerItemStatusFailed(2)
    ;

    private int value;

    AVPlayerItemStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
