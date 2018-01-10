package com.cgfay.caincamera.bean.face;

/**
 * 106关键点位置
 * Created by cain on 2017/11/10.
 */

public class Face106PointsLandmark extends FaceLandmark {
    // 左眉毛
    public int leftEyebrowRightCorner = 37;      // 左眉毛右边角
    public int leftEyebrowLeftCorner = 33;       // 左眉毛左边角
    public int leftEyebrowUpperMiddle = 35;      // 左眉毛上中心
    public int leftEyebrowLowerMiddle = 65;      // 左眉毛下中心

    // 右眉毛
    public int rightEyebrowRightCorner = 38;     // 右眉毛右边角
    public int rightEyebrowLeftCorner = 42;      // 右眉毛左上角
    public int rightEyebrowUpperMiddle = 40;     // 右眉毛上中心
    public int rightEyebrowLowerMiddle = 70;     // 右眉毛下中心

    // 左眼
    public int leftEyeTop = 72;         //  左眼球上边
    public int leftEyeCenter = 74;      // 左眼球中心
    public int leftEyeBottom = 73;      // 左眼球下边
    public int leftEyeLeftCorner = 52;  // 左眼左边角
    public int leftEyeRightCorner = 55; // 左眼右边角

    // 右眼
    public int rightEyeTop = 75;            // 右眼球上边
    public int rightEyeCenter = 77;         // 右眼球中心
    public int rightEyeBottom = 76;         // 右眼球下边
    public int rightEyeLeftCorner = 58;     // 右眼左边角
    public int rightEyeRightCorner = 61;    // 右眼右边角

    public int eyeCenter = 43;   // 两眼中心，81点没有

    // 鼻子
    public int noseTop = 46;         // 鼻尖
    public int noseLeft = 82;        // 鼻子左边
    public int noseRight = 83;       // 鼻子右边
    public int noseLowerMiddle = 49; // 两鼻孔中心

    // 嘴巴
    public int mouthLeftCorner = 84;        // 嘴唇左边
    public int mouthRightCorner = 90;       // 嘴唇右边
    public int mouthUpperLipTop = 87;       // 上嘴唇上中心
    public int mouthUpperLipBottom = 98;    // 上嘴唇下中心
    public int mouthLowerLipTop = 102;      // 下嘴唇上中心
    public int mouthLowerLipBottom = 93;    // 下嘴唇下中心

    // 下巴
    public int chinLeft = 14;        // 下巴左边
    public int chinRight = 18;       // 下巴右边
    public int chinCenter = 16;      // 下巴中心
}
