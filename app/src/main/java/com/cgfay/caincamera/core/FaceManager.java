package com.cgfay.caincamera.core;

import android.content.Context;
import android.util.Log;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.utils.CameraUtils;
import com.cgfay.caincamera.utils.SensorEventUtil;
import com.cgfay.caincamera.utils.faceplus.ConUtil;
import com.megvii.facepp.sdk.Facepp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Face++的人脸检测管理器
 * Created by cain.huang on 2017/8/15.
 */
public class FaceManager {

    private static FaceManager instance;

    // 人脸检测
    private Facepp mFacepp;
    // 最小人脸检测大小
    private int minFaceSize = 200;
    // 检测时间
    private int detectionInterval = 25;
    // 是否只检测一个人脸
    private boolean isOneFaceTrackig = false;
    // 是否区域选择
    private boolean isROIDetect = false;
    // 区域选择缩放比
    private float roiRatio = 0.8f;
    // 置信度
    private float[] mConfidence;
    // 姿态角
    private float[] mPitch;    // x轴
    private float[] mYaw;      // y轴
    private float[] mRoll;     // z轴

    // 是否检测106个人脸关键点检测
    private boolean is106Points = false;
    // 是否检测性别年龄等属性
    private boolean isFaceProperty = false;
    // 男人还是女人
    private String[] mGender;
    //年龄
    private int[] mAge;
    // 是否3D姿势
    private boolean is3DPose = true;
    // 是否后置相机
    private boolean isBackCamera = false;
    // 传感器
    private SensorEventUtil mSensorUtil;
    // 角度
    private int rotation = 0;
    private ArrayList<ArrayList> mFaceVertices;

    private FaceManager() {}

    public static FaceManager getInstance() {

        if (instance == null) {
            instance = new FaceManager();
        }
        return instance;
    }

    /**
     * 初始化人脸检测配置
     * @param context
     * @param width 图像的宽度
     * @param height 图像的高度
     */
    public void initFaceConfig(Context context, int width, int height) {
        mSensorUtil = new SensorEventUtil(context);
        mFacepp = new Facepp();
        int left = 0;
        int top = 0;
        int right = width;
        int bottom = height;

        if (isROIDetect) {
            float line = height * roiRatio;
            left = (int) ((width - line) / 2.0f);
            top = (int) ((height - line) / 2.0f);
            right = width - left;
            bottom = height - top;
        }

        mFacepp.init(context, ConUtil.getFileContent(context, R.raw.megviifacepp_0_4_7_model));
        Facepp.FaceppConfig faceppConfig = mFacepp.getFaceppConfig();
        faceppConfig.interval = detectionInterval;
        faceppConfig.minFaceSize = minFaceSize;
        faceppConfig.roi_left = left;
        faceppConfig.roi_top = top;
        faceppConfig.roi_right = right;
        faceppConfig.roi_bottom = bottom;
        // 是否只检测一个人脸
        if (isOneFaceTrackig)
            faceppConfig.one_face_tracking = 1;
        else
            faceppConfig.one_face_tracking = 0;
        // 识别模式，常规、鲁棒性还是快速
        faceppConfig.detectionMode = Facepp.FaceppConfig.DETECTION_MODE_TRACKING_ROBUST;
        mFacepp.setFaceppConfig(faceppConfig);
    }

    /**
     * 设置人脸检测配置
     * @param rotation
     */
    private void setConfig(int rotation) {
        Facepp.FaceppConfig faceppConfig = mFacepp.getFaceppConfig();
        if (faceppConfig.rotation != rotation) {
            faceppConfig.rotation = rotation;
            mFacepp.setFaceppConfig(faceppConfig);
        }
    }

    /**
     * 人脸检测
     * @param data
     * @param width
     * @param height
     */
    public void faceDetecting(byte[] data, int width, int height) {
        // 配置旋转角度
        int orientation = mSensorUtil.orientation;
        if (orientation == 0)
            rotation = CameraUtils.getPreviewOrientation();
        else if (orientation == 1)
            rotation = 0;
        else if (orientation == 2)
            rotation = 180;
        else if (orientation == 3)
            rotation = 360 - CameraUtils.getPreviewOrientation();
        setConfig(rotation);
        // 检测人脸
        Facepp.Face[] faces = mFacepp.detect(data, width, height, Facepp.IMAGEMODE_NV21);
        if (faces != null) {
            ArrayList<ArrayList> faceVertices = new ArrayList<ArrayList>();
            // 判断是否存在人脸
            if (faces.length > 0) {
                // 初始化姿态角
                mPitch = new float[faces.length];
                mYaw = new float[faces.length];
                mRoll = new float[faces.length];
                // 初始化置信度
                mConfidence = new float[faces.length];
                // 初始化性别年龄数组
                mGender = new String[faces.length];
                mAge = new int[faces.length];
                // 逐个人脸检测
                for (int index = 0; index < faces.length; index++) {
                    if (is106Points)
                        mFacepp.getLandmark(faces[index], Facepp.FPP_GET_LANDMARK106);
                    else
                        mFacepp.getLandmark(faces[index], Facepp.FPP_GET_LANDMARK81);

                    if (is3DPose) {
                        mFacepp.get3DPose(faces[index]);
                    }
                    Facepp.Face face = faces[index];
                    // 人脸属性
                    if (isFaceProperty) {
                        mFacepp.getAgeGender(faces[index]);
                        mGender[index] = "male";
                        if (face.female > face.male) {
                            mGender[index] = "female";
                        }
                        mAge[index] = (int) Math.max(face.age, 1);
                    }
                    // 姿态角
                    mPitch[index] = faces[index].pitch;
                    mYaw[index] = faces[index].yaw;
                    mRoll[index] = faces[index].roll;
                    // 置信度
                    mConfidence[index] = faces[index].confidence;

                    // TODO 计算人脸位置
                    
                }
            } else { // 如果没有人脸时，将姿态角和自信度都置为null，防止上一次的检测结果污染数据
                mPitch = null;
                mYaw = null;
                mRoll = null;
                mConfidence = null;
            }
            // 保存当前的顶点，先销毁以前保存的顶点数据VertexBuffer
            if (mFaceVertices != null) {
                for (int i = 0; i < mFaceVertices.size(); i++) {
                    mFaceVertices.get(i).clear();
                }
                mFaceVertices = null;
            }
            mFaceVertices = faceVertices;
        }
    }


