package com.cgfay.camera.engine.recorder;

import android.content.Context;
import android.opengl.EGLContext;
import android.opengl.GLES30;
import android.text.TextUtils;
import android.util.Log;

import com.cgfay.filter.gles.EglCore;
import com.cgfay.filter.gles.WindowSurface;
import com.cgfay.filter.glfilter.base.GLImageFilter;
import com.cgfay.filter.glfilter.utils.OpenGLUtils;
import com.cgfay.filter.glfilter.utils.TextureRotationUtils;
import com.cgfay.filter.multimedia.MediaAudioEncoder;
import com.cgfay.filter.multimedia.MediaEncoder;
import com.cgfay.filter.multimedia.MediaMuxerWrapper;
import com.cgfay.filter.multimedia.MediaVideoEncoder;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;

/**
 * 硬编码录制器
 * Created by cain.huang on 2017/12/7.
 */

public final class HardcodeEncoder {

    private static final String TAG = "HardcodeEncoder";
    private static final boolean VERBOSE = false;
    // 输出路径
    private String mOutputPath;
    // 输入纹理大小
    private int mTextureWidth, mTextureHeight;
    // 录制视频大小
    private int mVideoWidth, mVideoHeight;

    private EglCore mEglCore;
    // 录制视频用的EGLSurface
    private WindowSurface mRecordWindowSurface;
    // 录制的Filter
    private GLImageFilter mRecordFilter;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;
    // 复用器管理器
    private MediaMuxerWrapper mMuxerManager;

    // 是否允许录音
    private boolean enableAudio = true;

    // 是否处于录制状态
    private boolean isRecording = false;

    // MediaCodec 初始化和释放所需要的时间总和
    // 根据试验的结果，大部分手机在初始化和释放阶段的时间总和都要300ms ~ 800ms左右
    // 第一次初始化普遍时间比较长，新出的红米5(2G内存)在第一次初始化1624ms，后面则是600ms左右
    private long mProcessTime = 0;

    private static class HardcodeEncoderHolder {
        public static HardcodeEncoder instance = new HardcodeEncoder();
    }

    private HardcodeEncoder() {

    }

    public static HardcodeEncoder getInstance() {
        return HardcodeEncoderHolder.instance;
    }

    /**
     * destroy recorder
     */
    public void destroyRecorder() {
        release();
    }

    /**
     * init recorder, cost about 200ms, which is so long to run in render thread.
     * @param width
     * @param height
     * @param listener
     */
    public void initRecorder(int width, int height, MediaEncoder.MediaEncoderListener listener) {
        if (VERBOSE) {
            Log.d(TAG, "init recorder");
        }
        mVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
        long time = System.currentTimeMillis();
        mVideoWidth = width;
        mVideoHeight = height;

        String filePath = mOutputPath;

        // 如果路径为空，则生成默认的路径
        if (TextUtils.isEmpty(filePath)) {
            throw new IllegalArgumentException("filePath Must no be empty");
        }
        File file = new File(filePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            mMuxerManager = new MediaMuxerWrapper(file.getAbsolutePath());
            new MediaVideoEncoder(mMuxerManager, listener, mVideoWidth, mVideoHeight);
            if (enableAudio) {
                new MediaAudioEncoder(mMuxerManager, listener);
            }

            mMuxerManager.prepare();
        } catch (IOException e) {
            Log.e(TAG, "initRecorder:", e);
        }
        mProcessTime += (System.currentTimeMillis() - time);
    }

    /**
     * set texture size int GPU
     * @param width
     * @param height
     */
    public void setTextureSize(int width, int height) {
        mTextureWidth = width;
        mTextureHeight = height;
    }

