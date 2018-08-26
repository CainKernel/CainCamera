package com.cgfay.facedetectlibrary.engine;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.cgfay.facedetectlibrary.R;
import com.cgfay.facedetectlibrary.bean.FaceInfo;
import com.cgfay.facedetectlibrary.listener.FaceTrackerCallback;
import com.cgfay.facedetectlibrary.utils.ConUtil;
import com.cgfay.facedetectlibrary.utils.FaceppConstraints;
import com.cgfay.facedetectlibrary.utils.SensorEventUtil;
import com.megvii.facepp.sdk.Facepp;
import com.megvii.licensemanager.sdk.LicenseManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * 人脸检测器
 */
public final class FaceTracker {

    private static final String TAG = "FaceTracker";
    private static final boolean VERBOSE = false;

    private final Object mSyncFence = new Object();

    // 人脸检测参数
    private FaceTrackParam mFaceTrackParam;

    // 检测线程
    private TrackerThread mTrackerThread;

    private static class FaceTrackerHolder {
        private static FaceTracker instance = new FaceTracker();
    }

    private FaceTracker() {
        mFaceTrackParam = FaceTrackParam.getInstance();
    }

    public static FaceTracker getInstance() {
        return FaceTrackerHolder.instance;
    }

    /**
     * 检测回调
     * @param callback
     * @return
     */
    public FaceTrackerBuilder setFaceCallback(FaceTrackerCallback callback) {
        return new FaceTrackerBuilder(this, callback);
    }

    /**
     * 准备检测器
     */
    void initTracker() {
        synchronized (mSyncFence) {
            mTrackerThread = new TrackerThread("FaceTrackerThread");
            mTrackerThread.start();
        }
    }

    /**
     * 初始化人脸检测
     * @param context       上下文
     * @param orientation   图像角度
     * @param width         图像宽度
     * @param height        图像高度
     */
    public void prepareFaceTracker(Context context, int orientation, int width, int height) {
        synchronized (mSyncFence) {
            if (mTrackerThread != null) {
                mTrackerThread.prepareFaceTracker(context, orientation, width, height);
            }
        }
    }

    /**
     * 检测人脸
     * @param data
     * @param width
     * @param height
     */
    public void trackFace(byte[] data, int width, int height) {
        synchronized (mSyncFence) {
            if (mTrackerThread != null) {
                mTrackerThread.trackFace(data, width, height);
            }
        }
    }

    /**
     * 销毁检测器
     */
    public void destroyTracker() {
        synchronized (mSyncFence) {
            mTrackerThread.quitSafely();
        }
    }

    /**
     * 是否后置摄像头
     * @param backCamera
     * @return
     */
    public FaceTracker setBackCamera(boolean backCamera) {
        mFaceTrackParam.isBackCamera = backCamera;
        return this;
    }

    /**
     * 是否允许3D姿态角
     * @param enable
     * @return
     */
    public FaceTracker enable3DPose(boolean enable) {
        mFaceTrackParam.enable3DPose = enable;
        return this;
    }

    /**
     * 是否允许区域检测
     * @param enable
     * @return
     */
    public FaceTracker enableROIDetect(boolean enable) {
        mFaceTrackParam.enableROIDetect = enable;
        return this;
    }

    /**
     * 是否允许106个关键点
     * @param enable
     * @return
     */
    public FaceTracker enable106Points(boolean enable) {
        mFaceTrackParam.enable106Points = enable;
        return this;
    }

    /**
     * 是否允许多人脸检测
     * @param enable
     * @return
     */
    public FaceTracker enableMultiFace(boolean enable) {
        mFaceTrackParam.enableMultiFace = enable;
        return this;
    }

    /**
     * 是否允许人脸年龄检测
     * @param enable
     * @return
     */
    public FaceTracker enableFaceProperty(boolean enable) {
        mFaceTrackParam.enableFaceProperty = enable;
        return this;
    }

    /**
     * 最小检测人脸大小
     * @param size
     * @return
     */
    public FaceTracker minFaceSize(int size) {
        mFaceTrackParam.minFaceSize = size;
        return this;
    }

