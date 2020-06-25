package com.cgfay.cavfoundation.writer;

public enum AVAssetWriterStatus {
    AVAssetWriterStatusUnknown(0),
    AVAssetWriterStatusWriting(1),
    AVAssetWriterStatusCompleted(2),
    AVAssetWriterStatusFailed(3),
    AVAssetWriterStatusCancelled(4),
    ;
    private int value;

    AVAssetWriterStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
