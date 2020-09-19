package com.cgfay.cavfoundation.codec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 音频编码器
 */
public class CAVAudioEncoder extends CAVMediaEncoder {

    private int mMaxBufferSize = 8192;
    private int mTotalBytesRead;
    private long mPresentationTimeUs;   // 编码的时长

    // 音频参数
    private CAVAudioInfo mAudioInfo;

    public CAVAudioEncoder(@NonNull CAVAudioInfo info) {
        mAudioInfo = info;
    }

    /**
     * 准备编码器
     *
     * @throws Exception
     */
    @Override
    public void prepare() throws IOException {
        MediaFormat format = MediaFormat.createAudioFormat(mAudioInfo.getMimeType(),
                mAudioInfo.getSampleRate(), mAudioInfo.getChannelCount());
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, mAudioInfo.getProfile());
        format.setInteger(MediaFormat.KEY_BIT_RATE, mAudioInfo.getBitRate());
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, mMaxBufferSize);

        mMediaCodec = MediaCodec.createEncoderByType(mAudioInfo.getMimeType());
        mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mTotalBytesRead = 0;
        mPresentationTimeUs = 0;
        mIsEOS = false;
    }

    /**
     * PCM编码
     * @param data  pcm数据
     * @param size  大小
     */
    public void encodePCMData(byte[] data, int size) {
        if (VERBOSE) {
            Log.d(TAG, "encodePCMData: " + size);
        }
        if (mMediaCodec == null) {
            return;
        }
        // 将pcm数据送去编码
        while (true) {
            int inputBufferIndex = mMediaCodec.dequeueInputBuffer(TIME_OUT);
            if (inputBufferIndex >= 0) {
                ByteBuffer buffer = mMediaCodec.getInputBuffer(inputBufferIndex);
                if (buffer == null) {
                    continue;
                }
                buffer.clear();
                if (size <= 0) {
                    mIsEOS = true;
                    mMediaCodec.queueInputBuffer(inputBufferIndex, 0, 0,
                            mPresentationTimeUs, 0);
                } else {
                    mTotalBytesRead += size;
                    buffer.put(data, 0, size);
                    mMediaCodec.queueInputBuffer(inputBufferIndex, 0, size,
                            mPresentationTimeUs, 0);
                    mPresentationTimeUs = 1000000L * (mTotalBytesRead / mAudioInfo.getChannelCount() / 2)
                            / mAudioInfo.getSampleRate();
                }
                break;
            }
        }
        // 编码处理
        drainEncoder();
    }

    @Override
    public void signalEndOfInputStream() {
        encodePCMData(null, 0);
    }

    @Override
    protected int getTrackIndex() {
        return mAudioInfo.getTrack();
    }

    @Override
    protected void calculateTimeUs(MediaCodec.BufferInfo info) {
        info.presentationTimeUs = mPresentationTimeUs;
    }
}
