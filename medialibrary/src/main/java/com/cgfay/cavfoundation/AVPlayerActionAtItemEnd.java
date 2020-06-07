package com.cgfay.cavfoundation;

/**
 * 播放
 */
public enum AVPlayerActionAtItemEnd {
    AVPlayerActionAtItemEndAdvance(0),
    AVPlayerActionAtItemEndPause(1),
    AVPlayerActionAtItemEndNone(2);

    private int value;

    AVPlayerActionAtItemEnd(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
