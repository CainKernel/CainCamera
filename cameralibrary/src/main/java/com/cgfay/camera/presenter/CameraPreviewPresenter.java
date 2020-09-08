package com.cgfay.camera.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import androidx.annotation.NonNull;

import android.opengl.EGLContext;
import android.text.TextUtils;
import android.util.Log;

import com.cgfay.camera.activity.CameraSettingActivity;
import com.cgfay.camera.camera.CameraController;
import com.cgfay.camera.camera.CameraParam;
import com.cgfay.camera.camera.ICameraController;
import com.cgfay.camera.camera.OnFrameAvailableListener;
import com.cgfay.camera.camera.OnSurfaceTextureListener;
import com.cgfay.camera.camera.PreviewCallback;
import com.cgfay.camera.listener.OnCaptureListener;
import com.cgfay.camera.listener.OnFpsListener;
import com.cgfay.camera.fragment.CameraPreviewFragment;
import com.cgfay.camera.listener.OnPreviewCaptureListener;
import com.cgfay.camera.render.CameraRenderer;
import com.cgfay.uitls.utils.PathUtils;
import com.cgfay.cavfoundation.capture.CAVCaptureAudioMuteInput;
import com.cgfay.cavfoundation.capture.CAVCaptureAudioRecordInput;
import com.cgfay.cavfoundation.capture.CAVCaptureRecorder;
import com.cgfay.cavfoundation.capture.CAVCaptureRecordListener;
import com.cgfay.cavfoundation.codec.CAVAudioInfo;
import com.cgfay.cavfoundation.codec.CAVVideoInfo;
import com.cgfay.facedetect.engine.FaceTracker;
import com.cgfay.facedetect.listener.FaceTrackerCallback;
import com.cgfay.filter.glfilter.color.bean.DynamicColor;
import com.cgfay.filter.glfilter.makeup.bean.DynamicMakeup;
import com.cgfay.filter.glfilter.resource.FilterHelper;
import com.cgfay.filter.glfilter.resource.ResourceHelper;
import com.cgfay.filter.glfilter.resource.ResourceJsonCodec;
import com.cgfay.filter.glfilter.resource.bean.ResourceData;
import com.cgfay.filter.glfilter.resource.bean.ResourceType;
import com.cgfay.filter.glfilter.stickers.bean.DynamicSticker;
import com.cgfay.media.CAVCommandEditor;
import com.cgfay.media.recorder.MediaInfo;
import com.cgfay.media.recorder.SpeedMode;
import com.cgfay.landmark.LandmarkEngine;
import com.cgfay.uitls.utils.BitmapUtils;
import com.cgfay.uitls.utils.BrightnessUtils;
import com.cgfay.uitls.utils.FileUtils;
import com.cgfay.video.activity.VideoEditActivity;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 预览的presenter
 * @author CainHuang
 * @date 2019/7/3
 */
