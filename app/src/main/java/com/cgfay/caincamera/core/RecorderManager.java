package com.cgfay.caincamera.core;

import android.util.Log;

import com.cgfay.caincamera.gles.EglCore;
import com.cgfay.caincamera.gles.WindowSurface;
import com.cgfay.caincamera.multimedia.MediaAudioEncoder;
import com.cgfay.caincamera.multimedia.MediaEncoder;
import com.cgfay.caincamera.multimedia.MediaMuxerWrapper;
import com.cgfay.caincamera.multimedia.MediaVideoEncoder;

import java.io.File;
import java.io.IOException;

/**
 * 视频录制管理器
 * Created by cain.huang on 2017/11/3.
 */

public final class RecorderManager {

    private static final String TAG = "RecorderManager";

    private static RecorderManager mInstance;

    // 录制比特率
    private int mRecordBitrate;
    // 录制帧率
    private int mFrameRate = 25;
    // 像素资料量
    private int mBPP = 4;

    // 是否允许高清视频
    private boolean mEnableHD = false;
    // 码率乘高清值
    private int HDValue = 4;

    // 视频宽度
    private int mVideoWidth;
    // 视频高度
    private int mVideoHeight;

    // 录制视频用的EGLSurface
    private WindowSurface mRecordWindowSurface;

    private MediaMuxerWrapper mMuxer;

    // 录制文件路径
    private String mRecorderOutputPath = null;

    // 是否允许录音
    private boolean isEnableAudioRecording = true;

    public static RecorderManager getInstance() {
        if (mInstance == null) {
            mInstance = new RecorderManager();
        }
        return mInstance;
    }

    private RecorderManager() {

    }

    /**
     * 初始化录制器，此时耗时大约280ms左右
     * 如果放在渲染线程里执行，会导致一开始录制出来的视频开头严重掉帧
     * @param width
     * @param height
     */
    synchronized public void initRecorder(int width, int height) {
        initRecorder(width, height, null);
    }

    /**
     * 初始化录制器，耗时大约208ms左右
     * 如果放在渲染线程里面执行，会导致一开始录制出来的视频开头严重掉帧
     * @param width
     * @param height
     * @param listener
     */
    synchronized public void initRecorder(int width, int height,
                                          MediaEncoder.MediaEncoderListener listener) {
        mVideoWidth = width;
        mVideoHeight = height;
        RenderManager.getInstance().setVideoSize(mVideoWidth, mVideoHeight);
        // 如果路径为空，则生成默认的路径
        if (mRecorderOutputPath == null || mRecorderOutputPath.isEmpty()) {
            mRecorderOutputPath = ParamsManager.VideoPath
                    + "CainCamera_" + System.currentTimeMillis() + ".mp4";
            Log.d(TAG, "the outpath is empty, auto-created path is : " + mRecorderOutputPath);
        }
        File file = new File(mRecorderOutputPath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        // 计算帧率
        mRecordBitrate = width * height * mFrameRate / mBPP;
        if (mEnableHD) {
            mRecordBitrate *= HDValue;
        }
        try {

            mMuxer = new MediaMuxerWrapper(file.getAbsolutePath());
            new MediaVideoEncoder(mMuxer, listener, mVideoWidth, mVideoHeight);
            if (isEnableAudioRecording) {
                new MediaAudioEncoder(mMuxer, listener);
            }

            mMuxer.prepare();
        } catch (IOException e) {
            Log.e(TAG, "startRecording:", e);
        }
    }

    /**
     * 开始录制
     */
    synchronized void startRecording(EglCore eglCore) {
        mRecordWindowSurface = new WindowSurface(eglCore,
                ((MediaVideoEncoder) mMuxer.getVideoEncoder()).getInputSurface(),
                true);
        RenderManager.getInstance().initRecordingFilter();
        if (mMuxer != null) {
            mMuxer.startRecording();
        }
    }

    /**
     * 帧可用时调用
     */
    public void frameAvailable() {
        if (mMuxer != null && mMuxer.getVideoEncoder() != null) {
            mMuxer.getVideoEncoder().frameAvailableSoon();
        }
    }

    /**
     * 发送渲染指令
     * @param timeStamp 时间戳
     */
    public void drawRecorderFrame(long timeStamp) {
        if (mRecordWindowSurface != null) {
            mRecordWindowSurface.makeCurrent();
            RenderManager.getInstance().drawRecordingFrame();
            mRecordWindowSurface.setPresentationTime(timeStamp);
            mRecordWindowSurface.swapBuffers();
        }
    }

    /**
     * 停止录制
     */
    synchronized void stopRecording() {
        if (mMuxer != null) {
            mMuxer.stopRecording();
            mMuxer = null;
        }
        if (mRecordWindowSurface != null) {
            mRecordWindowSurface.release();
            mRecordWindowSurface = null;
        }
        RenderManager.getInstance().releaseRecordingFilter();
    }

    /**
     * 设置视频帧率
     * @param frameRate
     */
    public void setFrameRate(int frameRate) {
        mFrameRate = frameRate;
    }

    /**
     * 是否允许录制高清视频
     * @param enable
     */
    public void enableHighDefinition(boolean enable) {
        mEnableHD = enable;
    }


    /**
     * 是否允许录音
     * @param enable
     */
    public void setEnableAudioRecording(boolean enable) {
        isEnableAudioRecording = enable;
    }

    /**
     * 获取输出路径
     * @return
     */
    public String getOutputPath() {
        return mRecorderOutputPath;
    }

    /**
     * 设置输出路径
     * @param path
     * @return
     */
    public void setOutputPath(String path) {
        mRecorderOutputPath = null;
    }
}
