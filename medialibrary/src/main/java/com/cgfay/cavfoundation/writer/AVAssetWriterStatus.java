package com.cgfay.cavfoundation.writer;

/**
 * 媒体写入状态的
 */
public enum AVAssetWriterStatus {
    AVAssetWriterStatusUnknown(0),      // 未知状态
    AVAssetWriterStatusWriting(1),      // 正在写入
    AVAssetWriterStatusCompleted(2),    // 写入完成
    AVAssetWriterStatusFailed(3),       // 写入失败
    AVAssetWriterStatusCancelled(4),    // 取消写入
    ;
    private int value;

    AVAssetWriterStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