    //------------------------- setter and getter ---------------------------------//

    /**
     * 获取最小人脸检测大小
     * @return
     */
    public int getMinFaceSize() {
        return minFaceSize;
    }

    /**
     * 设置最小人脸检测大小
     * @param minFaceSize
     */
    public void setMinFaceSize(int minFaceSize) {
        this.minFaceSize = minFaceSize;
    }

    /**
     * 获取检测时间
     * @return
     */
    public int getDetectionInterval() {
        return detectionInterval;
    }

    /**
     * 设置检测时间间隔
     * @param detectionInterval
     */
    public void setDetectionInterval(int detectionInterval) {
        this.detectionInterval = detectionInterval;
    }

    /**
     * 获取是否检测一个人脸
     * @return
     */
    public boolean isOneFaceTrackig() {
        return isOneFaceTrackig;
    }

    /**
     * 设置是否只检测一个人脸
     * @param oneFaceTrackig
     */
    public void setOneFaceTrackig(boolean oneFaceTrackig) {
        isOneFaceTrackig = oneFaceTrackig;
    }

    /**
     * 是否区域选择
     * @return
     */
    public boolean isROIDetect() {
        return isROIDetect;
    }

    /**
     * 设置是否区域选择
     * @param ROIDetect
     */
    public void setROIDetect(boolean ROIDetect) {
        isROIDetect = ROIDetect;
    }

    /**
     * 选择区域的宽高比
     * @return
     */
    public float getRoiRatio() {
        return roiRatio;
    }

    /**
     * 设置选择区域的宽高比
     * @param roiRatio
     */
    public void setRoiRatio(float roiRatio) {
        this.roiRatio = roiRatio;
    }

    /**
     * 获取自信度
     * @return
     */
    public float[] getmConfidence() {
        return mConfidence;
    }

    /**
     * 获取X轴姿态角
     * @return
     */
    public float[] getmPitch() {
        return mPitch;
    }

    /**
     * 获取Y轴姿态角
     * @return
     */
    public float[] getmYaw() {
        return mYaw;
    }

    /**
     * 获取z轴姿态角
     * @return
     */
    public float[] getmRoll() {
        return mRoll;
    }

    /**
     * 是否106个监测点
     * @return
     */
    public boolean is106Points() {
        return is106Points;
    }

    /**
     * 设置是否使用106个监测点
     * @param is106Points
     */
    public void setIs106Points(boolean is106Points) {
        this.is106Points = is106Points;
    }

    /**
     * 是否开启年龄属性检测
     * @return
     */
    public boolean isFaceProperty() {
        return isFaceProperty;
    }

    /**
     * 设置是否开启年龄属性检测
     * @param faceProperty
     */
    public void setFaceProperty(boolean faceProperty) {
        isFaceProperty = faceProperty;
    }

    /**
     * 是否3D姿势
     * @return
     */
    public boolean is3DPose() {
        return is3DPose;
    }

    /**
     * 设置是否使用3D姿势
     * @param is3DPose
     */
    public void setIs3DPose(boolean is3DPose) {
        this.is3DPose = is3DPose;
    }

    /**
     * 设置是否打开的是后置摄像头
     * @param backCamera
     */
    public void setBackCamera(boolean backCamera) {
        isBackCamera = backCamera;
    }

    /**
     * 获取性别，需要在isFaceProperty = true时才有效
     * @return
     */
    public String[] getGender() {
        return mGender;
    }

    /**
     * 获取年龄，需要在isFaceProperty = true时才有效
     * @return
     */
    public int[] getAge() {
        return mAge;
    }

    /**
     * 获取人脸的顶点VertexBuffers
     * @return
     */
    public ArrayList<ArrayList> getFaceVertices() {
        return mFaceVertices;
    }
}
