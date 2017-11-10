package com.cgfay.caincamera.facetracker;

import java.util.ArrayList;

/**
 * 人脸关键点检测回调
 * Created by cain.huang on 2017/11/10.
 */

public interface FaceTrackerCallback {
    /**
     * 检测完成回调
     * @param hasFaces      是否存在人脸
     * @param facePoints    人脸关键点集合
     */
    void onTrackingFinish(boolean hasFaces, ArrayList<ArrayList> facePoints);
}
