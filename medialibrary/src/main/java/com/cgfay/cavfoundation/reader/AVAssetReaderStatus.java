package com.cgfay.cavfoundation.reader;

/**
 * 媒体读取器状态
 */
public enum AVAssetReaderStatus {
    AVAssetReaderStatusUnknown(0),      // 未知状态
    AVAssetReaderStatusReading(1),      // 正在读取
    AVAssetReaderStatusCompleted(2),    // 读取完成
    AVAssetReaderStatusFailed(3),       // 读取失败
    AVAssetReaderStatusCancelled(4),    // 取消读取
    ;

    private int value;

    AVAssetReaderStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
