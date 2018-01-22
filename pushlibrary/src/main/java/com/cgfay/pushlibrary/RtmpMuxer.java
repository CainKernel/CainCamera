package com.cgfay.pushlibrary;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Rtmp复用器
 * Created by cain on 2018/1/22.
 */

public final class RtmpMuxer {

    static {
        System.loadLibrary("hw_pusher");
    }

    /**
     * 定义输出格式
     */
    public static final class OutputFormat {
        private OutputFormat() {}
        /** RTMP media file format*/
        public static final int MUXER_OUTPUT_RTMP = 0;
    }

    // native方法

    /**
     * 初始化
     * @param format
     * @return
     */
    private static native long nativeSetup(int format);

    /**
     * 释放资源
     * @param nativeObject
     */
    private static native void nativeRelease(long nativeObject);

    /**
     * 开始推流
     * @param nativeObject
     */
    private static native void nativeStart(long nativeObject);

    /**
     * 停止推流
     * @param nativeObject
     */
    private static native void nativeStop(long nativeObject);

    /**
     * 添加轨道
     * @param nativeObject
     * @param keys
     * @param values
     * @return
     */
    private static native int nativeAddTrack(
            long nativeObject, @NonNull String[] keys, @NonNull Object[] values);

    /**
     * 设置旋转角度
     * @param nativeObject
     * @param degrees
     */
    private static native void nativeSetOrientationHint(long nativeObject, int degrees);

    /**
     * 写入数据
     * @param nativeObject
     * @param trackIndex
     * @param byteBuf
     * @param offset
     * @param size
     * @param presentationTimeUs
     * @param flags
     */
    private static native void nativeWriteSampleData(
            long nativeObject, int trackIndex, @NonNull ByteBuffer byteBuf,
            int offset, int size, long presentationTimeUs, int flags);

    // 复用器状态
    private static final int MUXER_STATE_UNINITIALIZED  = -1;
    private static final int MUXER_STATE_INITIALIZED    = 0;
    private static final int MUXER_STATE_STARTED        = 1;
    private static final int MUXER_STATE_STOPPED        = 2;

    private int mState = MUXER_STATE_UNINITIALIZED;

    // 上一个Track轨道索引
    private int mLastTrackIndex = -1;

    // native 对象句柄
    private long mNativeObject;

    public RtmpMuxer(String url, int format) throws IOException {
        if (url == null) {
            throw new IllegalArgumentException("path must not be null");
        }
        if (format != RtmpMuxer.OutputFormat.MUXER_OUTPUT_RTMP) {
            throw new IllegalArgumentException("format is invalid");
        }
        mNativeObject = nativeSetup(format);
        mState = MUXER_STATE_INITIALIZED;
    }

    /**
     * 设置旋转角度
     * @param degrees
     */
    public void setOrientationHint(int degrees) {
        if (degrees != 0 && degrees != 90  && degrees != 180 && degrees != 270) {
            throw new IllegalArgumentException("Unsupported angle: " + degrees);
        }
        if (mState == MUXER_STATE_INITIALIZED) {
            nativeSetOrientationHint(mNativeObject, degrees);
        } else {
            throw new IllegalStateException("Can't set rotation degrees due" +
                    " to wrong state.");
        }
    }

    /**
     * 开始推流
     */
    public void start() {
        if (mNativeObject == 0) {
            throw new IllegalStateException("Muxer has been released!");
        }
        if (mState == MUXER_STATE_INITIALIZED) {
            nativeStart(mNativeObject);
            mState = MUXER_STATE_STARTED;
        } else {
            throw new IllegalStateException("Can't start due to wrong state.");
        }
    }

    /**
     * 停止推流
     */
    public void stop() {
        if (mState == MUXER_STATE_STARTED) {
            nativeStop(mNativeObject);
            mState = MUXER_STATE_STOPPED;
        } else {
            throw new IllegalStateException("Can't stop due to wrong state.");
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        if (mState == MUXER_STATE_STARTED) {
            stop();
        }
        if (mNativeObject != 0) {
            nativeRelease(mNativeObject);
            mNativeObject = 0;
        }
        mState = MUXER_STATE_UNINITIALIZED;
    }

    /**
     * 重载finalize 用于销毁native层的对象
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            if (mNativeObject != 0) {
                nativeRelease(mNativeObject);
                mNativeObject = 0;
            }
        } finally {
            super.finalize();
        }
    }

    /**
     * Track类型
     * @param format
     * @return
     */
    public int addTrack(MediaFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("format must not be null.");
        }
        if (mState != MUXER_STATE_INITIALIZED) {
            throw new IllegalStateException("Muxer is not initialized.");
        }
        if (mNativeObject == 0) {
            throw new IllegalStateException("Muxer has been released!");
        }
        int trackIndex = -1;
        // TODO 反射MediaFormat的kay-value
//        // Convert the MediaFormat into key-value pairs and send to the native.
//        Map<String, Object> formatMap = format.getMap();
//
//        String[] keys = null;
//        Object[] values = null;
//        int mapSize = formatMap.size();
//        if (mapSize > 0) {
//            keys = new String[mapSize];
//            values = new Object[mapSize];
//            int i = 0;
//            for (Map.Entry<String, Object> entry : formatMap.entrySet()) {
//                keys[i] = entry.getKey();
//                values[i] = entry.getValue();
//                ++i;
//            }
//            trackIndex = nativeAddTrack(mNativeObject, keys, values);
//        } else {
//            throw new IllegalArgumentException("format must not be empty.");
//        }

        // Track index number is expected to incremented as addTrack succeed.
        // However, if format is invalid, it will get a negative trackIndex.
        if (mLastTrackIndex >= trackIndex) {
            throw new IllegalArgumentException("Invalid format.");
        }
        mLastTrackIndex = trackIndex;
        return trackIndex;
    }

    /**
     * 写入采样数据(已经过硬编码后的数据)
     * @param trackIndex
     * @param byteBuf
     * @param bufferInfo
     */
    synchronized void writeSampleData(final int trackIndex, final ByteBuffer byteBuf,
                                      final MediaCodec.BufferInfo bufferInfo) {
        if (trackIndex < 0 || trackIndex > mLastTrackIndex) {
            throw new IllegalArgumentException("trackIndex is invalid");
        }

        if (byteBuf == null) {
            throw new IllegalArgumentException("byteBuffer must not be null");
        }

        if (bufferInfo == null) {
            throw new IllegalArgumentException("bufferInfo must not be null");
        }
        if (bufferInfo.size < 0 || bufferInfo.offset < 0
                || (bufferInfo.offset + bufferInfo.size) > byteBuf.capacity()
                || bufferInfo.presentationTimeUs < 0) {
            throw new IllegalArgumentException("bufferInfo must specify a" +
                    " valid buffer offset, size and presentation time");
        }

        if (mNativeObject == 0) {
            throw new IllegalStateException("Muxer has been released!");
        }

        if (mState != MUXER_STATE_STARTED) {
            throw new IllegalStateException("Can't write, muxer is not started");
        }

        nativeWriteSampleData(mNativeObject, trackIndex, byteBuf,
                bufferInfo.offset, bufferInfo.size,
                bufferInfo.presentationTimeUs, bufferInfo.flags);
    }
}
