package com.cgfay.filterlibrary.glfilter.model;

/**
 * 美颜参数
 */
public class Beauty {
    // 磨皮程度 0.0 ~ 1.0f
    public float beautyIntensity;
    // 美肤程度 0.0 ~ 0.5f
    public float complexionIntensity;
    // 瘦脸程度 0.0 ~ 1.0f
    public float faceLift;
    // 削脸程度 0.0 ~ 1.0f
    public float faceShave;
    // 小脸 0.0 ~ 1.0f
    public float faceNarrow;
    // 下巴-1.0f ~ 1.0f
    public float chinIntensity;
    // 法令纹 0.0 ~ 1.0f
    public float nasolabialFoldsIntensity;
    // 额头 -1.0f ~ 1.0f
    public float foreheadIntensity;
    // 大眼 -1.0f ~ 1.0f
    public float eyeEnlargeIntensity;
    // 眼距 -1.0f ~ 1.0f
    public float eyeDistanceIntensity;
    // 眼角 -1.0f ~ 1.0f
    public float eyeCornerIntensity;
    // 卧蚕 0.0f ~ 1.0f
    public float eyeFurrowsIntensity;
    // 眼袋 0.0 ~ 1.0f
    public float eyeBagsIntensity;
    // 亮眼 0.0 ~ 1.0f
    public float eyeBrightIntensity;
    // 瘦鼻 0.0 ~ 1.0f
    public float noseThinIntensity;
    // 鼻翼 0.0 ~ 1.0f
    public float alaeIntensity;
    // 长鼻子 0.0 ~ 1.0f
    public float proboscisIntensity;
    // 嘴型 0.0 ~ 1.0f;
    public float mouthEnlargeIntensity;
    // 美牙 0.0 ~ 1.0f
    public float teethBeautyIntensity;

    public Beauty() {
        reset();
    }

    /**
     * 重置为默认参数
     */
    public void reset() {
        beautyIntensity = 1.0f;
        complexionIntensity = 0.5f;
        faceLift = 0.0f;
        faceShave = 0.0f;
        faceNarrow = 0.0f;
        chinIntensity = 0.0f;
        nasolabialFoldsIntensity = 0.0f;
        foreheadIntensity = 0.0f;
        eyeEnlargeIntensity = 0.0f;
        eyeDistanceIntensity = 0.0f;
        eyeCornerIntensity = 0.0f;
        eyeFurrowsIntensity = 0.0f;
        eyeBagsIntensity = 0.0f;
        eyeBrightIntensity = 0.0f;
        noseThinIntensity = 0.0f;
        alaeIntensity = 0.0f;
        proboscisIntensity = 0.0f;
        mouthEnlargeIntensity = 0.0f;
        teethBeautyIntensity = 0.0f;
    }
}
