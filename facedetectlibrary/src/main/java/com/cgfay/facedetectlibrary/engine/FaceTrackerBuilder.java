package com.cgfay.facedetectlibrary.engine;

import com.cgfay.facedetectlibrary.listener.FaceTrackerCallback;

/**
 * 人脸检测构建器
 */
public final class FaceTrackerBuilder {

    private FaceTracker mFaceTracker;
    private FaceTrackParam mFaceTrackParam;

    public FaceTrackerBuilder(FaceTracker tracker, FaceTrackerCallback callback) {
        mFaceTracker = tracker;
        mFaceTrackParam = FaceTrackParam.getInstance();
        mFaceTrackParam.trackerCallback = callback;
    }

    /**
     * 准备检测器
     */
    public void initTracker() {
        mFaceTracker.initTracker();
    }

    /**
     * 是否预览检测
     * @param previewTrack
     * @return
     */
    public FaceTrackerBuilder previewTrack(boolean previewTrack) {
        mFaceTrackParam.previewTrack = previewTrack;
        return this;
    }

}
