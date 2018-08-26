package com.cgfay.filterlibrary.glfilter.advanced.face;

import java.util.ArrayList;

/**
 * 人脸关键点回调
 */
public interface OnFacePointsListener {

    // 是否显示人脸关键点
    boolean showFacePoints();

    // 获取调试用的人脸关键点
    ArrayList<ArrayList> getDebugFacePoints();
}
