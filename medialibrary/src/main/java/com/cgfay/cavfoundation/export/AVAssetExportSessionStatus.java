package com.cgfay.cavfoundation.export;

/**
 * 导出状态枚举
 */
public enum AVAssetExportSessionStatus {
    AVExportSessionStatusUnknown(0),
    AVAssetExportSessionStatusWaiting(1),
    AVAssetExportSessionStatusExporting(2),
    AVAssetExportSessionStatusCompleted(3),
    AVAssetExportSessionStatusFailed(4),
    AVAssetExportSessionStatusCancelled(5);

    private int value;
    AVAssetExportSessionStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
