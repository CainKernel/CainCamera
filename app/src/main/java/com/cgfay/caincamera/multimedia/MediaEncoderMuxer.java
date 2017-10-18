package com.cgfay.caincamera.multimedia;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * 音视频混合器
 * Created by cain on 2017/10/14.
 */

public class MediaEncoderMuxer {

    private static final String TAG = "MediaEncoderMuxer";
    private static final boolean VERBOSE = true;

    private static final SimpleDateFormat  mDateTimeFormat
            = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);

    private String mOutputPath;

    private final MediaMuxer mMediaMuxer;

    private int mEncoderCount, mStartCount;
    private boolean mIsStarted;
    private MediaEncoder mVideoEncoder, mAudioEncoder;

    public MediaEncoderMuxer(String outputPath) throws IOException {
        mOutputPath = outputPath;
        mMediaMuxer = new MediaMuxer(mOutputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        mEncoderCount = 0;
        mStartCount = 0;
        mIsStarted = false;
    }

    /**
     * 准备
     * @throws IOException
     */
    public void prepare() throws IOException {
        if (mVideoEncoder != null) {
            mVideoEncoder.prepare();
        }
    }

    /**
     * 开始录制
     */
    public void startRecording() {
        if (mVideoEncoder != null) {
            mVideoEncoder.startRecording();
        }
        if (mAudioEncoder != null) {
            mAudioEncoder.startRecording();
        }
    }


    /**
     * 停止录制
     */
    public void stopRecording() {
        if (mVideoEncoder != null) {
            mVideoEncoder.stopRecording();
        }
        if (mAudioEncoder != null) {
            mAudioEncoder.stopRecording();
        }
    }

    /**
     * 销毁释放资源
     */
    public void release() {}


    /**
     * 添加录制编码器
     * @param encoder
     */
    void addEncoder(final MediaEncoder encoder) {
        if (encoder instanceof VideoMediaEncoder) {
            if (mVideoEncoder != null) {
                throw new IllegalArgumentException("Video encoder is already added!");
            }
            mVideoEncoder = encoder;
        } else if (encoder instanceof AudioMediaEncoder) {
            if (mAudioEncoder != null) {
                throw new IllegalArgumentException("Audio encoder is already added!");
            }
            mAudioEncoder = encoder;
        } else {
            throw new IllegalArgumentException("Don't enable this encoder!");
        }

        mEncoderCount = (mVideoEncoder != null ? 1 : 0) + (mAudioEncoder != null ? 1 : 0);
    }

    /**
     * 开始录制
     * @return
     */
    synchronized boolean start() {
        if (VERBOSE) {
            Log.d(TAG, "start:");
        }
        mStartCount++;
        if ((mEncoderCount > 0) && (mStartCount == mEncoderCount)) {
            mMediaMuxer.start();
            mIsStarted = true;
            notifyAll();
            if (VERBOSE) {
                Log.d(TAG, "MediaMuxer has been started: ");
            }
        }
        return mIsStarted;
    }

    /**
     * 停止录制
     */
    synchronized void stop() {
        if (VERBOSE) {
            Log.d(TAG, "stop:");
        }
        mStartCount--;
        if ((mEncoderCount  > 0) && (mStartCount <= 0)) {
            mMediaMuxer.stop();
            mMediaMuxer.release();
            mIsStarted = false;
            if (VERBOSE) {
                Log.d(TAG, "MediaMuxer has been stopped!");
            }
        }
    }

    /**
     * 音轨
     * @param format
     * @return
     */
    synchronized int addTrack(final MediaFormat format) {
        if (mIsStarted) {
            throw new IllegalArgumentException("Muxer has been already started!");
        }
        int track = mMediaMuxer.addTrack(format);
        if (VERBOSE) {
            Log.d(TAG, "trackNum = " + mEncoderCount + ", trackIx = "
                    + track + ", format = " + format);
        }
        return track;
    }

    /**
     * 写入编码后的数据
     * @param trackIndex
     * @param buffer
     * @param bufferInfo
     */
    synchronized void writeSampleData(int trackIndex, ByteBuffer buffer,
                                      MediaCodec.BufferInfo bufferInfo) {
        if (mStartCount >0) {
            mMediaMuxer.writeSampleData(trackIndex, buffer, bufferInfo);
        }
    }

    /**
     * 获取输出路径
     * @return
     */
    public String getOutputPath() {
        return mOutputPath;
    }

    /**
     * 判断是否开始
     * @return
     */
    public synchronized boolean isStarted() {
        return mIsStarted;
    }
}
