package com.cgfay.landmarklibrary;

/**
 * 关键点索引（106个关键点 + 扩展8个关键点）
 * Created by cain on 2017/11/10.
 */

public final class FaceLandmark {

    private FaceLandmark() {}

    // 左眉毛
    public static int leftEyebrowRightCorner = 37;      // 左眉毛右边角
    public static int leftEyebrowLeftCorner = 33;       // 左眉毛左边角
    public static int leftEyebrowLeftTopCorner = 34;    // 左眉毛左顶角
    public static int leftEyebrowRightTopCorner = 36;   // 左眉毛右顶角
    public static int leftEyebrowUpperMiddle = 35;      // 左眉毛上中心
    public static int leftEyebrowLowerMiddle = 65;      // 左眉毛下中心

    // 右眉毛
    public static int rightEyebrowRightCorner = 38;     // 右眉毛右边角
    public static int rightEyebrowLeftCorner = 42;      // 右眉毛左上角
    public static int rightEyebrowLeftTopCorner = 39;   // 右眉毛左顶角
    public static int rightEyebrowRightTopCorner = 41;  // 右眉毛右顶角
    public static int rightEyebrowUpperMiddle = 40;     // 右眉毛上中心
    public static int rightEyebrowLowerMiddle = 70;     // 右眉毛下中心

    // 左眼
    public static int leftEyeTop = 72;         //  左眼球上边
    public static int leftEyeCenter = 74;      // 左眼球中心
    public static int leftEyeBottom = 73;      // 左眼球下边
    public static int leftEyeLeftCorner = 52;  // 左眼左边角
    public static int leftEyeRightCorner = 55; // 左眼右边角

    // 右眼
    public static int rightEyeTop = 75;            // 右眼球上边
    public static int rightEyeCenter = 77;         // 右眼球中心
    public static int rightEyeBottom = 76;         // 右眼球下边
    public static int rightEyeLeftCorner = 58;     // 右眼左边角
    public static int rightEyeRightCorner = 61;    // 右眼右边角

    public static int eyeCenter = 43;   // 两眼中心

    // 鼻子
    public static int noseTop = 46;         // 鼻尖
    public static int noseLeft = 82;        // 鼻子左边
    public static int noseRight = 83;       // 鼻子右边
    public static int noseLowerMiddle = 49; // 两鼻孔中心

    // 脸边沿
    public static int leftCheekEdgeCenter = 4;        // 左脸颊边沿中心
    public static int rightCheekEdgeCenter = 28;      // 右脸颊边沿中心

    // 嘴巴
    public static int mouthLeftCorner = 84;        // 嘴唇左边
    public static int mouthRightCorner = 90;       // 嘴唇右边
    public static int mouthUpperLipTop = 87;       // 上嘴唇上中心
    public static int mouthUpperLipBottom = 98;    // 上嘴唇下中心
    public static int mouthLowerLipTop = 102;      // 下嘴唇上中心
    public static int mouthLowerLipBottom = 93;    // 下嘴唇下中心

    // 下巴
    public static int chinLeft = 14;        // 下巴左边
    public static int chinRight = 18;       // 下巴右边
    public static int chinCenter = 16;      // 下巴中心

    // 扩展的关键点(8个)
    public static int mouthCenter = 106;        // 嘴巴中心
    public static int leftEyebrowCenter = 107;  // 左眉心
    public static int rightEyebrowCenter = 108; // 右眉心
    public static int leftHead = 109;           // 额头左侧
    public static int headCenter = 110;         // 额头中心
    public static int rightHead = 111;          // 额头右侧
    public static int leftCheekCenter = 112;    // 左脸颊中心
    public static int rightCheekCenter = 113;   // 右脸颊中心
}
