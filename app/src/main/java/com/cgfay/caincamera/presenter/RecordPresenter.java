package com.cgfay.caincamera.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.cgfay.caincamera.activity.SpeedRecordActivity;
import com.cgfay.filter.glfilter.color.bean.DynamicColor;
import com.cgfay.filter.glfilter.resource.FilterHelper;
import com.cgfay.filter.glfilter.resource.ResourceJsonCodec;
import com.cgfay.filter.recorder.MediaInfo;
import com.cgfay.filter.recorder.VideoCommandQueue;
import com.cgfay.filter.recorder.AudioParams;
import com.cgfay.filter.recorder.MediaRecorder;
import com.cgfay.camera.engine.camera.CameraEngine;
import com.cgfay.camera.engine.camera.CameraParam;
import com.cgfay.camera.utils.PathConstraints;
import com.cgfay.filter.recorder.MediaType;
import com.cgfay.filter.recorder.OnRecordStateListener;
import com.cgfay.filter.recorder.RecordInfo;
import com.cgfay.filter.recorder.SpeedMode;
import com.cgfay.filter.recorder.VideoParams;
import com.cgfay.uitls.utils.FileUtils;
import com.cgfay.video.activity.VideoEditActivity;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;
import java.util.List;

/**
 * 录制器的presenter
 * @author CainHuang
 * @date 2019/7/7
 */
public class RecordPresenter implements SurfaceTexture.OnFrameAvailableListener, OnRecordStateListener {

    private static final String TAG = "RecordPresenter";

    private SpeedRecordActivity mActivity;

    // 音视频参数
    private final VideoParams mVideoParams;
    private final AudioParams mAudioParams;
    // 录制操作开始
    private boolean mOperateStarted = false;

    // 当前录制进度
    private float mCurrentProgress;
    // 最大时长
    private long mMaxDuration;
    // 剩余时长
    private long mRemainDuration;

    // 视频编码器
    private MediaRecorder mMediaRecorder;

    // 视频列表
    private List<MediaInfo> mVideoList = new ArrayList<>();

    // 录制音频信息
    private RecordInfo mAudioInfo;
    // 录制视频信息
    private RecordInfo mVideoInfo;

    // 视频合成命令队列
    private VideoCommandQueue mCommandQueue;

    public RecordPresenter(SpeedRecordActivity activity) {
        mActivity = activity;

        // 创建视频流
        mMediaRecorder = new MediaRecorder(this);

        // 视频参数
        mVideoParams = new VideoParams();
        mVideoParams.setVideoPath(getVideoTempPath(mActivity));

        // 音频参数
        mAudioParams = new AudioParams();
        mAudioParams.setAudioPath(getAudioTempPath(mActivity));

        // 命令队列
        mCommandQueue = new VideoCommandQueue();
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
        releaseCamera();
    }

