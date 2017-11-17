package com.cgfay.caincamera.core;

import android.util.Log;

import com.cgfay.caincamera.gles.EglCore;
import com.cgfay.caincamera.gles.WindowSurface;
import com.cgfay.caincamera.multimedia.MediaAudioEncoder;
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
//    private VideoEncoderCore mVideoEncoder;

    private MediaMuxerWrapper mMuxer;


    public static RecorderManager getInstance() {
        if (mInstance == null) {
            mInstance = new RecorderManager();
        }
        return mInstance;
    }

    private RecorderManager() {

    }

    /**
     * 开始录制
     */
    synchronized void startRecording(EglCore eglCore, int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;

        File file = new File(ParamsManager.VideoPath
                + "CainCamera_" + System.currentTimeMillis() + ".mp4");
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
            new MediaVideoEncoder(mMuxer, null, mVideoWidth, mVideoHeight);
            new MediaAudioEncoder(mMuxer, null);

            mMuxer.prepare();
            mMuxer.startRecording();

        } catch (IOException e) {
            Log.e(TAG, "startRecording:", e);
        }
        mRecordWindowSurface = new WindowSurface(eglCore,
                ((MediaVideoEncoder) mMuxer.getVideoEncoder()).getInputSurface(),
                true);

        RenderManager.getInstance().initRecordingFilter();
    }

    /**
     * 发送渲染指令
     * @param timeStamp 时间戳
     */
    public void drawRecorderFrame(long timeStamp) {
        mMuxer.getVideoEncoder().frameAvailableSoon();
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
}
