package com.cgfay.facedetectlibrary.listener;

import java.util.ArrayList;

/**
 * 人脸关键点检测回调
 * Created by cain.huang on 2017/11/10.
 */

public interface FaceTrackerCallback {
    /**
     * 检测完成回调
     * @param hasFaces      是否存在人脸
     * @param debugFacePoints 调试关键点
     */
    void onTrackingFinish(boolean hasFaces, ArrayList<ArrayList> debugFacePoints);
}
