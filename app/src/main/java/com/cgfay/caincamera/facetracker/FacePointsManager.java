package com.cgfay.caincamera.facetracker;

import com.cgfay.caincamera.bean.face.Face106PointsLandmark;
import com.cgfay.caincamera.bean.face.Face81PointsLandmark;
import com.cgfay.caincamera.bean.face.FaceLandmark;

import java.util.ArrayList;

/**
 * 人脸关键点管理器
 * 采用类似显示系统的双缓冲帧的方案，为了绘制时用到人脸关键点的同时，也可以录入新的一帧的关键点
 * Created by cain on 2017/11/10.
 */

public class FacePointsManager {

    private static FacePointsManager mInstance;

    // 是否处于添加人脸关键点过程
    private boolean isAddingPoints = false;

    // 人脸关键点集合
    private ArrayList<ArrayList> mFacePoints = new ArrayList<ArrayList>();

    // 后台录入关键点集合
    private ArrayList<ArrayList> mBackgroundPoints = new ArrayList<ArrayList>();

    // 一个人脸的所有关键点
    private ArrayList<float[]> mOneFacePoints = new ArrayList<float[]>();

    // 姿态角集合 float[0] = pitch, float[1] = yaw, float[2] = roll
    private ArrayList<float[]> mEulers = new ArrayList<float[]>();

    // 后台录入姿态角集合
    private ArrayList<float[]> mBackgroundEulers = new ArrayList<float[]>();

    public static FacePointsManager getInstance() {
        if (mInstance == null) {
            mInstance = new FacePointsManager();
        }
        return mInstance;
    }

    /**
     * 准备添加关键点，清除就的数据
     */
    synchronized public void prepareToAddPoints() {
        isAddingPoints = true;
        mOneFacePoints.clear();
        mBackgroundEulers.clear();
    }

    /**
     * 添加一个关键点
     * @param point
     */
    synchronized public void addOnePoint(float[] point) {
        if (isAddingPoints) {
            mOneFacePoints.add(point);
        }
    }

    /**
     * 添加姿态角
     * @param eulers
     */
    synchronized public void addEulers(float[] eulers) {
        if (isAddingPoints) {
            mBackgroundEulers.add(eulers);
        }
    }

    /**
     * 添加一个人脸的关键点
     */
    synchronized public void addOneFacePoints() {
        if (isAddingPoints) {
            mBackgroundPoints.add(mOneFacePoints);
        }
        mOneFacePoints.clear();
    }

    /**
     * 更新人脸关键点
     */
    synchronized public void updateFacePoints() {
        isAddingPoints = false;
        resetFacePoints();
        if (mBackgroundPoints.size() > 0) {
            mFacePoints.addAll(mBackgroundPoints);
        }
        if (mBackgroundEulers.size() > 0) {
            mEulers.addAll(mBackgroundEulers);
        }
    }

    /**
     * 清空人脸关键点
     */
    public void resetFacePoints() {
        mFacePoints.clear();
        mEulers.clear();
    }

    //---------------------------- setter and getter ------------------------------
    /**
     * 获取人脸关键点
     */
    public ArrayList<ArrayList> getFacePoints() {
        return mFacePoints;
    }

    /**
     * 获取姿态角
     * @return
     */
    public ArrayList<float[]> getEulers() {
        return mEulers;
    }

    /**
     * 获取关键点
     * @return
     */
    public FaceLandmark getFaceLandmark() {
        if (FaceTrackManager.getInstance().isIs106Points()) {
            return new Face106PointsLandmark();
        } else {
            return new Face81PointsLandmark();
        }
    }
}
