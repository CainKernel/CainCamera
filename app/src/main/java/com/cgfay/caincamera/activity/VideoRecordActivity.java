package com.cgfay.caincamera.activity;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import com.cgfay.caincamera.jni.FFmpegHandler;
import com.cgfay.caincamera.view.AspectFrameLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 使用FFmpeg录制视频页面（未完成）
 */
public class VideoRecordActivity extends AppCompatActivity implements View.OnClickListener,
        SurfaceHolder.Callback, Camera.PreviewCallback, IMediaRecorder {

    public static final int AUDIO_RECORD_ERROR_UNKNOWN = 0;
    /**
     * 采样率设置不支持
     */
    public static final int AUDIO_RECORD_ERROR_SAMPLERATE_NOT_SUPPORT = 1;
    /**
     * 最小缓存获取失败
     */
    public static final int AUDIO_RECORD_ERROR_GET_MIN_BUFFER_SIZE_NOT_SUPPORT = 2;
    /**
     * 创建AudioRecord失败
     */
    public static final int AUDIO_RECORD_ERROR_CREATE_FAILED = 3;

    private Button mBtnRecord;

    private AspectFrameLayout mLayoutAspect;
    private SurfaceView mSurfaceView;

    private HandlerThread mRenderThread;
    private Handler mRenderHandler;
    // 视频编码任务
    private VideoStreamTask mVideoStreamTask;
    // 音频编码任务
    private AudioStreamTask mAudioStreamTask;
    // 音频录制
    private AudioRecorder mAudioRecorder;

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
            if (!mRecording) {
                startVideoRecord();
                startAudioRecord();
            } else {
                stopVideoRecord();
                stopAudioRecord();
            }
        }
    }

    /**
     * 开始视频录制
     */
    private void startVideoRecord() {
        mRenderHandler.post(new Runnable() {
            @Override
            public void run() {
                String filename = "/DCIM/Camera/" + System.currentTimeMillis() + ".mp4";
                String path = Environment.getExternalStorageDirectory().getPath() + filename;
                FFmpegHandler.initMediaRecorder(path, mImageWidth, mImageHeight, mImageWidth, mImageHeight,
                        25, 5760000, true, 40000, 44100);
                FFmpegHandler.startRecord();
                mRecording = true;
            }
        });
    }

    /**
     * 停止视频录制
     */
    private void stopVideoRecord() {
        mRecording = false;
        mRenderHandler.post(new Runnable() {
            @Override
            public void run() {
                FFmpegHandler.stopRecord();
            }
        });

    }

    /**
     * 开始音频录制
     */
    private void startAudioRecord() {
        mAudioRecorder = new AudioRecorder(this);
        mAudioRecorder.start();
    }

    /**
     * 停止音频录制
     */
    private void stopAudioRecord() {
        mAudioRecorder.stopRecord();
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
        parameters.setRecordingHint(true);
        // 设置自动对焦
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        mCamera.setParameters(parameters);
        // 设置预览宽高
        setPreviewSize(mCamera, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        // 设置拍照宽高
        setPictureSize(mCamera, DEFAULT_WIDTH, DEFAULT_HEIGHT);
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
    }

    @Override
    public void onPreviewFrame(final byte[] data, Camera camera) {
        // 录制视频数据
        if (mRecording) {
            mRenderHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (null != mVideoStreamTask) {
                        switch(mVideoStreamTask.getStatus()){
                            case RUNNING:
                                return;

                            case PENDING:
                                mVideoStreamTask.cancel(false);
                                break;
                        }
                    }
                    mVideoStreamTask = new VideoStreamTask(data);
                    mVideoStreamTask.execute((Void)null);
                }
            });
        }

        // 添加回调
        camera.addCallbackBuffer(mPreviewBuffer);
    }


    @Override
    public void onAudioError(int what, String message) {
        Log.d("onAudioError", "what = " + what + ", message = " + message);
    }

    @Override
    public void receiveAudioData(final byte[] sampleBuffer, final int len) {
        // 音频编码
        if (mRecording) {
            mRenderHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (null != mAudioStreamTask) {
                        switch(mAudioStreamTask.getStatus()) {
                            case RUNNING:
                                return;

                            case PENDING:
                                mAudioStreamTask.cancel(false);
                                break;
                        }
                    }
                    mAudioStreamTask = new AudioStreamTask(sampleBuffer, len);
                    mAudioStreamTask.execute((Void)null);
                }
            });

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

    // ------------------------------------- 视频编码线程 -------------------------------------------
    private class VideoStreamTask extends AsyncTask<Void, Void, Void> {

        private byte[] mData;

        //构造函数
        VideoStreamTask(byte[] data){
            this.mData = data;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (mData != null) {
                FFmpegHandler.encodeYUVFrame(mData);
            }
            return null;
        }
    }

    // ----------------------------------- 音频编码线程 ---------------------------------------------
    private class AudioStreamTask extends AsyncTask<Void, Void, Void> {

        private byte[] mData; // 音频数据
        private int mSize;

        //构造函数
        AudioStreamTask(byte[] data, int size) {
            mData = data;
            mSize = size;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (mData != null) {
                FFmpegHandler.encodePCMFrame(mData, mSize);
            }
            return null;
        }
    }

    // ------------------------------------------ 音频录音线程 --------------------------------------
    public class AudioRecorder extends Thread {
        // 是否停止线程
        private boolean mStop = false;

        private AudioRecord mAudioRecord = null;
        /** 采样率 */
        private int mSampleRate = 44100;
        private IMediaRecorder mMediaRecorder;

        public AudioRecorder(IMediaRecorder mediaRecorder) {
            this.mMediaRecorder = mediaRecorder;
        }

        /** 设置采样率 */
        public void setSampleRate(int sampleRate) {
            this.mSampleRate = sampleRate;
        }

        @Override
        public void run() {
            if (mSampleRate != 8000 && mSampleRate != 16000 && mSampleRate != 22050
                    && mSampleRate != 44100) {
                mMediaRecorder.onAudioError(AUDIO_RECORD_ERROR_SAMPLERATE_NOT_SUPPORT,
                        "sampleRate not support.");
                return;
            }

            final int mMinBufferSize = AudioRecord.getMinBufferSize(mSampleRate,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

            if (AudioRecord.ERROR_BAD_VALUE == mMinBufferSize) {
                mMediaRecorder.onAudioError(AUDIO_RECORD_ERROR_GET_MIN_BUFFER_SIZE_NOT_SUPPORT,
                        "parameters are not supported by the hardware.");
                return;
            }

            mAudioRecord = new AudioRecord(android.media.MediaRecorder.AudioSource.MIC, mSampleRate,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, mMinBufferSize);
            if (null == mAudioRecord) {
                mMediaRecorder.onAudioError(AUDIO_RECORD_ERROR_CREATE_FAILED, "new AudioRecord failed.");
                return;
            }
            try {
                mAudioRecord.startRecording();
            } catch (IllegalStateException e) {
                mMediaRecorder.onAudioError(AUDIO_RECORD_ERROR_UNKNOWN, "startRecording failed.");
                return;
            }

            byte[] sampleBuffer = new byte[2048];

            try {
                while (!mStop) {
                    int result = mAudioRecord.read(sampleBuffer, 0, 2048);
                    if (result > 0) {
                        mMediaRecorder.receiveAudioData(sampleBuffer, result);
                    }
                }
            } catch (Exception e) {
                String message = "";
                if (e != null)
                    message = e.getMessage();
                mMediaRecorder.onAudioError(AUDIO_RECORD_ERROR_UNKNOWN, message);
            }

            mAudioRecord.release();
            mAudioRecord = null;
        }

        /**
         * 停止音频录制
         */
        public void stopRecord() {
            mStop = true;
        }
    }


}