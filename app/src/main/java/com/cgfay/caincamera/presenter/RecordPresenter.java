package com.cgfay.caincamera.presenter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import android.text.TextUtils;
import android.util.Log;

import com.cgfay.caincamera.activity.SpeedRecordActivity;
import com.cgfay.camera.camera.CameraApi;
import com.cgfay.camera.camera.CameraController;
import com.cgfay.camera.camera.CameraXController;
import com.cgfay.camera.camera.ICameraController;
import com.cgfay.camera.camera.OnFrameAvailableListener;
import com.cgfay.camera.camera.OnSurfaceTextureListener;
import com.cgfay.cavfoundation.capture.CAVCaptureAudioMuteInput;
import com.cgfay.cavfoundation.capture.CAVCaptureAudioRecordInput;
import com.cgfay.cavfoundation.capture.CAVCaptureRecorder;
import com.cgfay.cavfoundation.capture.CAVCaptureRecordListener;
import com.cgfay.cavfoundation.codec.CAVAudioInfo;
import com.cgfay.cavfoundation.codec.CAVVideoInfo;
import com.cgfay.media.recorder.MediaInfo;
import com.cgfay.uitls.utils.PathUtils;
import com.cgfay.media.recorder.SpeedMode;
import com.cgfay.media.CAVCommandEditor;
import com.cgfay.uitls.utils.FileUtils;
import com.cgfay.video.activity.VideoEditActivity;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 录制器的presenter
 * @author CainHuang
 * @date 2019/7/7
 */
public class RecordPresenter implements OnSurfaceTextureListener, OnFrameAvailableListener, CAVCaptureRecordListener {

    private static final String TAG = "RecordPresenter";

    public static final int SECOND_IN_US = 1000000;

    private SpeedRecordActivity mActivity;

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

    // 相机控制器
    private final ICameraController mCameraController;

    // EGL上下文
    private WeakReference<EGLContext> mWeakGLContext;

    public RecordPresenter(SpeedRecordActivity activity) {
        mActivity = activity;

        // 命令行编辑器
        mCommandEditor = new CAVCommandEditor();

        // 创建相机控制器
        if (CameraApi.hasCamera2(mActivity)) {
            mCameraController = new CameraXController(activity, ContextCompat.getMainExecutor(activity));
        } else {
            mCameraController = new CameraController(activity);
        }
        mCameraController.setOnFrameAvailableListener(this);
        mCameraController.setOnSurfaceTextureListener(this);

        mAudioInfo = new CAVAudioInfo();
        mVideoInfo = new CAVVideoInfo();
        mSpeedMode = SpeedMode.MODE_NORMAL;
        mEnableAudio = true;
    }

    /**
     * 启动
     */
    public void onResume() {
        openCamera();
    }

    /**
     * 暂停
     */
    public void onPause() {
        closeCamera();
    }

    /**
     * 切换相机
     */
    public void switchCamera() {
        mCameraController.switchCamera();
    }

    /**
     * 释放资源
     */
    public void release() {
        mActivity = null;
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        if (mCommandEditor != null) {
            mCommandEditor.release();
            mCommandEditor = null;
        }
    }

    /**
     * 设置速度模式
     * @param mode
     */
    public void setSpeedMode(SpeedMode mode) {
        mSpeedMode = mode;
    }

    /**
     * 设置录制时长
     * @param seconds
     */
    public void setRecordSeconds(int seconds) {
        mMaxDuration = mRemainDuration = seconds * SECOND_IN_US;
    }

    /**
     * 设置是否允许音频录制
     * @param enable
     */
    public void setAudioEnable(boolean enable) {
        mEnableAudio = enable;
    }

    /**
     * 开始录制
     */
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
            Log.d(TAG, "startRecord: aaa");
            if (mWeakGLContext != null && mWeakGLContext.get() != null) {
                mMediaRecorder.initVideoRenderer(mWeakGLContext.get());
            } else {
                Log.d(TAG, "startRecord: failed to init renderer" );
            }
        } catch (IOException e) {
            Log.e(TAG, "startRecord: ", e);
        }
        mOperateStarted = true;
    }

    /**
     * 停止录制
     */
    public void stopRecord() {
        if (!mOperateStarted) {
            return;
        }
        mOperateStarted = false;
        if (mMediaRecorder != null) {
            mMediaRecorder.stopRecord();
        }
    }

    /**
     * 开始录制
     */
    @Override
    public void onCaptureStart() {
        mActivity.hidViews();
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
        mActivity.setProgress(progress);
        if (duration >= mRemainDuration) {
            stopRecord();
        }
    }

    /**
     * 录制回调
     *
     * @param path     视频路径
     * @param duration
     */
    @Override
    public void onCaptureFinish(String path, long duration) {
        mVideoList.add(new MediaInfo(path, duration));
        mRemainDuration -= duration;
        float progress = duration * 1.0f / mMaxDuration;
        Log.d(TAG, "onCaptureFinish: " + progress);
        mActivity.addProgressSegment(progress);
        mActivity.showViews();
        mOperateStarted = false;
    }

    /**
     * 绑定EGLContext
     * @param context
     */
    public void onBindSharedContext(EGLContext context) {
        mWeakGLContext = new WeakReference<>(context);
        Log.d(TAG, "onBindSharedContext: " );
    }

    /**
     * 录制帧可用
     * @param texture
     * @param timestamp
     */
    public void onRecordFrameAvailable(int texture, long timestamp) {
        if (mOperateStarted && mMediaRecorder != null && mMediaRecorder.isRecording()) {
            mMediaRecorder.renderFrame(texture, timestamp);
        }
    }

    @Override
    public void onSurfaceTexturePrepared(@NonNull SurfaceTexture surfaceTexture) {
        mActivity.bindSurfaceTexture(surfaceTexture);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mActivity.onFrameAvailable();
    }

    /**
     * 删除上一段视频
     */
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
        mActivity.deleteProgressSegment();
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
        mActivity.updateTextureSize(width, height);
    }

    /**
     * 释放资源
     */
    private void closeCamera() {
        mCameraController.closeCamera();
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
            mActivity.showProgressDialog();
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
                            mActivity.hideProgressDialog();
                            if (result == 0) {
                                Intent intent = new Intent(mActivity, VideoEditActivity.class);
                                intent.putExtra(VideoEditActivity.VIDEO_PATH, finalPath);
                                mActivity.startActivity(intent);
                            } else {
                                mActivity.showToast("合成失败");
                            }
                        }
                    });
        }
    }


    /**
     * 获取绑定的Activity
     * @return
     */
    public Activity getActivity() {
        return mActivity;
    }

    /**
     * 获取录制视频的段数
     * @return
     */
    public int getRecordVideoSize() {
        return mVideoList.size();
    }

    /**
     * 创建合成的视频文件名
     * @return
     */
    public String generateOutputPath() {
        return PathUtils.getVideoCachePath(mActivity);
    }

}
