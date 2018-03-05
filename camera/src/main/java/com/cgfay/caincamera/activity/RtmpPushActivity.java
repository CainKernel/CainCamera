package com.cgfay.caincamera.activity;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cgfay.caincamera.R;
import com.cgfay.cainfilter.utils.TextureRotationUtils;
import com.cgfay.pushlibrary.AudioPusher;
import com.cgfay.pushlibrary.RtmpPusher;
import com.cgfay.utilslibrary.AspectFrameLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RtmpPushActivity extends AppCompatActivity implements View.OnClickListener,
        SurfaceHolder.Callback, Camera.PreviewCallback{

    private static final int SCREEN_PORTRAIT = 0;
    private static final int SCREEN_LANDSCAPE_LEFT = 90;
    private static final int SCREEN_LANDSCAPE_RIGHT = 270;
    private int screen;
    private byte[] raw;

    private RtmpPusher mRtmpPusher;
    private AudioPusher mAudioPusher;

    private String mRtmpUrl = "rtmp://192.168.0.102/live/test";

    private EditText mEtRtmp;
    private Button mBtnPush;

    private AspectFrameLayout mLayoutAspect;
    private SurfaceView mSurfaceView;

    private HandlerThread mRenderThread;
    private Handler mRenderHandler;

    private boolean mBackReverse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String phoneName = Build.MODEL;
        if (phoneName.toLowerCase().contains("bullhead")
                || phoneName.toLowerCase().contains("nexus 5x")) {
            mBackReverse = true;
        }
        setContentView(R.layout.activity_rtmp_push);

        mLayoutAspect = (AspectFrameLayout) findViewById(R.id.layout_aspect);
        mLayoutAspect.setAspectRatio(mCurrentRatio);

        mEtRtmp = (EditText) findViewById(R.id.et_rtmp);
        mEtRtmp.setText(mRtmpUrl);

        mBtnPush = (Button) findViewById(R.id.btn_push);
        mBtnPush.setOnClickListener(this);

        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mSurfaceView.getHolder().addCallback(this);

        initRenderThread();
    }

    @Override
    protected void onPause() {
        mBtnPush.setText("开始推流");
        if (mRenderHandler != null) {
            mRenderHandler.post(new Runnable() {
                @Override
                public void run() {
                    stopPush();
                }
            });
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mAudioPusher != null) {
            mAudioPusher.stop();
            mAudioPusher = null;
        }
        destoryRenderThread();
        super.onDestroy();

    }

    /**
     * 初始化渲染线程
     */
    private void initRenderThread() {
        mRenderThread = new HandlerThread("Render Thread");
        mRenderThread.start();
        mRenderHandler = new Handler(mRenderThread.getLooper());
    }

    /**
     * 销毁渲染线程
     */
    private void destoryRenderThread() {
        if (mRenderHandler != null) {
            mRenderHandler.removeCallbacksAndMessages(null);
            mRenderHandler = null;
        }
        if (mRenderThread != null) {
            mRenderThread.quit();
            mRenderThread = null;
        }
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        if (mRenderHandler != null) {
            mRenderHandler.post(new Runnable() {
                @Override
                public void run() {
                    internalSurfaceCreated(holder);
                }
            });
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format,
                               final int width, final int height) {
        switch (format) {
            case PixelFormat.A_8:
                Log.v("surfaceChanged", "pixel format A_8");
                break;
            case PixelFormat.LA_88:
                Log.v("surfaceChanged", "pixel format LA_88");
                break;
            case PixelFormat.L_8:
                Log.v("surfaceChanged", "pixel format L_8");
                break;
            case PixelFormat.RGBA_4444:
                Log.v("surfaceChanged", "pixel format RGBA_4444");
                break;
            case PixelFormat.RGBA_5551:
                Log.v("surfaceChanged", "pixel format RGBA_5551");
                break;
            case PixelFormat.RGBA_8888:
                Log.v("surfaceChanged", "pixel format RGBA_8888");
                break;
            case PixelFormat.RGBX_8888:
                Log.v("surfaceChanged", "pixel format RGBX_8888");
                break;
            case PixelFormat.RGB_332:
                Log.v("surfaceChanged", "pixel format RGB_332");
                break;
            case PixelFormat.RGB_565:
                Log.v("surfaceChanged", "pixel format RGB_565");
                break;
            case PixelFormat.RGB_888:
                Log.v("surfaceChanged", "pixel format RGB_888");
                break;
            default:
                Log.v("surfaceChanged", "pixel format unknown " + format);
                break;
        }

        if (mRenderHandler != null) {
            mRenderHandler.post(new Runnable() {
                @Override
                public void run() {
                    internalSurfaceChanged(width, height);
                }
            });
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mRenderHandler != null) {
            mRenderHandler.post(new Runnable() {
                @Override
                public void run() {
                    internalSurfacDestory();
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_push:
                recordVideo();
                break;
        }
    }

    /**
     * 录制或停止录制
     */
    private void recordVideo() {
        if (mPreviewing) {
            if (!mRecording) {
                mRenderHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        boolean success = startPush();
                        if (success) {
                            mRecording = true;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mBtnPush.setText("停止推流");
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(RtmpPushActivity.this,
                                            "推流器初始化失败", Toast.LENGTH_SHORT);
                                }
                            });
                        }
                    }
                });
            } else {
                mRecording = false;
                mRenderHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        stopPush();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mBtnPush.setText("开始推流");
                            }
                        });
                    }
                });
            }
        }
    }

    /**
     * 初始化RTMP推流器
     */
    private boolean startPush() {
        mRtmpPusher = new RtmpPusher();
        String url = mRtmpUrl;
        if (!TextUtils.isEmpty(mEtRtmp.getText())) {
            url = mEtRtmp.getText().toString();
        }
        int result = mRtmpPusher.initVideo(url,
                mImageHeight, mImageWidth, (int) (mImageWidth * mImageHeight * 8 * 0.25));
        if (result == 0) {
            mAudioPusher = new AudioPusher(mRtmpPusher);
            mAudioPusher.start();
        }
        return result == 0;
    }

    /**
     * 停止推流
     */
    private void stopPush() {
        if (mAudioPusher != null) {
            mAudioPusher.stop();
            mAudioPusher = null;
        }
        if (mRtmpPusher != null) {
            mRtmpPusher.stop();
            mRtmpPusher = null;
        }
    }

    // ------------------------------------ 内部方法 -----------------------------------
    private static final float Ratio_16_9 = 0.5625f;
    private static final float Ratio_4_3 = 0.75f;

    // 备注：720P推流会很卡，480P推流效率还可以接受
    private static final int DEFAULT_WIDTH = 640;
    private static final int DEFAULT_HEIGHT = 480;

    private int mCameraId;
    private Camera mCamera;
    private float mCurrentRatio = Ratio_4_3;
    private SurfaceHolder mSurfaceHolder;
    private byte[] mPreviewBuffer;

    private int mImageWidth;
    private int mImageHeight;

    private int mViewWidth;
    private int mViewHeight;

    private boolean mPreviewing = false;
    private boolean mRecording = false;

    private void internalSurfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
        if (mCamera != null) {
            throw new RuntimeException("camera already initialized!");
        }
        // 打开后置摄像头
        mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        mCamera = Camera.open(mCameraId);
        if (mCamera == null) {
            throw new RuntimeException("Unable to open camera");
        }
        Camera.Parameters parameters = mCamera.getParameters();
        int fps = chooseFixedPreviewFps(parameters, 30 * 1000);
        parameters.setRecordingHint(true);
        // 设置自动对焦
        mCamera.setParameters(parameters);
        // 设置预览宽高
        setPreviewSize(mCamera, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        // 设置拍照宽高
        setPictureSize(mCamera, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        // 设置后置摄像头旋转角度
        int orientation = calculateCameraPreviewOrientation(RtmpPushActivity.this,
                Camera.CameraInfo.CAMERA_FACING_BACK);
        if (mBackReverse) {
            mCamera.setDisplayOrientation(360 - orientation);
        } else {
            mCamera.setDisplayOrientation(orientation);
        }

        // 获取实际预览的宽高
        Camera.Size size = mCamera.getParameters().getPreviewSize();

        // 预览图像的宽高
        mImageWidth = size.width;
        mImageHeight = size.height;

        // 缓冲区，YUV的Buffer 大小 是 width * height * 1.5
        mPreviewBuffer = new byte[mImageWidth * mImageHeight * 3 / 2];

        // 设置回调
        mCamera.setPreviewCallbackWithBuffer(RtmpPushActivity.this);

        // 设置回调的Buffer
        mCamera.addCallbackBuffer(mPreviewBuffer);

        // 开始预览
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();
            mCamera.cancelAutoFocus();
            mPreviewing = true;
        } catch (IOException e) {
            e.printStackTrace();
            mPreviewing = false;
        }
        mRecording = false;
    }

    private void internalSurfaceChanged(int width, int height) {
        mViewWidth = width;
        mViewHeight = height;
    }

    private void internalSurfacDestory() {
        if (mCamera != null) {
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.addCallbackBuffer(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        mSurfaceHolder = null;
        mPreviewBuffer = null;
        mRecording = false;
        // 停止推流
        stopPush();
    }

    @Override
    public void onPreviewFrame(final byte[] data, Camera camera) {
        // 录制视频数据
        if (mRecording) {
            mRenderHandler.post(new Runnable() {
                @Override
                public void run() {

                    switch (screen) {
                        case SCREEN_PORTRAIT:
                            if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                                if (mRtmpPusher != null) {
                                    mRtmpPusher.pushYUV(data,1);
                                }
                            } else {
                                if (mRtmpPusher != null) {
                                    mRtmpPusher.pushYUV(data,2);
                                }
                            }
                            break;
                        case SCREEN_LANDSCAPE_LEFT:
                            if (mRtmpPusher != null) {
                                mRtmpPusher.pushYUV(data,3);
                            }
                            break;
                        case SCREEN_LANDSCAPE_RIGHT:
                            if (mRtmpPusher != null) {
                                landscapeData2Raw(data);
                                mRtmpPusher.pushYUV(raw, 3);
                            }
                            break;
                    }
                }
            });
        }

        // 添加回调
        camera.addCallbackBuffer(mPreviewBuffer);
    }

    /**
     * 数据倒插，这是横屏时画面反过来使用的
     * @param data
     */
    private void landscapeData2Raw(byte[] data) {
        int width = mImageWidth, height = mImageHeight;
        int y_len = width * height;
        int k = 0;
        // y数据倒叙插入raw中
        for (int i = y_len - 1; i > -1; i--) {
            raw[k] = data[i];
            k++;
        }
        int maxpos = data.length - 1;
        int uv_len = y_len >> 2; // 4:1:1
        for (int i = 0; i < uv_len; i++) {
            int pos = i << 1;
            raw[y_len + i * 2] = data[maxpos - pos - 1];
            raw[y_len + i * 2 + 1] = data[maxpos - pos];
        }
    }

    /**
     * 选择合适的预览fps
     * @param parameters
     * @param expectedThoudandFps
     * @return
     */
    private int chooseFixedPreviewFps(Camera.Parameters parameters, int expectedThoudandFps) {
        List<int[]> supportedFps = parameters.getSupportedPreviewFpsRange();
        for (int[] entry : supportedFps) {
            if (entry[0] == entry[1] && entry[0] == expectedThoudandFps) {
                parameters.setPreviewFpsRange(entry[0], entry[1]);
                return entry[0];
            }
        }
        int[] temp = new int[2];
        int guess;
        parameters.getPreviewFpsRange(temp);
        if (temp[0] == temp[1]) {
            guess = temp[0];
        } else {
            guess = temp[1] / 2;
        }
        return guess;
    }

    /**
     * 设置预览大小
     * @param camera
     * @param expectWidth
     * @param expectHeight
     */
    private void setPreviewSize(Camera camera, int expectWidth, int expectHeight) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = calculatePerfectSize(parameters.getSupportedPreviewSizes(),
                expectWidth, expectHeight);
        parameters.setPreviewSize(size.width, size.height);
        camera.setParameters(parameters);
    }

    /**
     * 设置拍摄的照片大小
     * @param camera
     * @param expectWidth
     * @param expectHeight
     */
    private void setPictureSize(Camera camera, int expectWidth, int expectHeight) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = calculatePerfectSize(parameters.getSupportedPictureSizes(),
                expectWidth, expectHeight);
        parameters.setPictureSize(size.width, size.height);
        camera.setParameters(parameters);
    }

    /**
     * 计算最完美的Size
     * @param sizes
     * @param expectWidth
     * @param expectHeight
     * @return
     */
    private Camera.Size calculatePerfectSize(List<Camera.Size> sizes, int expectWidth,
                                             int expectHeight) {
        sortList(sizes); // 根据宽度进行排序

        // 根据当前期望的宽高判定
        List<Camera.Size> bigEnough = new ArrayList<>();
        List<Camera.Size> noBigEnough = new ArrayList<>();
        for (Camera.Size size : sizes) {
            if (size.height * expectWidth / expectHeight == size.width) {
                if (size.width >= expectWidth && size.height >= expectHeight) {
                    bigEnough.add(size);
                } else {
                    noBigEnough.add(size);
                }
            }
        }
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareAreaSize());
        } else if (noBigEnough.size() > 0) {
            return Collections.max(noBigEnough, new CompareAreaSize());
        } else { // 如果不存在满足要求的数值，则辗转计算宽高最接近的值
            Camera.Size result = sizes.get(0);
            boolean widthOrHeight = false; // 判断存在宽或高相等的Size
            // 辗转计算宽高最接近的值
            for (Camera.Size size : sizes) {
                // 如果宽高相等，则直接返回
                if (size.width == expectWidth && size.height == expectHeight
                        && ((float) size.height / (float) size.width) == mCurrentRatio) {
                    result = size;
                    break;
                }
                // 仅仅是宽度相等，计算高度最接近的size
                if (size.width == expectWidth) {
                    widthOrHeight = true;
                    if (Math.abs(result.height - expectHeight)
                            > Math.abs(size.height - expectHeight)
                            && ((float) size.height / (float) size.width) == mCurrentRatio) {
                        result = size;
                        break;
                    }
                }
                // 高度相等，则计算宽度最接近的Size
                else if (size.height == expectHeight) {
                    widthOrHeight = true;
                    if (Math.abs(result.width - expectWidth)
                            > Math.abs(size.width - expectWidth)
                            && ((float) size.height / (float) size.width) == mCurrentRatio) {
                        result = size;
                        break;
                    }
                }
                // 如果之前的查找不存在宽或高相等的情况，则计算宽度和高度都最接近的期望值的Size
                else if (!widthOrHeight) {
                    if (Math.abs(result.width - expectWidth)
                            > Math.abs(size.width - expectWidth)
                            && Math.abs(result.height - expectHeight)
                            > Math.abs(size.height - expectHeight)
                            && ((float) size.height / (float) size.width) == mCurrentRatio) {
                        result = size;
                    }
                }
            }
            return result;
        }
    }

    /**
     * 分辨率由大到小排序
     * @param list
     */
    private void sortList(List<Camera.Size> list) {
        Collections.sort(list, new CompareAreaSize());
    }

    /**
     * 比较器
     */
    private class CompareAreaSize implements Comparator<Camera.Size> {
        @Override
        public int compare(Camera.Size pre, Camera.Size after) {
            return Long.signum((long) pre.width * pre.height -
                    (long) after.width * after.height);
        }
    }

    /**
     * 计算旋转角度
     * @param activity
     * @param cameraId
     * @return
     */
    public int calculateCameraPreviewOrientation(Activity activity, int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                screen = SCREEN_PORTRAIT;
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                screen = SCREEN_LANDSCAPE_LEFT;
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                screen = 180;
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                screen = SCREEN_LANDSCAPE_RIGHT;
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

}
