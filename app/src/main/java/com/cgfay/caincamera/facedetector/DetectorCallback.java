package com.cgfay.caincamera.facedetector;

/**
 * 人脸检测回调
 * Created by cain.huang on 2017/8/18.
 */
public interface DetectorCallback {
    void onTrackingFinish(boolean hasFaces);
}
