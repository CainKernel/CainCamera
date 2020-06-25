package com.cgfay.cavfoundation.reader;

/**
 * 媒体读取器状态
 */
public enum AVAssetReaderStatus {
    AVAssetReaderStatusUnknown(0),
    AVAssetReaderStatusReading(1),
    AVAssetReaderStatusCompleted(2),
    AVAssetReaderStatusFailed(3),
    AVAssetReaderStatusCancelled(4),
    ;

    private int value;

    AVAssetReaderStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
