package com.cgfay.caincamera.bean.face;

/**
 * 81个关键点
 * Created by cain on 2017/11/10.
 */

public class Face81PointsLandmark extends FaceLandmark {

    // 左眉毛
    public int leftEyebrowRightCorner = 19;      // 左眉毛右边角
    public int leftEyebrowLeftCorner = 18;       // 左眉毛左边角
    public int leftEyebrowUpperMiddle = 20;      // 左眉毛上中心
    public int leftEyebrowLowerMiddle = 21;      // 左眉毛下中心

    // 右眉毛
    public int rightEyebrowRightCorner = 27;     // 右眉毛右边角
    public int rightEyebrowLeftCorner = 26;      // 右眉毛左上角
    public int rightEyebrowUpperMiddle = 28;     // 右眉毛上中心
    public int rightEyebrowLowerMiddle = 29;     // 右眉毛下中心

    // 左眼
    public int leftEyeTop = 3;  //  左眼球上边
    public int leftEyeCenter = 0; // 左眼球中心
    public int leftEyeBottom = 4;   // 左眼球下边
    public int leftEyeLeftCorner = 1; // 左眼左边角
    public int leftEyeRightCorner = 2; // 左眼右边角

    // 右眼
    public int rightEyeTop = 12;  //  右眼球上边
    public int rightEyeCenter = 13; // 右眼球中心
    public int rightEyeBottom = 9;   // 右眼球下边
    public int rightEyeLeftCorner = 10; // 右眼左边角
    public int rightEyeRightCorner = 11; // 右眼右边角

    public int eyeCenter = -1;   // 两眼中心，81点没有

    // 鼻子
    public int noseTop = 34;         // 鼻尖
    public int noseLeft = 40;        // 鼻子左边
    public int noseRight = 41;       // 鼻子右边
    public int noseLowerMiddle = 35; // 两鼻孔中心

    // 嘴巴
    public int mouthLeftCorner = 44;     // 嘴唇左边
    public int mouthRightCorner = 45;    // 嘴唇右边
    public int mouthUpperLipTop = 46;    // 上嘴唇上中心
    public int mouthUpperLipBottom = 47; // 上嘴唇下中心
    public int mouthLowerLipTop = 54;    // 下嘴唇上中心
    public int mouthLowerLipBottom = 55; // 下嘴唇下中心

    // 下巴
    public int chinLeft = 72;        // 下巴左边
    public int chinRight = 80;       // 下巴右边
    public int chinCenter = 64;      // 下巴中心
}
