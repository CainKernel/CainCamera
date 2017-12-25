package com.cgfay.caincamera.activity;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.cgfay.caincamera.R;
import com.cgfay.caincamera.utils.CameraUtils;
import com.cgfay.caincamera.view.AspectFrameLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 使用FFmpeg录制视频页面（未完成）
 */
public class VideoRecordActivity extends AppCompatActivity
        implements View.OnClickListener, SurfaceHolder.Callback, Camera.PreviewCallback {

    private Button mBtnRecord;

    private AspectFrameLayout mLayoutAspect;
    private SurfaceView mSurfaceView;

    private HandlerThread mRenderThread;
    private Handler mRenderHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);

        mLayoutAspect = (AspectFrameLayout) findViewById(R.id.layout_aspect);
        mLayoutAspect.setAspectRatio(mCurrentRatio);
        mBtnRecord = (Button) findViewById(R.id.btn_record);
        mBtnRecord.setOnClickListener(this);

        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mSurfaceView.getHolder().addCallback(this);

        initRenderThread();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destoryRenderThread();
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
            case R.id.btn_record:
                recordVideo();
                break;
        }
    }

    /**
     * 录制或停止录制
     */
    private void recordVideo() {
        if (mPreviewing) {
            mRecording = !mRecording;
        }
    }


    // ------------------------------------ 内部方法 -----------------------------------
    private static final float Ratio_16_9 = 0.5625f;

    private static final int DEFAULT_WIDTH = 1280;
    private static final int DEFAULT_HEIGHT = 720;

    private Camera mCamera;
    private float mCurrentRatio = Ratio_16_9;
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
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        if (mCamera == null) {
            throw new RuntimeException("Unable to open camera");
        }
        Camera.Parameters parameters = mCamera.getParameters();
        int fps = chooseFixedPreviewFps(parameters, 30 * 1000);
        Log.d("internalSurfaceCreated", "相机实际合适的fps：" + (fps / 1000));
        parameters.setRecordingHint(true);
        mCamera.setParameters(parameters);
        // 期望宽高
        int width = DEFAULT_WIDTH;
        int height = DEFAULT_HEIGHT;
        // 设置预览宽高
        setPreviewSize(mCamera, width, height);
        // 设置拍照宽高
        setPictureSize(mCamera, width, height);
        // 设置后置摄像头旋转角度
        int orientation = calculateCameraPreviewOrientation(VideoRecordActivity.this,
                Camera.CameraInfo.CAMERA_FACING_BACK);
        mCamera.setDisplayOrientation(orientation);

        // 获取实际预览的宽高
        Camera.Size size = mCamera.getParameters().getPreviewSize();

        // 预览图像的宽高
        mImageWidth = size.width;
        mImageHeight = size.height;

        // 缓冲区，YUV的Buffer 大小 是 width * height * 1.5
        mPreviewBuffer = new byte[mImageWidth * mImageHeight * 3 / 2];

        // 设置回调
        mCamera.setPreviewCallbackWithBuffer(VideoRecordActivity.this);

        // 设置回调的Buffer
        mCamera.addCallbackBuffer(mPreviewBuffer);

        // 开始预览
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();
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
        Log.d("internalSurfaceChanged", "displayWidth = " + width + ", displayHeight = " + height);
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
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        // 录制视频数据


        // 添加回调
        camera.addCallbackBuffer(mPreviewBuffer);
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
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
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
