package com.cgfay.cavfoundation;

public enum AVPlayerStatus {
    AVPlayerStatusUnknown(0),
    AVPlayerStatusReadyToPlay(1),
    AVPlayerStatusFailed(2);

    private int value;

    AVPlayerStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
