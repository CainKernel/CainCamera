package com.cgfay.cavfoundation.codec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 音频解码器
 */
public class CAVAudioDecoder {

    private static final String TAG = "CAVAudioDecoder";

    private static final int DECODE_DELAY = 10;

    /**
     * 采样率
     */
    private int mSampleRate;

    /**
     * 声道数
     */
    private int mChannels;

    /**
     * 硬解码格式
     */
    private MediaFormat mMediaFormat;

    /**
     * 音频解码器
     */
    private MediaCodec mMediaCodec;

    /**
     * 视频解码器info
     */
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();

    /**
     * 初始化
     */
    private boolean mInited = false;

    /**
     * 结尾
     */
    private boolean mEndOfStream = false;

    public CAVAudioDecoder(int sampleRate, int channels) {
        mSampleRate = sampleRate;
        mChannels = channels;
        mInited = false;
    }

    /**
     * 初始化音频解码器
     * @param mimeType      媒体mime类型
     */
    public boolean initDecoder(@NonNull String mimeType, int sampleRate, int channelCount, byte[] csd0) {

        // 格式化
        mMediaFormat = MediaFormat.createAudioFormat(mimeType, sampleRate, channelCount);
        mMediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(csd0));
        try {
            mMediaCodec = MediaCodec.createDecoderByType(mimeType);
            mMediaCodec.configure(mMediaFormat, null, null, 0);
            mMediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        mInited = true;
        return true;
    }

    /**
     * 将音频裸数据送去解码
     * @param decodeData
     * @param size
     * @param pts
     */
    public void sendPacket(byte[] decodeData, int size, long pts) {
        if (decodeData != null && mMediaCodec != null && mInited) {
            try {
                int inputBufferIndex = mMediaCodec.dequeueInputBuffer(DECODE_DELAY);
                if (inputBufferIndex >= 0) {
                    // 获取可用的缓冲区
                    ByteBuffer byteBuffer;
                    do {
                        byteBuffer = mMediaCodec.getInputBuffer(inputBufferIndex);
                    } while (byteBuffer == null);
                    byteBuffer.clear();
                    byteBuffer.put(decodeData);

                    // 送去解码
                    mMediaCodec.queueInputBuffer(inputBufferIndex, 0, size, pts, 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 接收解码后的音频数据
     */
    public boolean receiveFrame() {
        if (mMediaCodec != null && mInited) {
            int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, DECODE_DELAY);
            if (outputBufferIndex >= 0) {
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                    return false;
                }
                // 输出缓冲区
                if (mBufferInfo.size != 0) {

                }
                mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                // 结尾标志
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    mEndOfStream = true;
                }
                return true;
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                Log.d(TAG, "receiveFrame: INFO_OUTPUT_BUFFERS_CHANGED");
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                Log.d(TAG, "receiveFrame: INFO_OUTPUT_FORMAT_CHANGED");
            }
        }
        return false;
    }

    /**
     * 刷新解码器
     */
    public void flushDecoder() {
        if (mMediaCodec != null) {
            mMediaCodec.flush();
        }
    }

    /**
     * 停止解码器
     */
    public void stopDecoder() {
        if (mMediaCodec != null) {
            mMediaCodec.flush();
            mMediaCodec.stop();
        }
        mInited = false;
    }

    /**
     * 释放解码器
     */
    public void releaseDecoder() {
        if (mInited) {
            stopDecoder();
        }
        if (mMediaCodec != null) {
            mMediaCodec.release();
            mMediaCodec = null;
        }
    }
}
