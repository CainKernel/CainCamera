package com.cgfay.caincamera.bean.face;

/**
 * 关键点标识基类
 * Created by cain on 2017/11/10.
 */

public class FaceLandmark {

    // 左眉毛
    public int leftEyebrowRightCorner;      // 左眉毛右边角
    public int leftEyebrowLeftCorner;       // 左眉毛左边角
    public int leftEyebrowUpperMiddle;      // 左眉毛上中心
    public int leftEyebrowLowerMiddle;      // 左眉毛下中心

    // 右眉毛
    public int rightEyebrowRightCorner;     // 右眉毛右边角
    public int rightEyebrowLeftCorner;      // 右眉毛左上角
    public int rightEyebrowUpperMiddle;     // 右眉毛上中心
    public int rightEyebrowLowerMiddle;     // 右眉毛下中心

    // 左眼
    public int leftEyeTop;  //  左眼球上边
    public int leftEyeCenter; // 左眼球中心
    public int leftEyeBottom;   // 左眼球下边
    public int leftEyeLeftCorner; // 左眼左边角
    public int leftEyeRightCorner; // 左眼右边角

    // 右眼
    public int rightEyeTop;  //  右眼球上边
    public int rightEyeCenter; // 右眼球中心
    public int rightEyeBottom;   // 右眼球下边
    public int rightEyeLeftCorner; // 右眼左边角
    public int rightEyeRightCorner; // 右眼右边角

    public int eyeCenter;   // 两眼中心，81点没有

    // 鼻子
    public int noseTop;         // 鼻尖
    public int noseLeft;        // 鼻子左边
    public int noseRight;       // 鼻子右边
    public int noseLowerMiddle; // 两鼻孔中心

    // 嘴巴
    public int mouthLeftCorner;     // 嘴唇左边
    public int mouthRightCorner;    // 嘴唇右边
    public int mouthUpperLipTop;    // 上嘴唇上中心
    public int mouthUpperLipBottom; // 上嘴唇下中心
    public int mouthLowerLipTop;    // 下嘴唇上中心
    public int mouthLowerLipBottom; // 下嘴唇下中心

    // 下巴
    public int chinLeft;        // 下巴左边
    public int chinRight;       // 下巴右边
    public int chinCenter;      // 下巴中心

}
