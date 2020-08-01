package com.cgfay.cavfoundation.generator;

/**
 * 缩略图读取结果
 */
public enum AVAssetImageGeneratorResult {

    AVAssetImageGeneratorSucceeded(0),
    AVAssetImageGeneratorFailed(1),
    AVAssetImageGeneratorCancelled(2)
    ;

    private int value;

    AVAssetImageGeneratorResult(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
