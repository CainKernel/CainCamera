package com.cgfay.caincamera.presenter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.media.AudioFormat;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.cgfay.caincamera.fragment.FFMediaRecordFragment;
import com.cgfay.camera.camera.CameraApi;
import com.cgfay.camera.camera.CameraController;
import com.cgfay.camera.camera.CameraXController;
import com.cgfay.camera.camera.ICameraController;
import com.cgfay.camera.camera.OnFrameAvailableListener;
import com.cgfay.camera.camera.OnSurfaceTextureListener;
import com.cgfay.camera.camera.PreviewCallback;
import com.cgfay.uitls.utils.PathUtils;

import com.cgfay.media.recorder.AVFormatter;
import com.cgfay.media.CAVCommandEditor;
import com.cgfay.media.recorder.FFMediaRecorder;
import com.cgfay.media.recorder.FFAudioRecorder;
import com.cgfay.uitls.utils.FileUtils;
import com.cgfay.video.activity.VideoEditActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用FFmpeg进行录制的Presenter
 */
public class FFMediaRecordPresenter implements PreviewCallback, FFAudioRecorder.OnRecordCallback, OnSurfaceTextureListener,
        OnFrameAvailableListener, FFMediaRecorder.OnRecordListener {

    private static final String TAG = "FFMediaRecordPresenter";
    private static final boolean VERBOSE = true;

    private FragmentActivity mActivity;
    private FFMediaRecordFragment mFragment;

    // 命令行编辑器
    private CAVCommandEditor mCommandEditor;

    // 最大时长(毫秒)
    private int mMaxDuration = 15 * 1000;
    private int mRemainDuration;

    private int mPreviewRotate;
    private int mRecordWidth;
    private int mRecordHeight;
    private FFAudioRecorder mFFAudioRecorder;
    private FFMediaRecorder mMediaRecorder;
    private boolean mIsRecording;
    private Handler mHandler;

    private final List<VideoInfo> mVideoList;

    private final ICameraController mCameraController;

    public FFMediaRecordPresenter(FragmentActivity activity, FFMediaRecordFragment fragment) {
        mActivity = activity;
        mFragment = fragment;
        mIsRecording = false;
        mPreviewRotate = 0;
        // 命令行编辑器
        mCommandEditor = new CAVCommandEditor();
        mFFAudioRecorder = new FFAudioRecorder();
        mFFAudioRecorder.setOnRecordCallback(this);
        mFFAudioRecorder.setSampleFormat(AudioFormat.ENCODING_PCM_16BIT);
        mHandler = new Handler(Looper.myLooper());
        mVideoList = new ArrayList<>();
        if (CameraApi.hasCamera2(mActivity)) {
            mCameraController = new CameraXController(mActivity, ContextCompat.getMainExecutor(activity));
        } else {
            mCameraController = new CameraController(mActivity);
        }
        mCameraController.setPreviewCallback(this);
        mCameraController.setOnFrameAvailableListener(this);
        mCameraController.setOnSurfaceTextureListener(this);
    }

    /**
     * 启动
     */
    public void onResume() {
        Log.d(TAG, "onResume: ");
        openCamera();
    }

    /**
     * 暂停
     */
    public void onPause() {
        Log.d(TAG, "onPause: ");
        closeCamera();
    }

    /**
     * 释放资源
     */
    public void release() {
        Log.d(TAG, "release: ");
        mActivity = null;
        if (mCommandEditor != null) {
            mCommandEditor.release();
            mCommandEditor = null;
        }
    }

    /**
     * 切换相机
     */
    public void switchCamera() {
        mCameraController.switchCamera();
    }

    /**
     * 录制时长
     * @param seconds
     */
    public void setRecordSeconds(int seconds) {
        mMaxDuration = seconds * 1000;
        mRemainDuration = mMaxDuration;
        Log.d(TAG, "setRecordSeconds: " + seconds + "s");
    }

    /**
     * 设置是否允许音频录制
     * @param enable
     */
    public void setAudioEnable(boolean enable) {
        Log.d(TAG, "setAudioEnable: " + enable);
    }

    /**
     * 开始录制
     */
    public void startRecord() {
        Log.d(TAG, "startRecord: ");
        if (mIsRecording) {
            Log.e(TAG, "startRecord: recording state is error");
            return;
        }
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
        }
        mPreviewRotate = mCameraController.getOrientation();
        // 上下翻转
        if (mCameraController instanceof CameraController && mCameraController.isFront()) {
            mPreviewRotate = 360 - mPreviewRotate;
        }
        Log.d(TAG, "startRecord: " + mPreviewRotate);
        mMediaRecorder = new FFMediaRecorder.RecordBuilder(generateOutputPath())
                .setVideoParams(mRecordWidth, mRecordHeight, AVFormatter.PIXEL_FORMAT_NV21, 30)
//                .setVideoFilter("lutyuv='u=128:v=128'") // 黑白滤镜
//                .setVideoFilter("noise=alls=20:allf=t+p") // 噪点
//                .setVideoFilter("boxblur") // 模糊
//                .setVideoFilter("boxblur=2:1:cr=0:ar=0") // 指定模糊参数
//                .setVideoFilter("hue='h=60:s=-3'") // 调整色调
//                .setVideoFilter("drawgrid=width=100:height=100:thickness=2:color=black@0.9") // 添加网格
//                .setVideoFilter("vignette=PI/4")  // 光晕
                .setRotate(mPreviewRotate)
                .setMirror(mCameraController.isFront())
                .setAudioParams(mFFAudioRecorder.getSampleRate(), AVFormatter.getSampleFormat(mFFAudioRecorder.getSampleFormat()), mFFAudioRecorder.getChannels())
                .create();
        mMediaRecorder.setRecordListener(this);
        mMediaRecorder.startRecord();
        mFFAudioRecorder.start();
    }

    /**
     * 停止录制
     */
    public void stopRecord() {
        if (VERBOSE) {
            Log.d(TAG, "stopRecord: ");
        }
        if (mIsRecording) {
            mIsRecording = false;
        }
        if (mMediaRecorder != null) {
            mMediaRecorder.stopRecord();
        }
        mFFAudioRecorder.stop();
    }

    /**
     * 音频录制完成
     */
    @Override
    public void onRecordFinish() {
        Log.d(TAG, "onRecordFinish: audio record finish");
    }

    /**
     * 录制一帧音频帧数据
     * @param data
     */
    @Override
    public void onRecordSample(byte[] data) {
        if (mIsRecording) {
            if (mMediaRecorder != null) {
                mMediaRecorder.recordAudioFrame(data, data.length);
            }
        }
    }

    /**
     * 是否正在录制
     * @return
     */
    public boolean isRecording() {
        return mIsRecording;
    }

    @Override
    public void onRecordStart() {
        if (VERBOSE) {
            Log.d(TAG, "onRecordStart: ");
        }
        mFragment.hidViews();
        mIsRecording = true;
    }

    @Override
    public void onRecording(float duration) {
        if (VERBOSE) {
            Log.d(TAG, "onRecording: " + duration);
        }
        float progress = duration / mMaxDuration;
        mFragment.setProgress(progress);
        if (duration > mRemainDuration) {
            stopRecord();
        }
    }

    @Override
    public void onRecordFinish(boolean success, float duration) {
        mIsRecording = false;
        if (mMediaRecorder != null) {
            if (success) {
                mVideoList.add(new VideoInfo(mMediaRecorder.getOutput(), duration));
                mRemainDuration = mRemainDuration - (int)duration;
                float currentProgress = duration / mMaxDuration;
                mFragment.addProgressSegment(currentProgress);
            }
        }
        mFragment.showViews();
        mFragment.showToast("录制成功");
        if (VERBOSE) {
            Log.d(TAG, "onRecordFinish: " + success + ", duration：" + duration);
        }
    }

    @Override
    public void onRecordError(String msg) {
        if (VERBOSE) {
            Log.d(TAG, "onRecordError: ");
        }
        mFragment.showToast(msg);
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    @Override
    public void onSurfaceTexturePrepared(@NonNull SurfaceTexture surfaceTexture) {
        mFragment.bindSurfaceTexture(surfaceTexture);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mFragment.onFrameAvailable();
    }

    @Override
    public void onPreviewFrame(byte[] data) {
        if (mMediaRecorder != null && mIsRecording) {
            mHandler.post(() -> mMediaRecorder.recordVideoFrame(data, data.length,
                    mRecordWidth, mRecordHeight,
                    AVFormatter.PIXEL_FORMAT_NV21));
        }
    }

    /**
     * 删除上一段视频
     */
    public void deleteLastVideo() {
        if (VERBOSE) {
            Log.d(TAG, "deleteLastVideo: ");
        }
        int index = mVideoList.size() - 1;
        if (index >= 0) {
            VideoInfo videoInfo = mVideoList.get(index);
            String path = videoInfo.getFileName();
            mRemainDuration += videoInfo.getDuration();
            if (!TextUtils.isEmpty(path)) {
                FileUtils.deleteFile(path);
                mVideoList.remove(index);
            }
        }
        mFragment.deleteProgressSegment();
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

        mPreviewRotate = mCameraController.getOrientation();
        mRecordWidth = mCameraController.getPreviewWidth();
        mRecordHeight = mCameraController.getPreviewHeight();

        if (mPreviewRotate == 90 || mPreviewRotate == 270) {
            width = mRecordHeight;
            height = mRecordWidth;
        } else {
            width = mRecordWidth;
            height = mRecordHeight;
        }

        mFragment.updateTextureSize(width, height);
    }

    /**
     * 释放资源
     */
    private void closeCamera() {
        mCameraController.closeCamera();
        if (VERBOSE) {
            Log.d(TAG, "closeCamera: ");
        }
    }

    /**
     * 合并视频并跳转至编辑页面
     */
    public void mergeAndEdit() {
        if (VERBOSE) {
            Log.d(TAG, "mergeAndEdit: ");
        }
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
            mFragment.showProgressDialog();
            List<String> videos = new ArrayList<>();
            for (VideoInfo info : mVideoList) {
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
                            mFragment.hideProgressDialog();
                            if (result == 0) {
                                Intent intent = new Intent(mActivity, VideoEditActivity.class);
                                intent.putExtra(VideoEditActivity.VIDEO_PATH, finalPath);
                                mActivity.startActivity(intent);
                            } else {
                                mFragment.showToast("合成失败");
                            }
                        }
                    });
        }
    }

    /**
     * 获取Activity
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

    /**
     * 视频信息
     */
    public class VideoInfo {

        private String fileName;
        private float duration;

        public VideoInfo(String fileName, float duration) {
            this.fileName = fileName;
            this.duration = duration;
        }

        public float getDuration() {
            return duration;
        }

        public String getFileName() {
            return fileName;
        }
    }
}
