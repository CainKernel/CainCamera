package com.cgfay.cavfoundation.codec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 基于MediaCodec的视频解码器
 */
public class CAVVideoDecoder {

    private static final String TAG = "CAVVideoDecoder";

    private static final int DECODE_DELAY = 10;

    /**
     * 硬解码格式
     */
    private MediaFormat mMediaFormat;
    /**
     * 视频硬解码器
     */
    private MediaCodec mMediaCodec;

    /**
     * 视频解码器info
     */
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();

    /**
     * 渲染输出的Surface
     */
    private Surface mSurface;

    /**
     * 是否已经初始化
     */
    private boolean mInited;

    public CAVVideoDecoder() {
        mSurface = null;
        mInited = false;
    }

    /**
     * 设置输出的Surface，在调用之前
     * @param surface 渲染输出Surface
     */
    public void setSurface(Surface surface) {
        mSurface = surface;
    }

    /**
     * 初始化解码器, FFmpeg调用的话，将AVCodecContext的extradata复制过来
     * @param mimeType  媒体类型
     * @param width     宽度
     * @param height    高度
     * @param csd0      csd0参数
     * @param csd1      csd1参数
     * @return          是否初始化成功
     */
    public boolean initDecoder(String mimeType, int width, int height, byte[] csd0, byte[] csd1) {
        if (mSurface == null) {
            Log.e(TAG, "initDecoder: Failed to init video decoder: surface is null");
            return false;
        }
        // 初始化格式数据
        mMediaFormat = MediaFormat.createVideoFormat(mimeType, width, height);
        mMediaFormat.setInteger(MediaFormat.KEY_WIDTH, width);
        mMediaFormat.setInteger(MediaFormat.KEY_HEIGHT, height);
        mMediaFormat.setLong(MediaFormat.KEY_MAX_INPUT_SIZE, width * height);
        mMediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(csd0));
        mMediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(csd1));

        // 准备视频解码器
        try {
            mMediaCodec = MediaCodec.createDecoderByType(mimeType);
            mMediaCodec.configure(mMediaFormat, mSurface, null, 0);
            mMediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        mInited = true;
        return true;
    }

    /**
     * 将待解码的数据送去解码
     * @param decodeData    待解码的视频帧数据
     * @param size          大小
     * @param pts           时钟
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
     * 提取解码后的视频帧并渲染
     * @param render    是否渲染输出到Surface中
     */
    public void receiveFrame(boolean render) {
        if (mMediaCodec != null && mInited) {
            if (mSurface != null) {
                // 渲染解码后的数据
                int outputIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, DECODE_DELAY);
                while (outputIndex >= 0) {
                    mMediaCodec.releaseOutputBuffer(outputIndex, render);
                    outputIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, DECODE_DELAY);
                }
            }
        }
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
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        mInited = false;
    }

}
