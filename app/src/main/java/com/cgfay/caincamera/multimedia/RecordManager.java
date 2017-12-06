package com.cgfay.caincamera.multimedia;

import android.util.Log;

import com.cgfay.caincamera.core.ParamsManager;
import com.cgfay.caincamera.core.RenderManager;
import com.cgfay.caincamera.gles.EglCore;
import com.cgfay.caincamera.gles.WindowSurface;

import java.io.File;

/**
 * 录制管理器
 * Created by cain.huang on 2017/12/6.
 */

public class RecordManager {

    private static final String TAG = "RecordManager";

    public static final int RECORD_WIDTH = 540;
    public static final int RECORD_HEIGHT = 960;

    private static RecordManager mInstance;

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

    // 录制视频用的EGLSurface
    private WindowSurface mRecordWindowSurface;

    // 录制文件路径
    private String mRecorderOutputPath = null;

    // 是否允许录音
    private boolean isEnableAudioRecording = true;

    private MediaEncoderCore mMediaEncoder;

    public static RecordManager getInstance() {
        if (mInstance == null) {
            mInstance = new RecordManager();
        }
        return mInstance;
    }

    private RecordManager() {

    }

    /**
     * 初始化录制器
     * 如果放在渲染线程里面执行，会导致一开始录制出来的视频开头严重掉帧
     * @param width
     * @param height
     * @param listener
     */
    synchronized public void initRecorder(int width, int height,
                                          MediaEncoderCore.EncoderStateListener listener) {
        mVideoWidth = width;
        mVideoHeight = height;
        RenderManager.getInstance().setVideoSize(mVideoWidth, mVideoHeight);
        // 如果路径为空，则生成默认的路径
        if (mRecorderOutputPath == null || mRecorderOutputPath.isEmpty()) {
            mRecorderOutputPath = ParamsManager.VideoPath
                    + "CainCamera_" + System.currentTimeMillis() + ".mp4";
            Log.d(TAG, "the outpath is empty, auto-created path is : " + mRecorderOutputPath);
        }
        // 计算父目录是否存在，如果不存在则
        File file = new File(mRecorderOutputPath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        // 计算帧率
        mRecordBitrate = width * height * mFrameRate / mBPP;
        if (mEnableHD) {
            mRecordBitrate *= HDValue;
        }
        if (mMediaEncoder == null) {
            throw new RuntimeException("MediaEncoder is not Empty.");
        }
        mMediaEncoder = new MediaEncoderCore(width, height, mRecordBitrate, mEnableHD);
        mMediaEncoder.setEncoderStateListener(listener);
        mMediaEncoder.prepare(mRecorderOutputPath);
    }

    /**
     * 开始录制
     */
    synchronized void startRecording(EglCore eglCore) {
        mRecordWindowSurface = new WindowSurface(eglCore,
                mMediaEncoder.getInputSurface(),
                true);
        RenderManager.getInstance().initRecordingFilter();
        if (mMediaEncoder != null) {
            mMediaEncoder.startRecording();
        }
    }

    /**
     * 帧可用时调用
     */
    public void frameAvailable() {
        if (mMediaEncoder != null) {
            mMediaEncoder.frameAvailable();
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
        if (mMediaEncoder != null) {
            mMediaEncoder.stopRecording();
            mMediaEncoder = null;
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
        mRecorderOutputPath = path;
    }
}
