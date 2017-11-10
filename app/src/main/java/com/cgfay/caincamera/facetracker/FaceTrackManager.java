package com.cgfay.caincamera.facetracker;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.bean.Size;
import com.cgfay.caincamera.core.ParamsManager;
import com.cgfay.caincamera.utils.CameraUtils;
import com.cgfay.caincamera.utils.GlUtil;
import com.cgfay.caincamera.utils.SensorEventUtil;
import com.cgfay.caincamera.utils.faceplus.ConUtil;
import com.megvii.facepp.sdk.Facepp;

import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * 人脸关键点检测管理器
 * Created by cain.huang on 2017/11/10.
 */

public class FaceTrackManager {

    private static FaceTrackManager mInstance;

    // 是否开启调试模式
    private boolean isDebug = true;

    // 属性值
    private boolean is3DPose = false;
    private boolean isROIDetect = false;
    private boolean is106Points = true;
    private boolean isBackCamera = false;
    private boolean isFaceProperty = false;
    private boolean isOneFaceTrackig = false;

    // 检测模式
    private int mTrackModel = Facepp.FaceppConfig.DETECTION_MODE_TRACKING;

    // 检测线程
    private HandlerThread mTrackerThread = new HandlerThread("FaceTrackThread");
    private Handler mTrackerHandler;

    // 人脸检测实体
    private Facepp facepp;

    // 最小人脸大小
    private int min_face_size = 200;
    // 检测时间检测
    private int detection_interval = 25;

    // 传感器监听器
    private SensorEventUtil mSensorUtil;

    private float roi_ratio = 0.8f;

    private int Angle;

    // 是否处于检测过程中
    boolean isDetecting = false;

    // 置信度
    float confidence;
    // 姿态角x轴
    float pitch;
    // 姿态角y轴
    float yaw;
    // 姿态角z轴
    float roll;

    int rotation = Angle;

    // 关键点绘制检测
    private FacePointsDrawer mFacePointsDrawer;

    private final float[] mMVPMatrix = GlUtil.IDENTITY_MATRIX;
    private final float[] mProjMatrix = new float[16];
    private final float[] mVMatrix = new float[16];

    // 预览画面宽高
    private int mViewWidth;
    private int mViewHeight;


    private FaceTrackerCallback mFaceTrackerCallback;

    public static FaceTrackManager getInstance() {
        if (mInstance == null) {
            mInstance = new FaceTrackManager();
        }
        return mInstance;
    }

    /**
     * 初始化人脸检测
     * 备注：在相机打开之后调用
     */
    public void initFaceTracking(Context context) {
        if (ParamsManager.canFaceTrack) {

            facepp = new Facepp();

            mSensorUtil = new SensorEventUtil(ParamsManager.context);

            mTrackerThread.start();
            mTrackerHandler = new Handler(mTrackerThread.getLooper());

            ConUtil.acquireWakeLock(context);

            if (CameraUtils.getCamera() != null) {
                Angle = 360 - CameraUtils.getPreviewOrientation();
                if (isBackCamera)
                    Angle = CameraUtils.getPreviewOrientation();

                Size size = CameraUtils.getPreviewSize();
                int width = size.getWidth();
                int height = size.getHeight();

                int left = 0;
                int top = 0;
                int right = width;
                int bottom = height;
                if (isROIDetect) {
                    float line = height * roi_ratio;
                    left = (int) ((width - line) / 2.0f);
                    top = (int) ((height - line) / 2.0f);
                    right = width - left;
                    bottom = height - top;
                }

                String errorCode = facepp.init(context, ConUtil.getFileContent(context,
                        R.raw.megviifacepp_0_4_7_model));
                Facepp.FaceppConfig faceppConfig = facepp.getFaceppConfig();
                faceppConfig.interval = detection_interval;
                faceppConfig.minFaceSize = min_face_size;
                faceppConfig.roi_left = left;
                faceppConfig.roi_top = top;
                faceppConfig.roi_right = right;
                faceppConfig.roi_bottom = bottom;
                if (isOneFaceTrackig)
                    faceppConfig.one_face_tracking = 1;
                else
                    faceppConfig.one_face_tracking = 0;

                faceppConfig.detectionMode = mTrackModel;

                facepp.setFaceppConfig(faceppConfig);
            }
        }

        // 初始化关键点绘制器
        if (ParamsManager.enableDrawingPoints) {
            mFacePointsDrawer = new FacePointsDrawer();
        }
    }


