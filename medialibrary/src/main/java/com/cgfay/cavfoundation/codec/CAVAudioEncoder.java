package com.cgfay.cavfoundation.codec;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;

/**
 * 音频编码器
 */
public class CAVAudioEncoder {

    private static final String TAG = "CAVAudioEncoder";

    private static final String MIME_TYPE = "audio/mp4a-latm";

    private static final int BUFFER_SIZE = 8192;

    private static final int TIME_OUT = -1;

    private int mBitrate;
    private int mSampleRate;
    private int mChannelCount;

    // 音频硬编码器
    private MediaFormat mMediaFormat;
    private MediaCodec mMediaCodec;
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();

    private int mTotalBytesRead;
    private long mPresentationTimeUs;   // 编码的时长

    private int mBufferSize = BUFFER_SIZE;
    private String mMimeType = MIME_TYPE;

    private boolean mInited;

    public CAVAudioEncoder() {
        mBitrate = 128000;
        mSampleRate = 44100;
        mChannelCount = 2;
        mTotalBytesRead = 0;
        mPresentationTimeUs = 0;
        mInited = false;
    }

    /**
     * 设置编码参数
     * @param bitrate
     * @param sampleRate
     * @param channelCount
     */
    public void setEncodeOptions(int bitrate, int sampleRate, int channelCount) {
        mBitrate = bitrate;
        mSampleRate = sampleRate;
        mChannelCount = channelCount;
    }

    /**
     * 设置缓冲区大小
     * @param size
     */
    public void setBufferSize(int size) {
        mBufferSize = size;
    }

    /**
     * 设置mime类型
     * @param mimeType 媒体类型
     */
    public void setMimeType(@NonNull String mimeType) {
        mMimeType = mimeType;
    }

    /**
     * 准备编码器
     * @throws Exception 异常
     */
    public void prepareHardwareEncoder() throws Exception {
        // 编码格式
        mMediaFormat = MediaFormat.createAudioFormat(mMimeType, mSampleRate, mChannelCount);
        mMediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, mBitrate);
        mMediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, mBufferSize);

        // 创建编码器
        mMediaCodec = MediaCodec.createEncoderByType(mMimeType);
        mMediaCodec.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec.start();
    }

    /**
     * 停止编码器
     */
    public void stopEncoder() {
        if (mMediaCodec != null && mInited) {
            mMediaCodec.stop();
        }
        mInited = false;
    }

    /**
     * 释放编码器
     */
    public void release() {
        if (mInited) {
            stopEncoder();
        }
        if (mMediaCodec != null) {
            mMediaCodec.release();
            mMediaCodec = null;
        }
        mInited = false;
    }

    /**
     * 将音频PCM数据送去编码
     * @param pcmData               PCM数据
     * @param len                   PCM长度
     * @param presentationTimeUs    编码音频PCM的pts
     * @return                      处理结果
     */
    public boolean sendFrame(byte[] pcmData, int len, long presentationTimeUs) {
        if (mMediaCodec != null && mInited) {
            int inputBufferIndex = mMediaCodec.dequeueInputBuffer(TIME_OUT);
            if (inputBufferIndex >= 0) {
                ByteBuffer buffer = null;
                while (buffer == null) {
                    buffer = mMediaCodec.getInputBuffer(inputBufferIndex);
                }
                buffer.clear();
                if (len <= 0) {
                    mMediaCodec.queueInputBuffer(inputBufferIndex, 0, 0,
                            presentationTimeUs, 0);
                } else {
                    buffer.put(pcmData, 0, len);
                    mMediaCodec.queueInputBuffer(inputBufferIndex, 0, len,
                            presentationTimeUs, 0);
                }
                Log.d(TAG, "sendFrame: presentationTimeUs: " + presentationTimeUs);
                return true;
            }
        }
        return false;
    }


    /**
     * 获取编码后的AAC数据
     */
    public boolean receivePacket() {
        int outputIndex = 0;
        while (outputIndex != MediaCodec.INFO_TRY_AGAIN_LATER) {
            outputIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 0);
            if (outputIndex >= 0) {
                ByteBuffer buffer = null;
                while (buffer == null) {
                    buffer = mMediaCodec.getOutputBuffer(outputIndex);
                }
                buffer.position(mBufferInfo.offset);
                buffer.limit(mBufferInfo.offset + mBufferInfo.size);
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    mMediaCodec.releaseOutputBuffer(outputIndex, false);
                } else {
                    // TODO 提取编码后的数据

                }
            } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                Log.d(TAG, "receivePacket: MediaCodec.INFO_OUTPUT_FORMAT_CHANGED");
            }
        }
        return false;
    }
}
