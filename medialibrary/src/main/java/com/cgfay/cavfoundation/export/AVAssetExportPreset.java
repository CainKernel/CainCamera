package com.cgfay.cavfoundation.export;

/**
 * 导出类型
 */
public enum AVAssetExportPreset {
    // 导出低质量
    AVExportPresetQualityLow(1),
    // 导出中质量
    AVExportPresetQualityMedium(2),
    // 导出高质量
    AVExportPresetQualityHighest(3),
    // 导出M4A音频文件
    AVExportPresetM4A(4)
    ;

    private int value;

    AVAssetExportPreset(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