    /**
     * 视图发生变化时调用
     * @param width
     * @param height
     */
    public void onDisplayChanged(int width, int height) {
        mViewWidth = width;
        mViewHeight = height;

        int ratio = 1;
        // 投影矩阵
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    /**
     *  释放资源
     */
    public void release() {
        // 释放检测线程以及Handler回调
        if (mTrackerHandler != null) {
            mTrackerHandler.removeCallbacksAndMessages(null);
            mTrackerHandler = null;
        }
        if (mTrackerThread != null) {
            mTrackerThread.quitSafely();
            mTrackerThread = null;
        }
        // 释放检测实体
        if (facepp != null) {
            facepp.release();
            facepp = null;
        }
    }

    /**
     * SDK角度配置
     * @param rotation 当前的角度
     */
    private void setConfig(int rotation) {
        Facepp.FaceppConfig faceppConfig = facepp.getFaceppConfig();
        if (faceppConfig.rotation != rotation) {
            faceppConfig.rotation = rotation;
            facepp.setFaceppConfig(faceppConfig);
        }
    }

    /**
     * 人脸检测
     * @param data
     */
    public void onFaceTracking(final byte[] data) {
        if (isDetecting) {
            return;
        }
        isDetecting = true;
        if (ParamsManager.canFaceTrack && mTrackerHandler != null) {
            mTrackerHandler.post(new Runnable() {
                @Override
                public void run() {
                    Size size = CameraUtils.getPreviewSize();
                    int width = size.getWidth();
                    int height = size.getHeight();

                    // 调整检测监督
                    long faceDetectTime_action = System.currentTimeMillis();
                    int orientation = mSensorUtil.orientation;
                    if (orientation == 0)
                        rotation = Angle;
                    else if (orientation == 1)
                        rotation = 0;
                    else if (orientation == 2)
                        rotation = 180;
                    else if (orientation == 3)
                        rotation = 360 - Angle;

                    setConfig(rotation);

                    final Facepp.Face[] faces = facepp.detect(data, width, height, Facepp.IMAGEMODE_NV21);
                    if (isDebug) {
                        final long algorithmTime = System.currentTimeMillis() - faceDetectTime_action;
                        Log.d("onFaceTracking", "track time = " + algorithmTime);
                    }
                    if (faces != null) {
                        // 所有人脸关键点集合
                        ArrayList<ArrayList> facePoints = new ArrayList<ArrayList>();
                        confidence = 0.0f;
                        if (faces.length >= 0) {
                            for (int index = 0; index < faces.length; index++) {
                                if (is106Points)
                                    facepp.getLandmark(faces[index], Facepp.FPP_GET_LANDMARK106);
                                else
                                    facepp.getLandmark(faces[index], Facepp.FPP_GET_LANDMARK81);

                                if (is3DPose) {
                                    facepp.get3DPose(faces[index]);
                                }

                                Facepp.Face face = faces[index];

                                if (isFaceProperty) {
                                    long time_AgeGender_action = System.currentTimeMillis();
                                    facepp.getAgeGender(faces[index]);

                                    if (isDebug) {
                                        Log.d("onFaceTracking", "getAgeGenderTime: "
                                                + (System.currentTimeMillis() - time_AgeGender_action));

                                        String gender = "man";
                                        if (face.female > face.male)
                                            gender = "woman";
                                        Log.d("onFaceTracking", "age: "
                                                + (int) Math.max(face.age, 1)
                                                + "\ngender: " + gender);
                                    }
                                }

                                // 设置姿态角以及置信度
                                pitch = faces[index].pitch;
                                yaw = faces[index].yaw;
                                roll = faces[index].roll;
                                confidence = faces[index].confidence;

                                // 调整宽高
                                if (orientation == 1 || orientation == 2) {
                                    width = size.getHeight();
                                    height = size.getWidth();
                                }

                                // 一个人脸的关键点
                                ArrayList<FloatBuffer> onePoints = new ArrayList<FloatBuffer>();
                                for (int i = 0; i < faces[index].points.length; i++) {
                                    float x = (faces[index].points[i].x / height) * 2 - 1;
                                    if (isBackCamera)
                                        x = -x;
                                    float y = 1 - (faces[index].points[i].y / width) * 2;
                                    float[] pointf = new float[] { x, y, 0.0f };
                                    if (orientation == 1)
                                        pointf = new float[] { -y, x, 0.0f };
                                    if (orientation == 2)
                                        pointf = new float[] { y, -x, 0.0f };
                                    if (orientation == 3)
                                        pointf = new float[] { -x, -y, 0.0f };

                                    FloatBuffer fb = GlUtil.createFloatBuffer(pointf);
                                    onePoints.add(fb);
                                }
                                facePoints.add(onePoints);
                            }

                            if (ParamsManager.enableDrawingPoints) {
                                synchronized (mFacePointsDrawer) {
                                    mFacePointsDrawer.points = facePoints;
                                }
                            }

                            if (mFaceTrackerCallback != null) {
                                mFaceTrackerCallback.onTrackingFinish(true, facePoints);
                            }
                        } else {
                            pitch = 0.0f;
                            yaw = 0.0f;
                            roll = 0.0f;

                            if (mFaceTrackerCallback != null) {
                                mFaceTrackerCallback.onTrackingFinish(false, null);
                            }
                        }
                    } else {
                        if (mFaceTrackerCallback != null) {
                            mFaceTrackerCallback.onTrackingFinish(false, null);
                        }
                    }
                    isDetecting = false;
                }
            });
        } else {
            // 人脸关键点回调
            if (mFaceTrackerCallback != null) {
                mFaceTrackerCallback.onTrackingFinish(false, null);
            }
        }
    }

    /**
     * 绘制关键点
     */
    public void drawTrackPoints() {
        // 回执关键点
        Matrix.setLookAtM(mVMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1f, 0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);

        GLES30.glViewport(0, 0, mViewWidth, mViewHeight);
        mFacePointsDrawer.draw(mMVPMatrix);
    }

    /**
     * 设置人脸关键点检测器回调
     * @param callback
     */
    public void setFaceCallback(FaceTrackerCallback callback) {
        mFaceTrackerCallback = callback;
    }


    //--------------------------- setter and getter ----------------------------

    public void setIs3DPose(boolean is3DPose) {
        this.is3DPose = is3DPose;
    }

    public void setROIDetect(boolean ROIDetect) {
        isROIDetect = ROIDetect;
    }

    public void setIs106Points(boolean is106Points) {
        this.is106Points = is106Points;
    }

    public void setBackCamera(boolean backCamera) {
        isBackCamera = backCamera;
    }

    public void setFaceProperty(boolean faceProperty) {
        isFaceProperty = faceProperty;
    }

    public void setOneFaceTrackig(boolean oneFaceTrackig) {
        isOneFaceTrackig = oneFaceTrackig;
    }

    // 设置检测模式
    public void setTrackModel(int trackModel) {
        this.mTrackModel = trackModel;
    }

    // 设置最小人脸大小
    public void setMin_face_size(int min_face_size) {
        this.min_face_size = min_face_size;
    }

    // 设置检测间隔
    public void setDetection_interval(int detection_interval) {
        this.detection_interval = detection_interval;
    }

    // 置信度
    public float getConfidence() {
        return confidence;
    }

    // 姿态角x轴
    public float getPitch() {
        return pitch;
    }

    // 姿态角y轴
    public float getYaw() {
        return yaw;
    }

    // 姿态角z轴
    public float getRoll() {
        return roll;
    }
}
