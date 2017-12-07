package com.cgfay.caincamera.multimedia;

import android.opengl.EGLContext;
import android.util.Log;

import com.cgfay.caincamera.core.ParamsManager;
import com.cgfay.caincamera.core.RenderManager;
import com.cgfay.caincamera.gles.EglCore;
import com.cgfay.caincamera.gles.WindowSurface;

import java.io.File;
import java.io.IOException;

/**
 * 录制编码管理器
 * Created by cain.huang on 2017/12/7.
 */

public class EncoderManager {

    private static final String TAG = "EncoderManager";

    private static EncoderManager mInstance;

    // 录制比特率
    private int mRecordBitrate;
    // 录制帧率
    private int mFrameRate = 25;
    // 像素资料量
    private int mBPP = 4;

    // 是否允许高清视频
    private boolean mEnableHD = false;
    // 码率乘高清值
    private int HDValue = 16;

    // 视频宽度
    private int mVideoWidth;
    // 视频高度
    private int mVideoHeight;

    private EglCore mEglCore;
    // 录制视频用的EGLSurface
    private WindowSurface mRecordWindowSurface;

    // 复用器管理器
    private MediaMuxerWrapper mMuxerManager;

    // 录制文件路径
    private String mRecorderOutputPath = null;

    // 是否允许录音
    private boolean isEnableAudioRecording = true;

    // 是否处于录制状态
    private boolean isRecording = false;

    public static EncoderManager getInstance() {
        if (mInstance == null) {
            mInstance = new EncoderManager();
        }
        return mInstance;
    }

    private EncoderManager() {

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

            mMuxerManager = new MediaMuxerWrapper(file.getAbsolutePath());
            new MediaVideoEncoder(mMuxerManager, listener, mVideoWidth, mVideoHeight);
            if (isEnableAudioRecording) {
                new MediaAudioEncoder(mMuxerManager, listener);
            }

            mMuxerManager.prepare();
        } catch (IOException e) {
            Log.e(TAG, "startRecording:", e);
        }
    }

    /**
     * 开始录制，共享EglContext实现多线程录制
     */
    public synchronized void startRecording(EGLContext eglContext) {
        if (mMuxerManager.getVideoEncoder() == null) {
            return;
        }
        // 释放之前的Egl
        if (mRecordWindowSurface != null) {
            mRecordWindowSurface.releaseEglSurface();
        }
        if (mEglCore != null) {
            mEglCore.release();
        }
        // 重新创建一个EglContext 和 Window Surface
        mEglCore = new EglCore(eglContext, EglCore.FLAG_RECORDABLE);
        if (mRecordWindowSurface != null) {
            mRecordWindowSurface.recreate(mEglCore);
        } else {
            mRecordWindowSurface = new WindowSurface(mEglCore,
                    ((MediaVideoEncoder) mMuxerManager.getVideoEncoder()).getInputSurface(),
                    true);
        }
        mRecordWindowSurface.makeCurrent();
        RenderManager.getInstance().initRecordingFilter();
        if (mMuxerManager != null) {
            mMuxerManager.startRecording();
        }
        isRecording = true;
    }

    /**
     * 帧可用时调用
     */
    public void frameAvailable() {
        if (mMuxerManager != null && mMuxerManager.getVideoEncoder() != null && isRecording) {
            mMuxerManager.getVideoEncoder().frameAvailableSoon();
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
    public synchronized void stopRecording() {
        isRecording = false;
        if (mMuxerManager != null) {
            mMuxerManager.stopRecording();
            mMuxerManager = null;
        }
        if (mRecordWindowSurface != null) {
            mRecordWindowSurface.release();
            mRecordWindowSurface = null;
        }
        RenderManager.getInstance().releaseRecordingFilter();
    }

    /**
     * 暂停录制
     */
    public synchronized void pauseRecording() {
        if (mMuxerManager != null && isRecording) {
            mMuxerManager.pauseRecording();
        }
    }

    /**
     * 继续录制
     */
    public synchronized void continueRecording() {
        if (mMuxerManager != null && isRecording) {
            mMuxerManager.continueRecording();
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


    /**
     * 是否允许录音
     * @param enable
     */
    public void setEnableAudioRecording(boolean enable) {
        isEnableAudioRecording = enable;
    }

    /**
     * 设置输出路径
     * @param path
     * @return
     */
    public void setOutputPath(String path) {
        mRecorderOutputPath = path;
    }
}
