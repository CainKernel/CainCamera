package com.cgfay.caincamera.activity.facetrack;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.cgfay.caincamera.R;
import com.cgfay.utilslibrary.Size;
import com.cgfay.cainfilter.core.ParamsManager;
import com.cgfay.caincamera.facetracker.FacePointsDrawer;
import com.cgfay.cainfilter.filter.camera.CameraFilter;
import com.cgfay.cainfilter.utils.GlUtil;
import com.cgfay.caincamera.utils.facepp.ConUtil;
import com.cgfay.caincamera.utils.facepp.Util;
import com.cgfay.caincamera.utils.facepp.SensorEventUtil;
import com.cgfay.utilslibrary.CameraUtils;
import com.megvii.facepp.sdk.Facepp;
import com.megvii.licensemanager.sdk.LicenseManager;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Face++ 人脸检测
 * 这是使用了GLSurfaceView实现渲染的
 * 人脸检测SDK本身使用的GLSurfaceView进行渲染，人脸关键点检测的时间在20ms以内
 * Created by cain.huang on 2017/7/27.
 */

public class FaceTrackActivity extends AppCompatActivity implements Camera.PreviewCallback,
        GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    private boolean is3DPose, isDebug, isROIDetect, is106Points, isBackCamera, isFaceProperty,
            isOneFaceTrackig;

    private boolean isTiming = false;
    private int printTime = 31;
    private GLSurfaceView mGlSurfaceView;

    private HandlerThread mTrackerThread = new HandlerThread("facepp");
    private Handler mTrackerHandler;

    private Facepp facepp;
    private int min_face_size = 200;
    private int detection_interval = 25;
    private SensorEventUtil sensorUtil;
    private float roi_ratio = 0.8f;

    // 相机输入流滤镜
    private CameraFilter mCameraFilter;

    boolean isPreviewing = false;

    private boolean canFaceTrack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenManager.initialize(this);
        setContentView(R.layout.activity_face_track);
        requestFaceNetwork();
    }

    /**
     * Face++SDK联网请求
     */
    private void requestFaceNetwork() {
        if (Facepp.getSDKAuthType(ConUtil.getFileContent(this, R.raw
                .megviifacepp_0_4_7_model)) == 2) {// 非联网授权
            canFaceTrack = true;
            return;
        }
        final LicenseManager licenseManager = new LicenseManager(this);
        licenseManager.setExpirationMillis(Facepp.getApiExpirationMillis(this, ConUtil.getFileContent(this, R.raw
                .megviifacepp_0_4_7_model)));

        String uuid = ConUtil.getUUIDString(this);
        long apiName = Facepp.getApiName();

        licenseManager.setAuthTimeBufferMillis(0);

        licenseManager.takeLicenseFromNetwork(uuid, Util.API_KEY, Util.API_SECRET, apiName,
                LicenseManager.DURATION_30DAYS, "Landmark", "1", true, new LicenseManager.TakeLicenseCallback() {
                    @Override
                    public void onSuccess() {
                        canFaceTrack = true;
                        init();
                    }

                    @Override
                    public void onFailed(int i, byte[] bytes) {
                        Log.d("LicenseManager", "Failed to register license!");
                        canFaceTrack = false;
                        init();
                    }
                });
    }

    private void init() {
        if (canFaceTrack) {
            if (android.os.Build.MODEL.equals("PLK-AL10"))
                printTime = 50;

            is3DPose = getIntent().getBooleanExtra("is3DPose", false);
            isDebug = true;
            isROIDetect = getIntent().getBooleanExtra("ROIDetect", false);
            is106Points = getIntent().getBooleanExtra("is106Points", false);
            isBackCamera = getIntent().getBooleanExtra("isBackCamera", false);
            isFaceProperty = getIntent().getBooleanExtra("isFaceProperty", false);
            isOneFaceTrackig = getIntent().getBooleanExtra("isOneFaceTrackig", false);

            min_face_size = getIntent().getIntExtra("faceSize", min_face_size);
            detection_interval = getIntent().getIntExtra("interval", detection_interval);

            facepp = new Facepp();

            sensorUtil = new SensorEventUtil(this);

            mTrackerThread.start();
            mTrackerHandler = new Handler(mTrackerThread.getLooper());
        }

        mGlSurfaceView = (GLSurfaceView) findViewById(R.id.opengl_layout_surfaceview);
        mGlSurfaceView.setEGLContextClientVersion(3);
        mGlSurfaceView.setRenderer(this);
        mGlSurfaceView.setRenderMode(mGlSurfaceView.RENDERMODE_WHEN_DIRTY);
        mGlSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoFocus();
            }
        });

    }

    /**
     *  自动对焦
     */
    private void autoFocus() {
        if (CameraUtils.getCamera() != null && isBackCamera) {
            CameraUtils.getCamera().cancelAutoFocus();
            Camera.Parameters parameters = CameraUtils.getCamera().getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            CameraUtils.getCamera().setParameters(parameters);
            CameraUtils.getCamera().autoFocus(null);
        }
    }

    // 通过屏幕参数、相机预览尺寸计算布局参数
    public RelativeLayout.LayoutParams getLayoutParam(int cameraWidth, int cameraHeight) {
        float scale = cameraWidth * 1.0f / cameraHeight;

        int layout_width = ScreenManager.mWidth;
        int layout_height = (int) (layout_width * scale);

        if (ScreenManager.mWidth >= ScreenManager.mHeight) {
            layout_height = ScreenManager.mHeight;
            layout_width = (int) (layout_height / scale);
        }

        RelativeLayout.LayoutParams layout_params = new RelativeLayout.LayoutParams(
                layout_width, layout_height);
        layout_params.addRule(RelativeLayout.CENTER_HORIZONTAL);// 设置照相机水平居中

        return layout_params;
    }

    private int Angle;

    @Override
    protected void onResume() {
        super.onResume();
        ConUtil.acquireWakeLock(this);

        CameraUtils.calculateCameraPreviewOrientation(this);
        CameraUtils.openCamera(this, CameraUtils.DESIRED_PREVIEW_FPS);

        if (CameraUtils.getCamera() != null && canFaceTrack) {
            Angle = 360 - CameraUtils.getPreviewOrientation();
            if (isBackCamera)
                Angle = CameraUtils.getPreviewOrientation();

            Size size = CameraUtils.getPreviewSize();
            int width = size.getWidth();
            int height = size.getHeight();

            // 设置大小
            mGlSurfaceView.setLayoutParams(getLayoutParam(width, height));

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

            String errorCode = facepp.init(this, ConUtil.getFileContent(this, R.raw.megviifacepp_0_4_7_model));
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
//            String[] array = getResources().getStringArray(R.array.trackig_mode_array);
//            if (trackModel.equals(array[0]))
//                faceppConfig.detectionMode = Facepp.FaceppConfig.DETECTION_MODE_TRACKING;
//            else if (trackModel.equals(array[1]))
//                faceppConfig.detectionMode = Facepp.FaceppConfig.DETECTION_MODE_TRACKING_ROBUST;
//            else if (trackModel.equals(array[2]))
//                faceppConfig.detectionMode = Facepp.FaceppConfig.DETECTION_MODE_TRACKING_FAST;
            faceppConfig.detectionMode = Facepp.FaceppConfig.DETECTION_MODE_TRACKING;

            facepp.setFaceppConfig(faceppConfig);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPreviewing = false;
        CameraUtils.releaseCamera();
        ConUtil.releaseWakeLock();

        if (mCameraFilter != null) {
            mCameraFilter.release();
            mCameraFilter = null;
        }

        timeHandle.removeMessages(0);

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        facepp.release();
    }

    private void setConfig(int rotation) {
        Facepp.FaceppConfig faceppConfig = facepp.getFaceppConfig();
        if (faceppConfig.rotation != rotation) {
            faceppConfig.rotation = rotation;
            facepp.setFaceppConfig(faceppConfig);
        }
    }

    private int mTextureID = -1;
    private SurfaceTexture mSurface;
    private FacePointsDrawer mFacePointsDrawer;

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }

    boolean isSuccess = false;
    float confidence;
    float pitch, yaw, roll;
    long time_AgeGender_end = 0;
    String AttriButeStr = "";
    int rotation = Angle;
    @Override
    public void onPreviewFrame(final byte[] data, Camera camera) {
        if (isSuccess || !isPreviewing) {
            return;
        }
        isSuccess = true;
        if (canFaceTrack && mTrackerHandler != null) {
            mTrackerHandler.post(new Runnable() {
                @Override
                public void run() {
                    Size size = CameraUtils.getPreviewSize();
                    int width = size.getWidth();
                    int height = size.getHeight();

                    long faceDetectTime_action = System.currentTimeMillis();
                    int orientation = sensorUtil.orientation;
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
                    final long algorithmTime = System.currentTimeMillis() - faceDetectTime_action;
                    Log.d("onPreviewFrame", "track time = " + algorithmTime);

                    if (faces != null) {
                        ArrayList<ArrayList> pointsOpengl = new ArrayList<ArrayList>();
                        confidence = 0.0f;

                        if (faces.length >= 0) {
                            for (int c = 0; c < faces.length; c++) {
                                if (is106Points)
                                    facepp.getLandmark(faces[c], Facepp.FPP_GET_LANDMARK106);
                                else
                                    facepp.getLandmark(faces[c], Facepp.FPP_GET_LANDMARK81);

                                if (is3DPose) {
                                    facepp.get3DPose(faces[c]);
                                }

                                Facepp.Face face = faces[c];

                                if (isFaceProperty) {
                                    long time_AgeGender_action = System.currentTimeMillis();
                                    facepp.getAgeGender(faces[c]);
                                    time_AgeGender_end = System.currentTimeMillis() - time_AgeGender_action;
                                    String gender = "man";
                                    if (face.female > face.male)
                                        gender = "woman";
                                    AttriButeStr = "\nage: " + (int) Math.max(face.age, 1) + "\ngender: " + gender;
                                }

                                pitch = faces[c].pitch;
                                yaw = faces[c].yaw;
                                roll = faces[c].roll;
                                confidence = faces[c].confidence;

                                if (orientation == 1 || orientation == 2) {

                                    width = size.getHeight();
                                    height = size.getWidth();

                                }

                                ArrayList<FloatBuffer> triangleVBList = new ArrayList<FloatBuffer>();
                                for (int i = 0; i < faces[c].points.length; i++) {
                                    float x = (faces[c].points[i].x / height) * 2 - 1;
                                    if (isBackCamera)
                                        x = -x;
                                    float y = 1 - (faces[c].points[i].y / width) * 2;
                                    float[] pointf = new float[] { x, y, 0.0f };
                                    if (orientation == 1)
                                        pointf = new float[] { -y, x, 0.0f };
                                    if (orientation == 2)
                                        pointf = new float[] { y, -x, 0.0f };
                                    if (orientation == 3)
                                        pointf = new float[] { -x, -y, 0.0f };

                                    FloatBuffer fb = GlUtil.createFloatBuffer(pointf);
                                    triangleVBList.add(fb);
                                }

                                pointsOpengl.add(triangleVBList);
                            }
                        } else {
                            pitch = 0.0f;
                            yaw = 0.0f;
                            roll = 0.0f;
                        }

                        synchronized (mFacePointsDrawer) {
                            mFacePointsDrawer.points = pointsOpengl;
                        }
                    }
                    isSuccess = false;
                    if (!isTiming) {
                        timeHandle.sendEmptyMessage(1);
                    }
                }
            });
        } else {
            mGlSurfaceView.requestRender();
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 黑色背景
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        mTextureID = GlUtil.createTextureOES();
        mSurface = new SurfaceTexture(mTextureID);
        mSurface.setOnFrameAvailableListener(this);
        // 定点
        mFacePointsDrawer = new FacePointsDrawer();

        // 渲染初始化
        Size size = CameraUtils.getPreviewSize();
        mCameraFilter = new CameraFilter();
        mCameraFilter.onInputSizeChanged(size.getWidth(), size.getHeight());
        // 设置预览回调
        CameraUtils.setPreviewCallback(this);
        CameraUtils.setPreviewSurface(mSurface);
        CameraUtils.startPreview();
        isPreviewing = true;
        // 定时刷新
        if (isTiming) {
            timeHandle.sendEmptyMessageDelayed(0, printTime);
        }
    }

    private int mViewWidth;
    private int mViewHeight;
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mViewWidth = width;
        mViewHeight = height;
        GLES30.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        ratio = 1;
        // 投影矩阵
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);

        // 渲染视图变化
        if (mCameraFilter != null) {
            mCameraFilter.onDisplayChanged(width, height);
        }

    }

    private final float[] mMVPMatrix = GlUtil.IDENTITY_MATRIX;
    private final float[] mProjMatrix = new float[16];
    private final float[] mVMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];

    @Override
    public void onDrawFrame(GL10 gl) {
        final long actionTime = System.currentTimeMillis();
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);// 清除屏幕和深度缓存
        float[] mtx = new float[16];
        mSurface.getTransformMatrix(mtx);

        // 绘制
        if (mCameraFilter != null) {
            mCameraFilter.setTextureTransformMatirx(mtx);
            mCameraFilter.drawFrame(mTextureID);
        }

        Matrix.setLookAtM(mVMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1f, 0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
        GLES30.glViewport(0, 0, mViewWidth, mViewHeight);
        mFacePointsDrawer.draw(mMVPMatrix);
        if (isDebug) {
            long endTime = System.currentTimeMillis() - actionTime;
            Log.d("onDrawFrame", "printTime = " + endTime);
        }
        mSurface.updateTexImage();
    }


    Handler timeHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    mGlSurfaceView.requestRender();// 发送去绘制照相机不断去回调
                    timeHandle.sendEmptyMessageDelayed(0, printTime);
                    break;
                case 1:
                    mGlSurfaceView.requestRender();// 发送去绘制照相机不断去回调
                    break;
            }
        }
    };
}