    /**
     * 检测时间间隔
     * @param interval
     * @return
     */
    public FaceTracker detectInterval(int interval) {
        mFaceTrackParam.detectInterval = interval;
        return this;
    }

    /**
     * 检测模式
     * @param mode
     * @return
     */
    public FaceTracker trackMode(int mode) {
        mFaceTrackParam.trackMode = mode;
        return this;
    }

    /**
     * Face++SDK联网请求验证
     */
    public static void requestFaceNetwork(Context context) {
        if (Facepp.getSDKAuthType(ConUtil.getFileContent(context, R.raw
                .megviifacepp_0_4_7_model)) == 2) {// 非联网授权
            FaceTrackParam.getInstance().canFaceTrack = true;
            return;
        }
        final LicenseManager licenseManager = new LicenseManager(context);
        licenseManager.setExpirationMillis(Facepp.getApiExpirationMillis(context,
                ConUtil.getFileContent(context, R.raw.megviifacepp_0_4_7_model)));
        String uuid = ConUtil.getUUIDString(context);
        long apiName = Facepp.getApiName();
        licenseManager.setAuthTimeBufferMillis(0);
        licenseManager.takeLicenseFromNetwork(uuid, FaceppConstraints.API_KEY, FaceppConstraints.API_SECRET, apiName,
                LicenseManager.DURATION_30DAYS, "Landmark", "1", true,
                new LicenseManager.TakeLicenseCallback() {
                    @Override
                    public void onSuccess() {
                        if (VERBOSE) {
                            Log.d(TAG, "success to register license!");
                        }
                        FaceTrackParam.getInstance().canFaceTrack = true;
                    }

                    @Override
                    public void onFailed(int i, byte[] bytes) {
                        if (VERBOSE) {
                            Log.d(TAG, "Failed to register license!");
                        }
                        FaceTrackParam.getInstance().canFaceTrack = false;
                    }
                });
    }


    /**
     * 检测线程
     */
    private static class TrackerThread extends Thread {

        // 人脸检测实体
        private Facepp facepp;
        // 传感器监听器
        private SensorEventUtil mSensorUtil;

        private Looper mLooper;
        private @Nullable Handler mHandler;

        public TrackerThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            Looper.prepare();
            synchronized (this) {
                mLooper = Looper.myLooper();
                notifyAll();
                mHandler = new Handler(mLooper);
            }
            Looper.loop();
            synchronized (this) {
                release();
                mHandler.removeCallbacksAndMessages(null);
                mHandler = null;
            }
        }

        /**
         * 安全退出
         * @return
         */
        public boolean quitSafely() {
            Looper looper = getLooper();
            if (looper != null) {
                looper.quitSafely();
                return true;
            }
            return false;
        }