public class CameraPreviewPresenter extends PreviewPresenter<CameraPreviewFragment>
        implements PreviewCallback, FaceTrackerCallback, OnCaptureListener, OnFpsListener,
        OnSurfaceTextureListener, OnFrameAvailableListener, CAVCaptureRecordListener {

    private static final String TAG = "CameraPreviewPresenter";
    private static final boolean VERBOSE = false;

    public static final int SECOND_IN_US = 1000000;

    // 当前索引
    private int mFilterIndex = 0;

    // 预览参数
    private CameraParam mCameraParam;

    private Activity mActivity;

    // 背景音乐
    private String mMusicPath;

    // 录制操作开始
    private boolean mOperateStarted = false;

    // 最大时长
    private long mMaxDuration;
    // 剩余时长
    private long mRemainDuration;

    // 速度模式
    private SpeedMode mSpeedMode;
    // 是否允许录音
    private boolean mEnableAudio;
    // 视频参数
    private CAVVideoInfo mVideoInfo;
    // 音频参数
    private CAVAudioInfo mAudioInfo;
    // 视频录制器
    private CAVCaptureRecorder mMediaRecorder;
    // 视频列表
    private List<MediaInfo> mVideoList = new ArrayList<>();

    // 命令行编辑器
    private CAVCommandEditor mCommandEditor;

    // 相机接口
    private ICameraController mCameraController;

    // 渲染器
    private final CameraRenderer mCameraRenderer;

    // EGL上下文
    private WeakReference<EGLContext> mWeakGLContext;

    public CameraPreviewPresenter(CameraPreviewFragment target) {
        super(target);
        mCameraParam = CameraParam.getInstance();

        mCameraRenderer = new CameraRenderer(this);
        // 命令行编辑器
        mCommandEditor = new CAVCommandEditor();
        mAudioInfo = new CAVAudioInfo();
        mVideoInfo = new CAVVideoInfo();
        mSpeedMode = SpeedMode.MODE_NORMAL;
        mEnableAudio = true;
    }

    public void onAttach(Activity activity) {
        mActivity = activity;
        mCameraRenderer.initRenderer();

//        // 备注：目前支持CameraX的渲染流程，但CameraX回调预览帧数据有些问题，人脸关键点SDK检测返回的数据错乱，暂不建议在商用项目中使用CameraX
//        if (CameraApi.hasCamera2(mActivity)) {
//            mCameraController = new CameraXController(getTarget(), ContextCompat.getMainExecutor(mActivity));
//        } else {
//            mCameraController = new CameraController(mActivity);
//        }
        mCameraController = new CameraController(mActivity);
        mCameraController.setPreviewCallback(this);
        mCameraController.setOnFrameAvailableListener(this);
        mCameraController.setOnSurfaceTextureListener(this);

        if (BrightnessUtils.getSystemBrightnessMode(mActivity) == 1) {
            mCameraParam.brightness = -1;
        } else {
            mCameraParam.brightness = BrightnessUtils.getSystemBrightness(mActivity);
        }

        // 初始化检测器
        FaceTracker.getInstance()
                .setFaceCallback(this)
                .previewTrack(true)
                .initTracker();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        openCamera();
        mCameraParam.captureCallback = this;
        mCameraParam.fpsCallback = this;
    }

    @Override
    public void onPause() {
        super.onPause();
        mCameraRenderer.onPause();
        closeCamera();
        mCameraParam.captureCallback = null;
        mCameraParam.fpsCallback = null;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 销毁人脸检测器
        FaceTracker.getInstance().destroyTracker();
        // 清理关键点
        LandmarkEngine.getInstance().clearAll();
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        if (mCommandEditor != null) {
            mCommandEditor.release();
            mCommandEditor = null;
        }
    }

    public void onDetach() {
        mActivity = null;
        mCameraRenderer.destroyRenderer();
    }

    @NonNull
    @Override
    public Context getContext() {
        return mActivity;
    }

    @Override
    public void onBindSharedContext(EGLContext context) {
        mWeakGLContext = new WeakReference<>(context);
    }

    @Override
    public void onRecordFrameAvailable(int texture, long timestamp) {
        if (mOperateStarted && mMediaRecorder != null && mMediaRecorder.isRecording()) {
            mMediaRecorder.renderFrame(texture, timestamp);
        }
    }

    @Override
    public void onSurfaceCreated(SurfaceTexture surfaceTexture) {
        mCameraRenderer.onSurfaceCreated(surfaceTexture);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        mCameraRenderer.onSurfaceChanged(width, height);
    }

    @Override
    public void onSurfaceDestroyed() {
        mCameraRenderer.onSurfaceDestroyed();
    }

    @Override
    public void changeResource(@NonNull ResourceData resourceData) {
        ResourceType type = resourceData.type;
        String unzipFolder = resourceData.unzipFolder;
        if (type == null) {
            return;
        }
        try {
            switch (type) {
                // 单纯的滤镜
                case FILTER: {
                    String folderPath = ResourceHelper.getResourceDirectory(mActivity) + File.separator + unzipFolder;
                    DynamicColor color = ResourceJsonCodec.decodeFilterData(folderPath);
                    mCameraRenderer.changeResource(color);
                    break;
                }

                // 贴纸
                case STICKER: {
                    String folderPath = ResourceHelper.getResourceDirectory(mActivity) + File.separator + unzipFolder;
                    DynamicSticker sticker = ResourceJsonCodec.decodeStickerData(folderPath);
                    mCameraRenderer.changeResource(sticker);
                    break;
                }

                // TODO 多种结果混合
                case MULTI: {
                    break;
                }

                // 所有数据均为空
                case NONE: {
                    mCameraRenderer.changeResource((DynamicSticker) null);
                    break;
                }

                default:
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "parseResource: ", e);
        }
    }

    @Override
    public void changeDynamicFilter(DynamicColor color) {
        mCameraRenderer.changeFilter(color);
    }

    @Override
    public void changeDynamicMakeup(DynamicMakeup makeup) {
        mCameraRenderer.changeMakeup(makeup);
    }

    @Override
    public void changeDynamicFilter(int filterIndex) {
        if (mActivity == null) {
            return;
        }
        String folderPath = FilterHelper.getFilterDirectory(mActivity) + File.separator +
                FilterHelper.getFilterList().get(filterIndex).unzipFolder;
        DynamicColor color = null;
        if (!FilterHelper.getFilterList().get(filterIndex).unzipFolder.equalsIgnoreCase("none")) {
            try {
                color = ResourceJsonCodec.decodeFilterData(folderPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mCameraRenderer.changeFilter(color);
    }

    @Override
    public int previewFilter() {
        mFilterIndex--;
        if (mFilterIndex < 0) {
            int count = FilterHelper.getFilterList().size();
            mFilterIndex = count > 0 ? count - 1 : 0;
        }
        changeDynamicFilter(mFilterIndex);
        return mFilterIndex;
    }

    @Override
    public int nextFilter() {
        mFilterIndex++;
        mFilterIndex = mFilterIndex % FilterHelper.getFilterList().size();
        changeDynamicFilter(mFilterIndex);
        return mFilterIndex;
    }

    @Override
    public int getFilterIndex() {
        return mFilterIndex;
    }

    @Override
    public void showCompare(boolean enable) {
        mCameraParam.showCompare = enable;
    }

    /**
     * 打开相机
     */
    private void openCamera() {
        mCameraController.openCamera();
        calculateImageSize();
    }

    /**
     * 计算imageView 的宽高
     */
    private void calculateImageSize() {
        int width;
        int height;
        if (mCameraController.getOrientation() == 90 || mCameraController.getOrientation() == 270) {
            width = mCameraController.getPreviewHeight();
            height = mCameraController.getPreviewWidth();
        } else {
            width = mCameraController.getPreviewWidth();
            height = mCameraController.getPreviewHeight();
        }
        mVideoInfo.setWidth(width);
        mVideoInfo.setHeight(height);
        mCameraRenderer.setTextureSize(width, height);
    }

    /**
     * 关闭相机
     */
    private void closeCamera() {
        mCameraController.closeCamera();
    }

    @Override
    public void takePicture() {
        mCameraRenderer.takePicture();
    }

    @Override
    public void switchCamera() {
        mCameraController.switchCamera();
    }

    @Override
    public void startRecord() {
        if (mOperateStarted) {
            return;
        }
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
        }
        try {
            mMediaRecorder = new CAVCaptureRecorder();
            mMediaRecorder.setOutputPath(generateOutputPath());
            mMediaRecorder.setVideoOutputPath(PathUtils.getVideoTempPath(mActivity));
            mMediaRecorder.setAudioOutputPath(PathUtils.getAudioTempPath(mActivity));
            mMediaRecorder.setOnCaptureRecordListener(this);
            mMediaRecorder.setSpeed(mSpeedMode.getSpeed());
            mMediaRecorder.setVideoInfo(mVideoInfo);
            mMediaRecorder.setAudioInfo(mAudioInfo);
            if (mEnableAudio) {
                mMediaRecorder.setAudioReader(new CAVCaptureAudioRecordInput(mAudioInfo));
            } else {
                mMediaRecorder.setAudioReader(new CAVCaptureAudioMuteInput(mAudioInfo));
            }
            mMediaRecorder.prepare();
            mMediaRecorder.startRecord();
            if (mWeakGLContext != null && mWeakGLContext.get() != null) {
                mMediaRecorder.initVideoRenderer(mWeakGLContext.get());
            }
        } catch (IOException e) {
            Log.e(TAG, "startRecord: ", e);
        }
        mOperateStarted = true;
    }

    @Override
    public void stopRecord() {
        if (!mOperateStarted) {
            return;
        }
        mOperateStarted = false;
        if (mMediaRecorder != null) {
            mMediaRecorder.stopRecord();
        }
    }

    @Override
    public void cancelRecord() {
        stopRecord();
    }

    @Override
    public boolean isRecording() {
        return (mOperateStarted && mMediaRecorder != null && mMediaRecorder.isRecording());
    }

    @Override
    public void setRecordAudioEnable(boolean enable) {
        mEnableAudio = enable;
    }

    @Override
    public void setRecordSeconds(int seconds) {
        mMaxDuration = mRemainDuration = seconds * SECOND_IN_US;
    }

    @Override
    public void setSpeedMode(SpeedMode mode) {
        mSpeedMode = mode;
    }

    @Override
    public void deleteLastVideo() {
        int index = mVideoList.size() - 1;
        if (index >= 0) {
            MediaInfo mediaInfo = mVideoList.get(index);
            String path = mediaInfo.getFileName();
            mRemainDuration += mediaInfo.getDuration();
            if (!TextUtils.isEmpty(path)) {
                FileUtils.deleteFile(path);
                mVideoList.remove(index);
            }
        }
        getTarget().deleteProgressSegment();
    }

    @Override
    public int getRecordedVideoSize() {
        return mVideoList.size();
    }

    @Override
    public void enableEdgeBlurFilter(boolean enable) {
        mCameraRenderer.changeEdgeBlur(enable);
    }

    @Override
    public void setMusicPath(String path) {
        mMusicPath = path;
    }

    @Override
    public void onOpenCameraSettingPage() {
        if (mActivity != null) {
            Intent intent = new Intent(mActivity, CameraSettingActivity.class);
            mActivity.startActivity(intent);
        }
    }

    /**
     * 相机打开回调
     */
    public void onCameraOpened() {
        Log.d(TAG, "onCameraOpened: " +
                "orientation - " + mCameraController.getOrientation()
                + "width - " + mCameraController.getPreviewWidth()
                + ", height - " + mCameraController.getPreviewHeight());
        FaceTracker.getInstance()
                .setBackCamera(!mCameraController.isFront())
                .prepareFaceTracker(mActivity,
                        mCameraController.getOrientation(),
                        mCameraController.getPreviewWidth(),
                        mCameraController.getPreviewHeight());
    }

    // ------------------------- Camera 输出SurfaceTexture准备完成回调 -------------------------------
    @Override
    public void onSurfaceTexturePrepared(@NonNull SurfaceTexture surfaceTexture) {
        onCameraOpened();
        mCameraRenderer.bindInputSurfaceTexture(surfaceTexture);
    }

    // ---------------------------------- 相机预览数据回调 ------------------------------------------
    @Override
    public void onPreviewFrame(byte[] data) {
        if (VERBOSE) {
            Log.d(TAG, "onPreviewFrame: width - " + mCameraController.getPreviewWidth()
                    + ", height - " + mCameraController.getPreviewHeight());
        }
        FaceTracker.getInstance()
                .trackFace(data, mCameraController.getPreviewWidth(),
                        mCameraController.getPreviewHeight());
    }

    // ---------------------------------- 人脸检测完成回调 ------------------------------------------
    @Override
    public void onTrackingFinish() {
        if (VERBOSE) {
            Log.d(TAG, "onTrackingFinish: ");
        }
        mCameraRenderer.requestRender();
    }

    // ------------------------------ SurfaceTexture帧可用回调 --------------------------------------
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//        mCameraRenderer.requestRender();
    }

    // ---------------------------------- 录制与合成 start ------------------------------------------
    /**
     * 开始录制
     */
    @Override
    public void onCaptureStart() {
        getTarget().hideOnRecording();
    }

    /**
     * 正在录制
     *
     * @param duration 录制的时长
     */
    @Override
    public void onCapturing(long duration) {
        float progress = duration * 1.0f / mMaxDuration;
        Log.d(TAG, "onCapturing: " + duration + ", remainDuration: " + mRemainDuration + ", progress: " + progress);
        getTarget().updateRecordProgress(progress);
        if (duration >= mRemainDuration) {
            stopRecord();
        }
    }

    /**
     * 录制回调
     *
     * @param path 视频路径
     */
    @Override
    public void onCaptureFinish(String path, long duration) {
        mVideoList.add(new MediaInfo(path, duration));
        mRemainDuration -= duration;
        float progress = duration * 1.0f / mMaxDuration;
        Log.d(TAG, "onCaptureFinish: " + progress);
        getTarget().addProgressSegment(progress);
        getTarget().resetAllLayout();
    }

    /**
     * 合并视频并跳转至编辑页面
     */
    public void mergeAndEdit() {
        if (mVideoList.size() < 1) {
            return;
        }

        if (mVideoList.size() == 1) {
            String path = mVideoList.get(0).getFileName();
            String outputPath = generateOutputPath();
            FileUtils.copyFile(path, outputPath);
            Intent intent = new Intent(mActivity, VideoEditActivity.class);
            intent.putExtra(VideoEditActivity.VIDEO_PATH, outputPath);
            mActivity.startActivity(intent);
        } else {
            getTarget().showConcatProgressDialog();
            List<String> videos = new ArrayList<>();
            for (MediaInfo info : mVideoList) {
                if (info != null && !TextUtils.isEmpty(info.getFileName())) {
                    videos.add(info.getFileName());
                }
            }
            String finalPath = generateOutputPath();
            mCommandEditor.execCommand(CAVCommandEditor.concatVideo(mActivity, videos, finalPath),
                    new CAVCommandEditor.CommandProcessCallback() {
                        @Override
                        public void onProcessing(int current) {
                            Log.d(TAG, "onProcessing: " + current);
                        }

                        @Override
                        public void onProcessResult(int result) {
                            getTarget().hideConcatProgressDialog();
                            if (result == 0) {
                                onOpenVideoEditPage(finalPath);
                            } else {
                                getTarget().showToast("合成失败");
                            }
                        }
                    });
        }
    }

    /**
     * 创建合成的视频文件名
     * @return
     */
    public String generateOutputPath() {
        return PathUtils.getVideoCachePath(mActivity);
    }

    /**
     * 打开视频编辑页面
     * @param path
     */
    public void onOpenVideoEditPage(String path) {
        if (mCameraParam.captureListener != null) {
            mCameraParam.captureListener.onMediaSelectedListener(path, OnPreviewCaptureListener.MediaTypeVideo);
        }
    }

    // ------------------------------------ 拍照截屏回调 --------------------------------------------

    @Override
    public void onCapture(Bitmap bitmap) {
        String filePath = PathUtils.getImageCachePath(mActivity);
        BitmapUtils.saveBitmap(filePath, bitmap);
        if (mCameraParam.captureListener != null) {
            mCameraParam.captureListener.onMediaSelectedListener(filePath, OnPreviewCaptureListener.MediaTypePicture);
        }
    }

    // ------------------------------------ 渲染fps回调 ------------------------------------------
    /**
     * fps数值回调
     * @param fps
     */
    @Override
    public void onFpsCallback(float fps) {
        if (getTarget() != null) {
            getTarget().showFps(fps);
        }
    }
}
