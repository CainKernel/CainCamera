package com.cgfay.cavfoundation.capture;

import android.opengl.EGLContext;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cgfay.cavfoundation.codec.AudioInfo;
import com.cgfay.cavfoundation.codec.VideoInfo;

import java.io.IOException;


/**
 * 视频录制器
 */
public class CAVCaptureRecorder implements CAVCaptureEncoder.OnCaptureEncoderListener,
        CAVCaptureAudioProcessor.OnAudioProcessorListener {

    private static final String TAG = "CAVMediaRecorder";

    // 音频读取器
    private CAVCaptureAudioInput mAudioReader;
    // 音频处理器
    private CAVCaptureAudioProcessor mAudioProcessor;
    // 音频编码器
    private CAVCaptureAudioEncoder mAudioEncoder;

    // 视频处理器
    private CAVCaptureVideoPreviewLayer mVideoProcessor;
    // 视频编码器
    private CAVCaptureVideoEncoder mVideoEncoder;
    // 封装器
    private CAVCaptureMuxer mMediaMuxer;

    // 音频参数
    private AudioInfo mAudioInfo;
    // 视频参数
    private VideoInfo mVideoInfo;
    // 设置输出路径
    private String mOutputPath;
    // 录制速度
    private float mSpeed;

    // 编码器数量
    private int mEncoderCount;
    // 准备的数量
    private int mPreparedCount;

    // 录制时长
    private long mDuration;

    // 录制监听器
    private OnCaptureRecordListener mCaptureListener;

    public CAVCaptureRecorder() {
        mAudioEncoder = null;
        mVideoEncoder = null;
        mMediaMuxer = null;
        mAudioReader = null;
        mAudioProcessor = null;
        mAudioInfo = null;
        mVideoInfo = null;
        mOutputPath = null;
        mSpeed = 1.0f;
        mEncoderCount = 0;
        mPreparedCount = 0;
        mDuration = 0;
        mCaptureListener = null;
    }

    /**
     * 准备编码器
     */
    public void prepare() throws IOException {
        if (TextUtils.isEmpty(mOutputPath)) {
            throw new IOException("Failed to prepare before set output path!");
        }

        // 创建封装器
        mMediaMuxer = new CAVCaptureMuxer();
        mMediaMuxer.setOutputPath(mOutputPath);

        // 创建音频编码器
        if (mAudioInfo != null) {
            mAudioEncoder = new CAVCaptureAudioEncoder(mMediaMuxer, this);
            mAudioEncoder.setAudioInfo(mAudioInfo);
            mAudioEncoder.prepare();
            if (mAudioReader == null) {
                mAudioReader = new CAVCaptureAudioMuteInput(mAudioInfo);
            }
            mAudioReader.prepare();
            mAudioProcessor = new CAVCaptureAudioProcessor(this);
            mAudioProcessor.setOnAudioProcessorListener(this);
            mEncoderCount++;
        }

        // 创建视频编码器
        if (mVideoInfo != null) {
            mVideoEncoder = new CAVCaptureVideoEncoder(mMediaMuxer, this);
            mVideoEncoder.setVideoInfo(mVideoInfo);
            mVideoEncoder.prepare();
            mEncoderCount++;
            mVideoProcessor = new CAVCaptureVideoPreviewLayer(this);
            mVideoProcessor.setVideoInfo(mVideoInfo);
        }

        // 准备封装器
        mMediaMuxer.prepare();
    }

    /**
     * 开始录制
     */
    public void startRecord() {
        Log.d(TAG, "startRecord: ");
        if (mAudioProcessor != null) {
            mAudioProcessor.start();
        }
        if (mMediaMuxer != null) {
            mMediaMuxer.startRecording();
        }
    }

    /**
     * 停止录制
     */
    public void stopRecord() {
        Log.d(TAG, "stopRecord: ");
        if (mMediaMuxer != null) {
            mMediaMuxer.stopRecording();
        }
        if (mAudioProcessor != null) {
            mAudioProcessor.stop();
        }
        if (mVideoProcessor != null) {
            mVideoProcessor.stop();
            mVideoProcessor = null;
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        Log.d(TAG, "release: ");
        if (mAudioProcessor != null) {
            mAudioProcessor.stop();
            mAudioProcessor = null;
        }
    }

    /**
     * 更新录制一帧视频
     * @param timeUs 时钟，用于音频处理同步
     */
    void updateRecordFrame(long timeUs) {
        if (mVideoEncoder != null) {
            mVideoEncoder.frameAvailable();
        }
        if (mAudioProcessor != null) {
            mAudioProcessor.updateVideoRecordTimeUs((long)(timeUs * getSpeed()));
        }
    }

    @Override
    public void onPrepared(@NonNull CAVCaptureEncoder encoder) {
        if (encoder instanceof CAVCaptureAudioEncoder) {
            Log.d(TAG, "CAVCaptureAudioEncoder onPrepared: ");
            mPreparedCount++;
        } else if (encoder instanceof CAVCaptureVideoEncoder) {
            Log.d(TAG, "CAVCaptureVideoEncoder onPrepared: ");
            mPreparedCount++;
        }

        // 准备完成回调，开始录制
        if (mEncoderCount > 0 && mPreparedCount >= mEncoderCount) {
            if (mCaptureListener != null) {
                mCaptureListener.onCaptureStart();
            }
        }
    }

    @Override
    public void onEncoding(@NonNull CAVCaptureEncoder encoder, long presentationUs) {
        if (encoder instanceof CAVCaptureAudioEncoder) {
            Log.d(TAG, "CAVCaptureAudioEncoder onEncoding: " + presentationUs);
        } else if (encoder instanceof CAVCaptureVideoEncoder) {
            Log.d(TAG, "CAVCaptureVideoEncoder onEncoding: " + presentationUs);
        }
        if (mDuration < presentationUs) {
            mDuration = presentationUs;
        }
        // 录制时长回调
        if (mCaptureListener != null) {
            mCaptureListener.onCapturing(mDuration);
        }
    }

    @Override
    public void onStopped(@NonNull CAVCaptureEncoder encoder) {
        if (encoder instanceof CAVCaptureAudioEncoder) {
            Log.d(TAG, "CAVCaptureAudioEncoder onStopped: ");
            mAudioEncoder = null;
            mEncoderCount--;
        } else if (encoder instanceof CAVCaptureVideoEncoder) {
            Log.d(TAG, "CAVCaptureVideoEncoder onStopped: ");
            mVideoEncoder = null;
            mEncoderCount--;
        }

        // 录制完成回调
        if (mEncoderCount <= 0) {
            release();
            if (mCaptureListener != null) {
                mCaptureListener.onCaptureFinish(mOutputPath, mDuration);
            }
        }
    }

    /**
     * 音频数据回调
     * @param data
     * @param length
     */
    @Override
    public void onAudioDataProvide(byte[] data, int length) {
        Log.d(TAG, "onAudioDataProvide: " + length);
        if (mAudioEncoder != null) {
            mAudioEncoder.encodeData(data, length);
        }
    }

    /**
     * 音频处理完成回调
     */
    @Override
    public void onAudioProcessFinish() {
        Log.d(TAG, "onAudioProcessFinish: ");
        if (mAudioEncoder != null) {
            mAudioEncoder.stopRecording();
        }
    }

    /**
     * 初始化录制渲染器
     * @param eglContext
     */
    public void initVideoRenderer(@NonNull EGLContext eglContext) {
        Log.d(TAG, "initVideoRenderer: ");
        if (mVideoProcessor != null) {
            mVideoProcessor.start(eglContext);
        }
    }

    /**
     * 录制渲染一个视频帧
     * @param texture
     * @param timesamp
     */
    public void renderFrame(int texture, long timesamp) {
        if (mVideoProcessor != null) {
            mVideoProcessor.renderFrame(texture, timesamp);
        }
        if (mAudioProcessor != null) {
            mAudioProcessor.updateVideoRecordTimeUs(timesamp);
        }
    }

    /**
     * 设置录制监听器
     * @param listener 监听器
     */
    public void setOnCaptureRecordListener(@Nullable OnCaptureRecordListener listener) {
        mCaptureListener = listener;
    }

    /**
     * 设置音频读取器
     */
    public void setAudioReader(@Nullable CAVCaptureAudioInput audioReader) {
        mAudioReader = audioReader;
    }

    /**
     * 设置速度
     * @param speed
     */
    public void setSpeed(float speed) {
        mSpeed = speed;
    }

    /**
     * 设置输出路径
     * @param path
     */
    public void setOutputPath(@NonNull String path) {
        mOutputPath = path;
    }

    /**
     * 设置音频参数
     * @param info
     */
    public void setAudioInfo(@Nullable AudioInfo info) {
        mAudioInfo = info;
    }

    /**
     * 设置视频参数
     * @param info
     */
    public void setVideoInfo(@Nullable VideoInfo info) {
        mVideoInfo = info;
    }

    /**
     * 获取音频参数
     */
    public AudioInfo getAudioInfo() {
        return mAudioInfo;
    }

    /**
     * 获取视频参数
     */
    public VideoInfo getVideoInfo() {
        return mVideoInfo;
    }

    /**
     * 获取音频读取器
     */
    public CAVCaptureAudioInput getAudioReader() {
        return mAudioReader;
    }

    /**
     * 获取速度
     */
    public float getSpeed() {
        return mSpeed;
    }

    /**
     * 正在录制阶段
     */
    public boolean isRecording() {
        return mVideoEncoder != null && mVideoEncoder.mIsCapturing;
    }

    /**
     * 获取编码输入Surface
     */
    Surface getInputSurface() {
        if (mVideoEncoder != null) {
            return mVideoEncoder.getInputSurface();
        }
        return null;
    }
}
