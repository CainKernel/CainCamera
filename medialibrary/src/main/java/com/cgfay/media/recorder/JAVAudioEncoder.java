package com.cgfay.media.recorder;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * 音频编码器
 */
public class JAVAudioEncoder {

    private static final String TAG = "AVAudioEncoder";

    static final int BUFFER_SIZE = 8192;

    private static final String AUDIO_MIME_TYPE = "audio/mp4a-latm";

    private static final int ENCODE_TIMEOUT = -1;

    private final int mBitrate;
    private final int mSampleRate;
    private final int mChannelCount;

    private MediaFormat mMediaFormat;
    private MediaCodec mMediaCodec;
    private MediaMuxer mMediaMuxer;
    private ByteBuffer[] mInputBuffers;
    private ByteBuffer[] mOutputBuffers;
    private MediaCodec.BufferInfo mBufferInfo;

    private String mOutputPath;
    private int mAudioTrackId;
    private int mTotalBytesRead;
    private long mPresentationTimeUs;   // 编码的时长
    private int mBufferSize = BUFFER_SIZE;

    public JAVAudioEncoder(int bitrate, int sampleRate, int channelCount) {
        mBitrate = bitrate;
        mSampleRate = sampleRate;
        mChannelCount = channelCount;
    }

    /**
     * 设置音频输出路径
     * @param path 输出路径
     */
    public void setOutputPath(final String path) {
        mOutputPath = path;
    }

    /**
     * 设置缓冲区大小
     * @param size
     */
    public void setBufferSize(int size) {
        mBufferSize = size;
    }

    /**
     * 准备编码器
     * @throws Exception
     */
    public void prepare() throws Exception {
        if (mOutputPath == null) {
            throw new IllegalStateException("No Output Path found.");
        }
        mMediaFormat = MediaFormat.createAudioFormat(AUDIO_MIME_TYPE, mSampleRate, mChannelCount);
        mMediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, mBitrate);
        mMediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, mBufferSize);

        mMediaCodec = MediaCodec.createEncoderByType(AUDIO_MIME_TYPE);
        mMediaCodec.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec.start();

        mInputBuffers = mMediaCodec.getInputBuffers();
        mOutputBuffers = mMediaCodec.getOutputBuffers();

        mBufferInfo = new MediaCodec.BufferInfo();

        mMediaMuxer = new MediaMuxer(mOutputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        mTotalBytesRead = 0;
        mPresentationTimeUs = 0;
    }

    /**
     * 释放资源
     */
    public void release() {
        try {
            if (mMediaCodec != null) {
                mMediaCodec.stop();
                mMediaCodec.release();
                mMediaCodec = null;
            }
            if (mMediaMuxer != null) {
                mMediaMuxer.stop();
                mMediaMuxer.release();
                mMediaMuxer = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 编码PCM数据
     * @param data
     * @param len
     */
    public void encodePCM(byte[] data, int len) {
        int inputIndex;
        inputIndex = mMediaCodec.dequeueInputBuffer(ENCODE_TIMEOUT);
        if (inputIndex >= 0) {
            ByteBuffer buffer = mInputBuffers[inputIndex];
            buffer.clear();

            if (len < 0) {
                mMediaCodec.queueInputBuffer(inputIndex, 0, 0, (long) mPresentationTimeUs, 0);
            } else {
                mTotalBytesRead += len;
                buffer.put(data, 0, len);
                mMediaCodec.queueInputBuffer(inputIndex, 0, len, (long) mPresentationTimeUs, 0);
                mPresentationTimeUs = 1000000L * (mTotalBytesRead / mChannelCount / 2) / mSampleRate;
                Log.d(TAG, "encodePCM: presentationUs：" + mPresentationTimeUs + ", s: " + (mPresentationTimeUs / 1000000f));
            }
        }

        int outputIndex = 0;
        while (outputIndex != MediaCodec.INFO_TRY_AGAIN_LATER) {
            outputIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 0);
            if (outputIndex >= 0) {
                ByteBuffer encodedData = mOutputBuffers[outputIndex];
                encodedData.position(mBufferInfo.offset);
                encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0 && mBufferInfo.size != 0) {
                    mMediaCodec.releaseOutputBuffer(outputIndex, false);
                } else {
                    mMediaMuxer.writeSampleData(mAudioTrackId, mOutputBuffers[outputIndex], mBufferInfo);
                    mMediaCodec.releaseOutputBuffer(outputIndex, false);
                }
            } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                mMediaFormat = mMediaCodec.getOutputFormat();
                mAudioTrackId = mMediaMuxer.addTrack(mMediaFormat);
                mMediaMuxer.start();
            }
        }
    }

    /**
     * 获取编码时长
     * @return
     */
    public long getDuration() {
        return mPresentationTimeUs;
    }
}
