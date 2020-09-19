package com.cgfay.cavfoundation.codec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 媒体编码器
 */
public abstract class CAVMediaEncoder {

    protected final String TAG = getClass().getSimpleName();
    protected static final boolean VERBOSE = true;

    protected static final long TIME_OUT = 1000;

    protected MediaCodec mMediaCodec;
    protected MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();

    protected OnEncodeListener mEncodeListener;

    protected boolean mIsEOS;

    /**
     * 编码监听器
     * @param listener
     */
    public void setOnEncodeListener(@Nullable OnEncodeListener listener) {
        mEncodeListener = listener;
    }

    public abstract void signalEndOfInputStream();

    /**
     * 编码输出
     */
    public void drainEncoder() {
        if (mMediaCodec == null) {
            return;
        }
        if (VERBOSE) {
            Log.d(TAG, "drainEncoder: ");
        }
        while (true) {
            int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, TIME_OUT);
            if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                break;
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                if (VERBOSE) Log.d(TAG, "drainEncoder: INFO_OUTPUT_BUFFERS_CHANGED");
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat newFormat = mMediaCodec.getOutputFormat();
                if (VERBOSE) {
                    Log.d(TAG, "drainEncoder: encoder output format changed: "
                            + newFormat.getString(MediaFormat.KEY_MIME));
                }
                if (mEncodeListener != null) {
                    mEncodeListener.onAddTrack(this, newFormat);
                }
            } else if (outputBufferIndex < 0) {
                Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                        outputBufferIndex);
            } else {
                ByteBuffer encodedData = mMediaCodec.getOutputBuffer(outputBufferIndex);
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + outputBufferIndex +
                            " was null");
                }

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    if (VERBOSE) {
                        Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                    }
                    // 写入额外参数
                    if (mEncodeListener != null) {
                        mEncodeListener.onWriteExtraData(getTrackIndex(), encodedData, mBufferInfo);
                    }
                    mBufferInfo.size = 0;
                }

                if (mBufferInfo.size != 0) {
                    calculateTimeUs(mBufferInfo);
                    if (mEncodeListener != null) {
                        mEncodeListener.onWriteFrame(getTrackIndex(), encodedData, mBufferInfo);
                    }
                    if (VERBOSE) {
                        Log.d(TAG, "sent " + mBufferInfo.size + " bytes to muxer, ts=" +
                                mBufferInfo.presentationTimeUs);
                    }
                }
                mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    mIsEOS = true;
                    break;      // out of while
                }
            }
        }
    }

    /**
     * 准备编码器
     * @throws Exception
     */
    public abstract void prepare() throws IOException;

    /**
     * 启动编码
     */
    public void start() {
        if (mMediaCodec != null) {
            mMediaCodec.start();
        }
    }

    /**
     * 停止编码
     */
    public void stop() {
        signalEndOfInputStream();
        drainEncoder();
        if (mMediaCodec != null) {
            mMediaCodec.stop();
        }
    }

    /**
     * 释放编码器
     */
    public void release() {
        if (mMediaCodec != null) {
            mMediaCodec.release();
            mMediaCodec = null;
        }
    }

    /**
     * 获取轨道索引
     */
    protected abstract int getTrackIndex();

    /**
     * 计算pts
     */
    protected abstract void calculateTimeUs(MediaCodec.BufferInfo info);

}