    /**
     * start record
     * @param sharedContext
     */
    public void startRecord(final Context context, final EGLContext sharedContext) {
        if (VERBOSE) {
            Log.d(TAG, " start record");
        }
        if (mMuxerManager.getVideoEncoder() == null) {
            return;
        }
        // release old record surface
        if (mRecordWindowSurface != null) {
            mRecordWindowSurface.releaseEglSurface();
        }
        if (mEglCore != null) {
            mEglCore.release();
            mEglCore = null;
        }
        // recreate new record surface
        mEglCore = new EglCore(sharedContext, EglCore.FLAG_RECORDABLE);
        if (mRecordWindowSurface != null) {
            mRecordWindowSurface.recreate(mEglCore);
        } else {
            mRecordWindowSurface = new WindowSurface(mEglCore,
                    ((MediaVideoEncoder) mMuxerManager.getVideoEncoder()).getInputSurface(),
                    true);
        }
        mRecordWindowSurface.makeCurrent();
        initRecordingFilter(context);
        if (mMuxerManager != null) {
            mMuxerManager.startRecording();
        }
        isRecording = true;
    }


    /**
     * 帧可用
     */
    public void frameAvailable() {
        if (mMuxerManager != null && mMuxerManager.getVideoEncoder() != null && isRecording) {
            mMuxerManager.getVideoEncoder().frameAvailableSoon();
        }
    }

    /**
     * 发送渲染指令
     * @param texture 当前Texture
     * @param timeStamp 时间戳
     */
    public void drawRecorderFrame(int texture, long timeStamp) {
        if (VERBOSE) {
            Log.d(TAG, "draw recording frame");
        }
        if (mRecordWindowSurface != null) {
            mRecordWindowSurface.makeCurrent();
            drawRecordingFrame(texture);
            mRecordWindowSurface.setPresentationTime(timeStamp);
            mRecordWindowSurface.swapBuffers();
        }
    }

    /**
     * stop recorder
     */
    public void stopRecord() {
        if (VERBOSE) {
            Log.d(TAG, "stop recording");
        }
        long time = System.currentTimeMillis();
        isRecording = false;
        if (mMuxerManager != null) {
            mMuxerManager.stopRecording();
            mMuxerManager = null;
        }
        releaseRecordingFilter();
        if (mRecordWindowSurface != null) {
            mRecordWindowSurface.release();
            mRecordWindowSurface = null;
        }
        if (VERBOSE) {
            mProcessTime += (System.currentTimeMillis() - time);
            Log.d(TAG, "sum of init and release time: " + mProcessTime + "ms");
            mProcessTime = 0;
        }
    }

    /**
     * init record filter
     */
    private void initRecordingFilter(Context context) {
        if (mRecordFilter == null) {
            mRecordFilter = new GLImageFilter(context);
        }
        mRecordFilter.onInputSizeChanged(mTextureWidth, mTextureHeight);
        mRecordFilter.onDisplaySizeChanged(mVideoWidth, mVideoHeight);
    }

    /**
     * draw on frame to record window surface
     */
    private void drawRecordingFrame(int textureId) {
        if (mRecordFilter != null) {
            GLES30.glViewport(0, 0, mVideoWidth, mVideoHeight);
            mRecordFilter.drawFrame(textureId, mVertexBuffer, mTextureBuffer);
        }
    }

    /**
     * release record filter
     */
    private void releaseRecordingFilter() {
        if (mRecordFilter != null) {
            mRecordFilter.release();
            mRecordFilter = null;
        }
    }

    /**
     * release all
     */
    public void release() {
        // 停止录制
        stopRecord();
        if (mEglCore != null) {
            mEglCore.release();
            mEglCore = null;
        }
        if (mRecordWindowSurface != null) {
            mRecordWindowSurface.release();
            mRecordWindowSurface = null;
        }
        if (mVertexBuffer != null) {
            mVertexBuffer.clear();
            mVertexBuffer = null;
        }
        if (mTextureBuffer != null) {
            mTextureBuffer.clear();
            mTextureBuffer = null;
        }
    }

    /**
     * enable to record audio
     * @param enable
     */
    public HardcodeEncoder enableAudioRecord(boolean enable) {
        if (VERBOSE) {
            Log.d(TAG, "enable audio recording ? " + enable);
        }
        enableAudio = enable;
        return this;
    }

    /**
     * 设置输出路径
     * @param path
     */
    public HardcodeEncoder setOutputPath(String path) {
        mOutputPath = path;
        return this;
    }

    /**
     * 获取输出路径
     * @return
     */
    public String getOutputPath() {
        return mOutputPath;
    }
}