    /**
     * 释放资源
     */
    public void release() {
        mActivity = null;
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
        }
        if (mCommandQueue != null) {
            mCommandQueue.release();
            mCommandQueue = null;
        }
    }

    /**
     * 设置速度模式
     * @param mode
     */
    public void setSpeedMode(SpeedMode mode) {
        mVideoParams.setSpeedMode(mode);
        mAudioParams.setSpeedMode(mode);
    }

    /**
     * 设置录制时长
     * @param seconds
     */
    public void setRecordSeconds(int seconds) {
        mMaxDuration = mRemainDuration = seconds * MediaRecorder.SECOND_IN_US;
        mVideoParams.setMaxDuration(mMaxDuration);
        mAudioParams.setMaxDuration(mMaxDuration);
    }

    /**
     * 设置是否允许音频录制
     * @param enable
     */
    public void setAudioEnable(boolean enable) {
        if (mMediaRecorder != null) {
            mMediaRecorder.setEnableAudio(enable);
        }
    }

    /**
     * 开始录制
     */
    public void startRecord() {
        if (mOperateStarted) {
            return;
        }
        mMediaRecorder.startRecord(mVideoParams, mAudioParams);
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
        mMediaRecorder.stopRecord();
    }

    @Override
    public void onRecordStart() {
        mActivity.hidViews();
    }

    @Override
    public void onRecording(long duration) {
        float progress = duration * 1.0f / mVideoParams.getMaxDuration();
        mActivity.setProgress(progress);
        if (duration > mRemainDuration) {
            stopRecord();
        }
    }

    @Override
    public void onRecordFinish(RecordInfo info) {
        if (info.getType() == MediaType.AUDIO) {
            mAudioInfo = info;
        } else if (info.getType() == MediaType.VIDEO) {
            mVideoInfo = info;
            mCurrentProgress = info.getDuration() * 1.0f / mVideoParams.getMaxDuration();
        }
        mActivity.showViews();
        if (mMediaRecorder.enableAudio() && (mAudioInfo == null || mVideoInfo == null)) {
            return;
        }
        if (mMediaRecorder.enableAudio()) {
            final String currentFile = generateOutputPath();
            FileUtils.createFile(currentFile);
            mCommandQueue.execCommand(VideoCommandQueue.mergeAudioVideo(mVideoInfo.getFileName(),
                    mAudioInfo.getFileName(), currentFile),
                    (result) -> {
                        if (result == 0) {
                            mVideoList.add(new MediaInfo(currentFile, mVideoInfo.getDuration()));
                            mRemainDuration -= mVideoInfo.getDuration();
                            mActivity.addProgressSegment(mCurrentProgress);
                            mActivity.showViews();
                            mCurrentProgress = 0;
                        }
                        // 删除旧的文件
                        FileUtils.deleteFile(mAudioInfo.getFileName());
                        FileUtils.deleteFile(mVideoInfo.getFileName());
                        mAudioInfo = null;
                        mVideoInfo = null;

                        // 如果剩余时间为0
                        if (mRemainDuration <= 0) {
                            mergeAndEdit();
                        }
                    });
        } else {
            if (mVideoInfo != null) {
                final String currentFile = generateOutputPath();
                FileUtils.moveFile(mVideoInfo.getFileName(), currentFile);
                mVideoList.add(new MediaInfo(currentFile, mVideoInfo.getDuration()));
                mRemainDuration -= mVideoInfo.getDuration();
                mAudioInfo = null;
                mVideoInfo = null;
                mActivity.addProgressSegment(mCurrentProgress);
                mActivity.showViews();
                mCurrentProgress = 0;
            }
        }
    }

    /**
     * SurfaceTexture创建成功回调
     * @param surfaceTexture
     */
    public void onBindSurfaceTexture(SurfaceTexture surfaceTexture) {
        CameraEngine.getInstance().setPreviewSurface(surfaceTexture);
        if (surfaceTexture != null) {
            surfaceTexture.setOnFrameAvailableListener(this);
        }
    }

    /**
     * 绑定EGLContext
     * @param context
     */
    public void onBindSharedContext(EGLContext context) {
        mVideoParams.setEglContext(context);
    }

    /**
     * 录制帧可用
     * @param texture
     * @param timestamp
     */
    public void onRecordFrameAvailable(int texture, long timestamp) {
        if (mOperateStarted && mMediaRecorder != null && mMediaRecorder.isRecording()) {
            mMediaRecorder.frameAvailable(texture, timestamp);
        }
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
        releaseCamera();
        CameraParam.getInstance().setBackCamera(true);
        CameraEngine.getInstance().openCamera(mActivity);
        calculateImageSize();
        CameraEngine.getInstance().startPreview();
    }

    /**
     * 计算imageView 的宽高
     */
    private void calculateImageSize() {
        int width;
        int height;
        if (CameraParam.getInstance().orientation == 90 || CameraParam.getInstance().orientation == 270) {
            width = CameraParam.getInstance().previewHeight;
            height = CameraParam.getInstance().previewWidth;
        } else {
            width = CameraParam.getInstance().previewWidth;
            height = CameraParam.getInstance().previewHeight;
        }
        mVideoParams.setVideoSize(width, height);
        mActivity.updateTextureSize(width, height);
    }

    /**
     * 释放资源
     */
    private void releaseCamera() {
        CameraEngine.getInstance().releaseCamera();
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
            mCommandQueue.execCommand(VideoCommandQueue.mergeVideo(mActivity, videos, finalPath),
                    (result) -> {
                        mActivity.hideProgressDialog();
                        if (result == 0) {
                            Intent intent = new Intent(mActivity, VideoEditActivity.class);
                            intent.putExtra(VideoEditActivity.VIDEO_PATH, finalPath);
                            mActivity.startActivity(intent);
                        } else {
                            mActivity.showToast("合成失败");
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
    public int getRecordVideos() {
        return mVideoList.size();
    }

    /**
     * 创建合成的视频文件名
     * @return
     */
    public String generateOutputPath() {
        return PathConstraints.getVideoCachePath(mActivity);
    }

    /**
     * 获取音频缓存绝对路径
     * @param context
     * @return
     */
    private static String getAudioTempPath(@NonNull Context context) {
        String directoryPath;
        // 判断外部存储是否可用，如果不可用则使用内部存储路径
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            directoryPath = context.getExternalCacheDir().getAbsolutePath();
        } else { // 使用内部存储缓存目录
            directoryPath = context.getCacheDir().getAbsolutePath();
        }
        String path = directoryPath + File.separator + "temp.aac";
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return path;
    }

    /**
     * 获取视频缓存绝对路径
     * @param context
     * @return
     */
    private static String getVideoTempPath(@NonNull Context context) {
        String directoryPath;
        // 判断外部存储是否可用，如果不可用则使用内部存储路径
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            directoryPath = context.getExternalCacheDir().getAbsolutePath();
        } else { // 使用内部存储缓存目录
            directoryPath = context.getCacheDir().getAbsolutePath();
        }
        String path = directoryPath + File.separator + "temp.mp4";
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return path;
    }

}
