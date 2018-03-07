package com.cgfay.cainfilter.multimedia;

import android.opengl.EGLContext;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

import com.cgfay.cainfilter.core.ParamsManager;
import com.cgfay.cainfilter.gles.EglCore;
import com.cgfay.cainfilter.gles.WindowSurface;
import com.cgfay.cainfilter.glfilter.base.GLDisplayFilter;
import com.cgfay.cainfilter.type.ScaleType;
import com.cgfay.cainfilter.utils.GlUtil;

import java.io.File;
import java.io.IOException;

/**
 * 录制编码管理器
 * Created by cain.huang on 2017/12/7.
 */

public class EncoderManager {

    private static final String TAG = "EncoderManager";
    private static final boolean VERBOSE = false;

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

    // 渲染Texture的宽度
    private int mTextureWidth;
    // 渲染Texture的高度
    private int mTextureHeight;
    // 视频宽度
    private int mVideoWidth;
    // 视频高度
    private int mVideoHeight;
    // 显示宽度
    private int mDisplayWidth;
    // 显示高度
    private int mDisplayHeight;
    // 缩放方式
    private ScaleType mScaleType = ScaleType.CENTER_CROP;

    private EglCore mEglCore;
    // 录制视频用的EGLSurface
    private WindowSurface mRecordWindowSurface;
    // 录制的Filter
    private GLDisplayFilter mRecordFilter;

    // 复用器管理器
    private MediaMuxerWrapper mMuxerManager;

    // 录制文件路径
    private String mRecorderOutputPath = null;

    // 是否允许录音
    private boolean isEnableAudioRecording = true;

    // 是否处于录制状态
    private boolean isRecording = false;

    // MediaCodec 初始化和释放所需要的时间总和
    // 根据试验的结果，大部分手机在初始化和释放阶段的时间总和都要300ms ~ 800ms左右
    // 第一次初始化普遍时间比较长，新出的红米5(2G内存)在第一次初始化1624ms，后面则是600ms左右
    private long mProcessTime = 0;

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
        long time = System.currentTimeMillis();
        mVideoWidth = width;
        mVideoHeight = height;
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
        mProcessTime += (System.currentTimeMillis() - time);
    }

    /**
     * 设置渲染Texture的宽高
     * @param width
     * @param height
     */
    public void setTextureSize(int width, int height) {
        mTextureWidth = width;
        mTextureHeight = height;
    }

    /**
     * 设置预览大小
     * @param width
     * @param height
     */
    public void setDisplaySize(int width, int height) {
        mDisplayWidth = width;
        mDisplayHeight = height;
    }

    /**
     * 调整视口大小
     */
    private void updateViewport() {
        float[] mvpMatrix = GlUtil.IDENTITY_MATRIX;
        if (mVideoWidth == 0 || mVideoHeight == 0) {
            mVideoWidth = mTextureWidth;
            mVideoHeight = mTextureHeight;
        }
        final double scale_x = mDisplayWidth / mVideoWidth;
        final double scale_y = mDisplayHeight / mVideoHeight;
        final double scale = (mScaleType == ScaleType.CENTER_CROP)
                ? Math.max(scale_x,  scale_y) : Math.min(scale_x, scale_y);
        final double width = scale * mVideoWidth;
        final double height = scale * mVideoHeight;
        Matrix.scaleM(mvpMatrix, 0, (float)(width / mDisplayWidth),
                (float)(height / mDisplayHeight), 1.0f);
        if (mRecordFilter != null) {
            mRecordFilter.setMVPMatrix(mvpMatrix);
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
        initRecordingFilter();
        updateViewport();
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
     * @param currentTexture 当前Texture
     * @param timeStamp 时间戳
     */
    public void drawRecorderFrame(int currentTexture, long timeStamp) {
        if (mRecordWindowSurface != null) {
            mRecordWindowSurface.makeCurrent();
            drawRecordingFrame(currentTexture);
            mRecordWindowSurface.setPresentationTime(timeStamp);
            mRecordWindowSurface.swapBuffers();
        }
    }

    /**
     * 停止录制
     */
    public synchronized void stopRecording() {
        long time = System.currentTimeMillis();
        isRecording = false;
        if (mMuxerManager != null) {
            mMuxerManager.stopRecording();
            mMuxerManager = null;
        }
        if (mRecordWindowSurface != null) {
            mRecordWindowSurface.release();
            mRecordWindowSurface = null;
        }
        // 释放资源
        releaseRecordingFilter();

        if (VERBOSE) {
            mProcessTime += (System.currentTimeMillis() - time);
            Log.d(TAG, "sum of init and release time: " + mProcessTime + "ms");
            mProcessTime = 0;
        }
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
     * 初始化录制的Filter
     * TODO 录制视频大小跟渲染大小、显示大小拆分成不同的大小
     */
    private void initRecordingFilter() {
        if (mRecordFilter == null) {
            mRecordFilter = new GLDisplayFilter();
        }
        mRecordFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
        mRecordFilter.onDisplayChanged(mVideoWidth, mVideoHeight);
    }

    /**
     * 渲染录制的帧
     */
    public void drawRecordingFrame(int textureId) {
        if (mRecordFilter != null) {
            GLES30.glViewport(0, 0, mVideoWidth, mVideoHeight);
            mRecordFilter.drawFrame(textureId);
        }
    }

    /**
     * 释放录制的Filter资源
     */
    public void releaseRecordingFilter() {
        if (mRecordFilter != null) {
            mRecordFilter.release();
            mRecordFilter = null;
        }
    }

    /**
     * 销毁资源
     */
    public void release() {
        // 停止录制
        stopRecording();
        if (mEglCore != null) {
            mEglCore.release();
            mEglCore = null;
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
