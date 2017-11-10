package com.cgfay.caincamera.bean.face;

/**
 * Created by cain on 2017/11/10.
 */

public class Face101PointsLandmark extends FaceLandmark {
    // 左眉毛
    public int leftEyebrowRightCorner = 24;      // 左眉毛右边角
    public int leftEyebrowLeftCorner = 19;       // 左眉毛左边角
    public int leftEyebrowUpperMiddle = 21;      // 左眉毛上中心
    public int leftEyebrowLowerMiddle = 27;      // 左眉毛下中心

    // 右眉毛
    public int rightEyebrowRightCorner = 34;     // 右眉毛右边角
    public int rightEyebrowLeftCorner = 29;      // 右眉毛左上角
    public int rightEyebrowUpperMiddle = 32;     // 右眉毛上中心
    public int rightEyebrowLowerMiddle = 36;     // 右眉毛下中心

    // 左眼
    public int leftEyeTop = 42;  //  左眼球上边
    public int leftEyeCenter = 95; // 左眼球中心
    public int leftEyeBottom = 46;   // 左眼球下边
    public int leftEyeLeftCorner = 39; // 左眼左边角
    public int leftEyeRightCorner = 45; // 左眼右边角

    // 右眼
    public int rightEyeTop = 54;  //  右眼球上边
    public int rightEyeCenter = 96; // 右眼球中心
    public int rightEyeBottom = 60;   // 右眼球下边
    public int rightEyeLeftCorner = 51; // 右眼左边角
    public int rightEyeRightCorner = 57; // 右眼右边角

    public int eyeCenter = 97;   // 两眼中心，81点没有

    // 鼻子
    public int noseTop = 99;         // 鼻尖
    public int noseLeft = 66;        // 鼻子左边
    public int noseRight = 71;       // 鼻子右边
    public int noseLowerMiddle = 100; // 两鼻孔中心

    // 嘴巴
    public int mouthLeftCorner = 75;     // 嘴唇左边
    public int mouthRightCorner = 81;    // 嘴唇右边
    public int mouthUpperLipTop = 78;    // 上嘴唇上中心
    public int mouthUpperLipBottom = 89; // 上嘴唇下中心
    public int mouthLowerLipTop = 93;    // 下嘴唇上中心
    public int mouthLowerLipBottom = 84; // 下嘴唇下中心

    // 下巴
    public int chinLeft = 8;        // 下巴左边
    public int chinRight = 10;       // 下巴右边
    public int chinCenter = 9;      // 下巴中心
}
