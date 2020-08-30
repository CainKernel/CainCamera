package com.cgfay.cavfoundation.capture;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 基于Java层的MediaMuxer实现的封装器
 */
public class CAVCaptureMuxer {

    private static final String TAG = "CAVCaptureMuxer";
    private static final boolean VERBOSE = true;

    // 视频编码器
    private CAVCaptureEncoder mVideoEncoder;
    // 音频编码器
    private CAVCaptureEncoder mAudioEncoder;
    // 媒体封装器
    private MediaMuxer mMediaMuxer;
    // 输出文件路径
    private String mOutputPath;

    // 编码器数量
    private int mEncoderCount;
    // 开始的编码器数量
    private int mStartedCount;
    // 封装器是否已经开始
    private boolean mStarted;

    public CAVCaptureMuxer() {
        mOutputPath = null;
        mEncoderCount = 0;
        mStartedCount = 0;
        mStarted = false;
    }

    /**
     * 设置输出路径
     *
     * @param path
     */
    public void setOutputPath(@NonNull String path) {
        mOutputPath = path;
    }



    /**
     * 准备的封装器
     *
     * @throws IllegalArgumentException
     */
    public void prepare() throws IOException {
        if (TextUtils.isEmpty(mOutputPath)) {
            throw new IOException("Failed to prepare before set output path!");
        }
        if (mMediaMuxer == null) {
            mMediaMuxer = new MediaMuxer(mOutputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        }
        if (mAudioEncoder != null) {
            mAudioEncoder.prepare();
        }
        if (mVideoEncoder != null) {
            mVideoEncoder.prepare();
        }
    }

    /**
     * 开始录制
     */
    public void startRecording() {
        if (mAudioEncoder != null) {
            mAudioEncoder.startRecording();
        }
        if (mVideoEncoder != null) {
            mVideoEncoder.startRecording();
        }
    }

    /**
     * 停止录制
     */
    public void stopRecording() {
        if (mAudioEncoder != null) {
            mAudioEncoder.stopRecording();
        }
        mAudioEncoder = null;
        if (mVideoEncoder != null) {
            mVideoEncoder.stopRecording();
        }
        mVideoEncoder = null;
    }

    /**
     * 是否已经开始
     */
    public synchronized boolean isStarted() {
        return mStarted;
    }

    /**
     * 添加编码器对象
     *
     * @param encoder
     */
    void addCaptureEncoder(@NonNull CAVCaptureEncoder encoder) {
        if (encoder instanceof CAVCaptureAudioEncoder) {
            if (mAudioEncoder != null) {
                throw new IllegalArgumentException("Audio encoder already added.");
            }
            mAudioEncoder = encoder;
        } else if (encoder instanceof CAVCaptureVideoEncoder) {
            if (mVideoEncoder != null) {
                throw new IllegalArgumentException("Video encoder already added.");
            }
            mVideoEncoder = encoder;
        } else {
            throw new IllegalArgumentException("unsupported encoder");
        }
        mEncoderCount = (mVideoEncoder != null ? 1 : 0) + (mAudioEncoder != null ? 1 : 0);
    }

    /**
     * 开始封装
     */
    synchronized boolean start() {
        if (mMediaMuxer == null) {
            return false;
        }
        mStartedCount++;
        if ((mEncoderCount > 0) && (mStartedCount == mEncoderCount)) {
            mMediaMuxer.start();
            mStarted = true;
            notifyAll();
            if (VERBOSE) {
                Log.d(TAG, "MediaMuxer is started");
            }
        }
        return mStarted;
    }

    /**
     * 停止封装
     */
    synchronized void stop() {
        if (mMediaMuxer == null) {
            return;
        }
        if (VERBOSE) {
            Log.d(TAG, "stop: mStartedCount = " + mStartedCount);
        }
        mStartedCount--;
        if ((mEncoderCount > 0) && (mStartedCount <= 0)) {
            mMediaMuxer.stop();
            mMediaMuxer.release();
            mMediaMuxer = null;
            mStarted = false;
        }
    }

    /**
     * 添加轨道
     *
     * @param format
     * @return 轨道索引
     */
    public int addTrack( @NonNull MediaFormat format) {
        if (mStarted) {
            throw new IllegalStateException("MediaMuxer already started");
        }
        if (mMediaMuxer != null) {
            int trackIndex = mMediaMuxer.addTrack(format);
            if (VERBOSE) {
                Log.d(TAG, "addTrack: trackNum = " + mEncoderCount
                        + ", trackIndex = " + trackIndex + "format: " + format);
            }
            return trackIndex;
        }
        return -1;
    }

    /**
     * 将编码后的数据写入封装器
     *
     * @param trackIndex
     * @param encodeBuffer
     * @param bufferInfo
     */
    synchronized void writeSampleData(int trackIndex, @NonNull ByteBuffer encodeBuffer,
                                      @NonNull MediaCodec.BufferInfo bufferInfo) {
        if (mStartedCount > 0) {
            if (mMediaMuxer != null) {
                mMediaMuxer.writeSampleData(trackIndex, encodeBuffer, bufferInfo);
            }
        }
    }

}