        /**
         * 获取Looper
         * @return
         */
        public Looper getLooper() {
            if (!isAlive()) {
                return null;
            }
            synchronized (this) {
                while (isAlive() && mLooper == null) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
            return mLooper;
        }

        /**
         * 获取线程Handler
         * @return
         */
        public Handler getThreadHandler() {
            return mHandler;
        }

        /**
         * 初始化人脸检测
         * @param context       上下文
         * @param orientation   图像角度
         * @param width         图像宽度
         * @param height        图像高度
         */
        public void prepareFaceTracker(final Context context, final int orientation,
                                       final int width, final int height) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    internalPrepareFaceTracker(context, orientation, width, height);
                }
            });
        }

        /**
         * 检测人脸
         * @param data      图像数据， NV21 或者 RGBA格式
         * @param width     图像宽度
         * @param height    图像高度
         * @return          是否检测成功
         */
        public void trackFace(final byte[] data, final int width, final int height) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    internalTrackFace(data, width, height);
                }
            });
        }


        /**
         * 释放资源
         */
        private void release() {
            if (facepp != null) {
                facepp.release();
                facepp = null;
            }
        }

        /**
         * 初始化人脸检测
         * @param context       上下文
         * @param orientation   图像角度，预览时设置相机的角度，如果是静态图片，则为0
         * @param width         图像宽度
         * @param height        图像高度
         */
        private synchronized void internalPrepareFaceTracker(Context context, int orientation, int width, int height) {
            FaceTrackParam faceTrackParam = FaceTrackParam.getInstance();
            if (!faceTrackParam.canFaceTrack) {
                return;
            }
            release();
            facepp = new Facepp();
            if (mSensorUtil == null) {
                mSensorUtil = new SensorEventUtil(context);
            }
            ConUtil.acquireWakeLock(context);
            if (!faceTrackParam.previewTrack) {
                faceTrackParam.rotateAngle = orientation;
            } else {
                faceTrackParam.rotateAngle = faceTrackParam.isBackCamera ? orientation : 360 - orientation;
            }

            int left = 0;
            int top = 0;
            int right = width;
            int bottom = height;
            // 限定检测区域
            if (faceTrackParam.enableROIDetect) {
                float line = height * faceTrackParam.roiRatio;
                left = (int) ((width - line) / 2.0f);
                top = (int) ((height - line) / 2.0f);
                right = width - left;
                bottom = height - top;
            }

            facepp.init(context, ConUtil.getFileContent(context, R.raw.megviifacepp_0_4_7_model));
            Facepp.FaceppConfig faceppConfig = facepp.getFaceppConfig();
            faceppConfig.interval = faceTrackParam.detectInterval;
            faceppConfig.minFaceSize = faceTrackParam.minFaceSize;
            faceppConfig.roi_left = left;
            faceppConfig.roi_top = top;
            faceppConfig.roi_right = right;
            faceppConfig.roi_bottom = bottom;
            faceppConfig.one_face_tracking = faceTrackParam.enableMultiFace ? 0 : 1;
            faceppConfig.detectionMode = faceTrackParam.trackMode;
            facepp.setFaceppConfig(faceppConfig);
        }

        /**
         * 检测人脸
         * @param data      图像数据，预览时为NV21，静态图片则为RGBA格式
         * @param width     图像宽度
         * @param height    图像高度
         * @return          是否检测成功
         */
        private synchronized void internalTrackFace(byte[] data, int width, int height) {
            FaceTrackParam faceTrackParam = FaceTrackParam.getInstance();
            if (!faceTrackParam.canFaceTrack || facepp == null) {
                if (faceTrackParam.trackerCallback != null) {
                    faceTrackParam.trackerCallback.onTrackingFinish(false, null);
                }
                return;
            }

            // 调整检测监督
            long faceDetectTime_action = System.currentTimeMillis();
            // 获取设备旋转
            int orientation = faceTrackParam.previewTrack ? mSensorUtil.orientation : 0;
            int rotation = 0;
            if (orientation == 0) {         // 0
                rotation = faceTrackParam.rotateAngle;
            } else if (orientation == 1) {  // 90
                rotation = 0;
            } else if (orientation == 2) {  // 270
                rotation = 180;
            } else if (orientation == 3) {  // 180
                rotation = 360 - faceTrackParam.rotateAngle;
            }
            // 设置旋转角度
            Facepp.FaceppConfig faceppConfig = facepp.getFaceppConfig();
            if (faceppConfig.rotation != rotation) {
                faceppConfig.rotation = rotation;
                facepp.setFaceppConfig(faceppConfig);
            }

            // 人脸检测
            final Facepp.Face[] faces = facepp.detect(data, width, height,
                    faceTrackParam.previewTrack ? Facepp.IMAGEMODE_NV21 : Facepp.IMAGEMODE_RGBA);

            // 计算检测时间
            if (VERBOSE) {
                final long algorithmTime = System.currentTimeMillis() - faceDetectTime_action;
                Log.d("onFaceTracking", "track time = " + algorithmTime);
            }

            // 调试用的人脸关键点
            ArrayList<ArrayList> debugPoints = new ArrayList<ArrayList>();
            // 计算人脸关键点
            if (faces != null) {
                ArrayList<FaceInfo> faceInfoArray = new ArrayList<>();
                if (faces.length > 0) {
                    for (int index = 0; index < faces.length; index++) {
                        FaceInfo faceInfo = new FaceInfo();
                        if (faceTrackParam.enable106Points) {
                            facepp.getLandmark(faces[index], Facepp.FPP_GET_LANDMARK106);
                        } else {
                            facepp.getLandmark(faces[index], Facepp.FPP_GET_LANDMARK81);
                        }
                        if (faceTrackParam.enable3DPose) {
                            facepp.get3DPose(faces[index]);
                        }
                        Facepp.Face face = faces[index];

                        // 是否检测性别年龄属性
                        if (faceTrackParam.enableFaceProperty) {
                            facepp.getAgeGender(face);
                            faceInfo.gender = face.female > face.male ? FaceInfo.GENDER_WOMAN
                                    : FaceInfo.GENDER_MAN;
                            faceInfo.age = Math.max(face.age, 1);
                        }

                        // 姿态角和置信度
                        faceInfo.pitch = face.pitch;
                        faceInfo.yaw = face.yaw;
                        faceInfo.roll = face.roll;
                        faceInfo.confidence = face.confidence;

                        // 预览状态下，宽高交换
                        if (faceTrackParam.previewTrack) {
                            if (orientation == 1 || orientation == 2) {
                                int temp = width;
                                width = height;
                                height = temp;
                            }
                        }

                        // 获取一个人的关键点坐标
                        ArrayList<FloatBuffer> onePoints = new ArrayList<FloatBuffer>();
                        float[] vertexPoints = new float[face.points.length * 2];
                        float[] texturePoints = new float[face.points.length * 2];
                        float[] cartesianPoints = new float[face.points.length * 2];
                        for (int i = 0; i < face.points.length; i++) {
                            float x = (face.points[i].x / height) * 2 - 1;
                            float y = 1 - (face.points[i].y / width) * 2;
                            if (faceTrackParam.previewTrack && faceTrackParam.isBackCamera) {
                                x = -x;
                            }
                            float[] point = new float[] {x, y};
                            if (orientation == 1) {
                                point = new float[] {-y, x};
                            } else if (orientation == 2) {
                                point = new float[] {y, -x};
                            } else if (orientation == 3) {
                                point = new float[] {-x, -y};
                            }
                            // 调试的顶点坐标
                            onePoints.add(createFloatBuffer(point));
                            // 顶点坐标
                            vertexPoints[2 * i] = -point[0];
                            vertexPoints[2 * i + 1] = point[1];
                            // 纹理坐标
                            texturePoints[2 * i] = (1 + vertexPoints[2 * i]) / 2.0f;
                            texturePoints[2 * i + 1] = (1 + vertexPoints[2 * i + 1]) / 2.0f;
                            // 笛卡尔坐标
                            cartesianPoints[2 * i] = texturePoints[2 * i] * height;
                            cartesianPoints[2 * i + 1] = texturePoints[2 * i + 1] * width;
                        }
                        faceInfo.vertexPoints = vertexPoints;
                        faceInfo.texturePoints = texturePoints;
                        faceInfo.cartesianPoint = cartesianPoints;
                        faceInfoArray.add(faceInfo);
                        debugPoints.add(onePoints);
                    }
                }
                // 添加到点计算工具中
                FacePointsEngine.newInstance().addFacePoints(faceInfoArray);
            }
            // 检测完成回调
            if (faceTrackParam.trackerCallback != null) {
                faceTrackParam.trackerCallback.onTrackingFinish(faces != null && faces.length > 0, debugPoints);
            }
        }

        /**
         * 创建顶点缓冲
         * @param coords
         * @return
         */
        private FloatBuffer createFloatBuffer(float[] coords) {
            ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * 4);
            bb.order(ByteOrder.nativeOrder());
            FloatBuffer fb = bb.asFloatBuffer();
            fb.put(coords);
            fb.position(0);
            return fb;
        }
    }

}
