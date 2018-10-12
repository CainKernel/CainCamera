package com.cgfay.landmarklibrary;

import android.util.SparseArray;

/**
 * 人脸关键点引擎
 */
public final class LandmarkEngine {

    private static class EngineHolder {
        public static LandmarkEngine instance = new LandmarkEngine();
    }

    private LandmarkEngine() {
        mFaceArrays = new SparseArray<OneFace>();
    }

    public static LandmarkEngine getInstance() {
        return EngineHolder.instance;
    }

    private final Object mSyncFence = new Object();

    // 人脸对象列表
    // 由于人脸数据个数有限，图像中的人脸个数小于千级，而且人脸索引是连续的，用SparseArray比Hashmap性能要更好
    private final SparseArray<OneFace> mFaceArrays;

    /**
     * 设置人脸数
     * @param size
     */
    public void setFaceSize(int size) {
        synchronized (mSyncFence) {
            // 剔除脏数据，有可能在前一次检测的人脸多余当前人脸
            if (mFaceArrays.size() > size) {
                mFaceArrays.removeAtRange(size, mFaceArrays.size() - size);
            }
        }
    }

    /**
     * 是否存在人脸
     * @return
     */
    public boolean hasFace() {
        boolean result;
        synchronized (mSyncFence) {
            result = mFaceArrays.size() > 0;
        }
        return result;
    }

    /**
     * 获取一个人脸关键点数据对象
     * @return
     */
    public OneFace getOneFace(int index) {
        OneFace oneFace = null;
        synchronized (mSyncFence) {
            oneFace = mFaceArrays.get(index);
            if (oneFace == null) {
                oneFace = new OneFace();
            }
        }
        return oneFace;
    }

    /**
     * 插入一个人脸关键点数据对象
     * @param index
     */
    public void putOneFace(int index, OneFace oneFace) {
        synchronized (mSyncFence) {
            mFaceArrays.put(index, oneFace);
        }
    }

    /**
     * 获取人脸列表
     * @return
     */
    public SparseArray<OneFace> getFaceArrays() {
        return mFaceArrays;
    }

    /**
     * 清空所有人脸对象
     */
    public void clearAll() {
        synchronized (mSyncFence) {
            mFaceArrays.clear();
        }
    }
}
