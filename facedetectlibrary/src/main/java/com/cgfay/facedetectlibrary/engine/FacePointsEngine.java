package com.cgfay.facedetectlibrary.engine;

import com.cgfay.facedetectlibrary.bean.FaceInfo;

import java.util.ArrayList;

/**
 * 人脸关键点殷勤
 */
public class FacePointsEngine {

    private final Object mSyncFence = new Object();

    // 人脸检测得到的关键点
    private final ArrayList<FaceInfo> mFacePoints;
    // 扩展之后的关键点
    private final ArrayList<FaceInfo> mExtraFacePoints;

    private FacePointsEngine() {
        mFacePoints = new ArrayList<>();
        mExtraFacePoints = new ArrayList<>();
    }

    public static FacePointsEngine newInstance() {
        return FacePointsEngineHolder.instance;
    }

    private static class FacePointsEngineHolder {
        private static FacePointsEngine instance = new FacePointsEngine();
    }

    /**
     * 添加人脸关键点集合
     * @param facePoints
     */
    public void addFacePoints(ArrayList<FaceInfo> facePoints) {
        synchronized (mSyncFence) {
            mFacePoints.clear();
            mFacePoints.addAll(facePoints);
        }
    }

    /**
     * 计算额外的关键点
     */
    public void calculateExtraFacePoints() {
        synchronized (mSyncFence) {
            extraFacePoints();
        }
    }

    /**
     * 额外关键点
     */
    private void extraFacePoints() {

    }

    /**
     * 获取人脸关键点
     * @return
     */
    public ArrayList<FaceInfo> getFacePoints() {
        return mExtraFacePoints;
    }
}
