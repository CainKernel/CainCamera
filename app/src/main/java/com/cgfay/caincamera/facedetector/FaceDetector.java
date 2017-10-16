package com.cgfay.caincamera.facedetector;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.core.ParamsManager;
import com.cgfay.caincamera.utils.CameraUtils;
import com.cgfay.caincamera.utils.SensorEventUtil;
import com.cgfay.caincamera.utils.faceplus.ConUtil;
import com.megvii.facepp.sdk.Facepp;

import java.util.ArrayList;


/**
 * 人脸检测
 * Created by cain on 2017/8/15.
 */
public class FaceDetector {

    public FaceDetector() {}

    private FaceDetectorHandler mDetectorHandler;
    private HandlerThread mHandlerThread;

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
    private ArrayList<ArrayList<float[]>> mFacePoints;

    private DetectorCallback mDetectorCallback;

    /**
     * 创建线程
     */
    public void create() {
        if (mHandlerThread == null) {
            mHandlerThread = new HandlerThread("Face Detector");
            mHandlerThread.start();
        }
        if (mDetectorHandler == null) {
            mDetectorHandler = new FaceDetectorHandler(mHandlerThread.getLooper());
        }
    }

    /**
     * 销毁线程
     */
    public void destory() {
        // 当Handler不存在时，需要销毁线程，否则可能会出现重新打开不了的情况
        if (mDetectorHandler == null) {
            if (mHandlerThread != null) {
                mHandlerThread.quitSafely();
                try {
                    mHandlerThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mHandlerThread = null;
            }
            return;
        }
        mDetectorHandler.sendEmptyMessage(FaceDetectorHandler.MSG_DESTORY);
        mHandlerThread.quitSafely();
        try {
            mHandlerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mHandlerThread = null;
        mDetectorHandler = null;
    }

    /**
     * 初始化人脸配置
     * @param width
     * @param height
     */
    public void initFaceConfig(int width, int height) {
        if (mDetectorHandler != null) {
            mDetectorHandler.sendMessage(mDetectorHandler
                    .obtainMessage(FaceDetectorHandler.MSG_INIT_CONFIG, width, height));
        }
    }

    /**
     * 人脸检测
     * @param data
     * @param width
     * @param height
     */
    public void faceDetecting(byte[] data, int width, int height) {
        if (mDetectorHandler != null) {
            mDetectorHandler.sendMessageAtFrontOfQueue(mDetectorHandler
                    .obtainMessage(FaceDetectorHandler.MSG_FACE_DETECTING, width, height, data));
        }
    }

    /**
     * 添加检测回调
     * @param callback
     */
    public void addDetectorCallback(DetectorCallback callback) {
        mDetectorCallback = callback;
    }

    private class FaceDetectorHandler extends Handler {

        static final int MSG_INIT_CONFIG = 0x01;
        static final int MSG_FACE_DETECTING = 0x02;
        static final int MSG_DESTORY = 0x03;
        private boolean isAvailable = false; // 人脸检测是否处于可用状态

        public FaceDetectorHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT_CONFIG:
                    initConfig(msg.arg1, msg.arg2);
                    break;

                case MSG_FACE_DETECTING:
//                    long time = System.currentTimeMillis();
                    // 如果没有创建检测对象
                    if (mFacepp == null) {
                        initConfig(msg.arg1, msg.arg2);
                    }
                    faceDetecting((byte[]) msg.obj, msg.arg1, msg.arg2);
//                    Log.d("faceDetecting", "time: " + (System.currentTimeMillis() - time));
                    break;

                case MSG_DESTORY:
                    if (mFacepp != null) {
                        mFacepp.release();
                        mFacepp = null;
                    }
                    isAvailable = false;
                    break;
            }
        }

        /**
         * 初始化
         * @param width
         * @param height
         */
        private void initConfig(int width, int height) {
            if (mSensorUtil == null) {
                mSensorUtil = new SensorEventUtil(ParamsManager.context);
            }
            if (mFacepp == null) {
                mFacepp = new Facepp();
            }
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

            mFacepp.init(ParamsManager.context, ConUtil.getFileContent(ParamsManager.context,
                    R.raw.megviifacepp_0_4_7_model));
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
            // 识别模式，常规、鲁棒性还是快速(实际测试，Normal效果会好很多)
            faceppConfig.detectionMode = Facepp.FaceppConfig.DETECTION_MODE_NORMAL;
            mFacepp.setFaceppConfig(faceppConfig);
            isAvailable = true;
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
            if (!isAvailable) {
                return;
            }
            // 配置旋转角度
            int orientation = mSensorUtil.orientation;
            if (orientation == 0)
                rotation = 360 - CameraUtils.getPreviewOrientation();
            else if (orientation == 1)
                rotation = 0;
            else if (orientation == 2)
                rotation = 180;
            else if (orientation == 3)
                rotation = CameraUtils.getPreviewOrientation();
            setConfig(rotation);
            if (mFacepp == null) {
                return;
            }
            // 检测人脸
            Facepp.Face[] faces = mFacepp.detect(data, width, height, Facepp.IMAGEMODE_NV21);
            if (faces != null) {
                ArrayList<ArrayList<float[]>> facePoints = new ArrayList<ArrayList<float[]>>();
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

                        // 计算人脸关键点
                        if (orientation == 1 || orientation == 2) {
                            int temp = width;
                            width = height;
                            height = temp;
                        }
                        ArrayList<float[]> points = new ArrayList<float[]>();
                        // TODO 两个边角交界的角度出现错乱，需要调整
                        for (int i = 0; i < faces[index].points.length; i++) {
                            float x = (faces[index].points[i].x / height) * 2 - 1;
                            if (isBackCamera)
                                x = -x;
                            float y = 1 - (faces[index].points[i].y / width) * 2;
                            float[] point = new float[] { -x, y, 0.0f };
                            if (orientation == 1) {
                                point = new float[]{ y, x, 0.0f };
                            } else if (orientation == 2) {
                                point = new float[]{ -y, -x, 0.0f };
                            } else if (orientation == 3) {
                                point = new float[] { x, -y, 0.0f };
                            }
                            points.add(point);
                        }
                        facePoints.add(points);
                    }
                } else { // 如果没有人脸时，将姿态角和自信度都置为null，防止上一次的检测结果污染数据
                    mPitch = null;
                    mYaw = null;
                    mRoll = null;
                    mConfidence = null;
                }
                // 判断是否存在关键点，不存在则清空数据
                if (facePoints.size() > 0) {
                    mFacePoints = facePoints;
                } else {
                    mFacePoints = null;
                }
                // 检测完成回调
                if (mDetectorCallback != null) {
                    mDetectorCallback.onTrackingFinish(faces.length > 0);
                }
            }
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
    public ArrayList<ArrayList<float[]>> getFacePoints() {
        return mFacePoints;
    }
}
