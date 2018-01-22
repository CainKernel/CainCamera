package com.cgfay.pushlibrary;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

/**
 * 推流器基类
 * Created by cain on 2018/1/22.
 */

public abstract class MediaPusher implements Runnable {
    private static final String TAG = "MediaPusher";
    private static final boolean VERBOSE = false;

    protected static final int TIMEOUT_USEC = 10000;

    protected final Object mSync = new Object();

    protected final Object mOperation = new Object();

    protected volatile boolean mIsPushing;

    private int mRequestDrain;

    protected volatile boolean mRequestStop;

    protected boolean mIsEOS;

    protected boolean mMuxerStarted;

    protected int mTrackIndex;

    protected MediaCodec mMediaCodec;

    protected final WeakReference<MediaRtmpMuxer> mWeakMuxer;

    private MediaCodec.BufferInfo mBufferInfo;

    protected final MediaPusherListener mListener;
    private long totalNans = 0;

    private boolean mIsVideo;

    public MediaPusher(final MediaRtmpMuxer muxer,
                       final MediaPusherListener listener, boolean isVideo) {
        mWeakMuxer = new WeakReference<MediaRtmpMuxer>(muxer);
        muxer.addPusher(this);
        mListener = listener;
        mIsVideo = isVideo;

        synchronized (mSync) {
            mBufferInfo = new MediaCodec.BufferInfo();
            new Thread(this, getClass().getSimpleName()).start();
            try {
                mSync.wait();
            } catch (final InterruptedException e) {

            }
        }
    }

    /**
     * 帧可用
     * @return
     */
    public boolean frameAvailable() {
        synchronized (mSync) {
            if (!mIsPushing || mRequestStop) {
                return false;
            }
            mRequestDrain++;
            mSync.notifyAll();
        }
        return true;
    }

    @Override
    public void run() {
        // 初始化标志
        synchronized (mSync) {
            mRequestStop = false;
            mRequestDrain = 0;
            mSync.notify();
        }

        final boolean isRunning = true;
        boolean localRequestStop;
        boolean localRequestDrain;
        while (isRunning) {
            localRequestStop = mRequestStop;
            localRequestDrain = (mRequestDrain > 0);
            if (localRequestDrain) {
                mRequestDrain--;
            }
            // 停止推流
            if (localRequestStop) {
                synchronized (mOperation) {
                    // 推流
                    drain();
                    // 发送停止信号
                    sendEndOfPushing();
                    // 推流停止信号
                    drain();
                    // 通知最后一帧推流完成，进入释放阶段
                    mOperation.notifyAll();
                    release();
                }
                break;
            }

            // 推流
            if (localRequestDrain) {
                drain();
            } else {
                synchronized (mSync) {
                    try {
                        mSync.wait();
                    } catch (final InterruptedException e) {
                        break;
                    }
                }
            }
        }

        // 推流线程退出
        if (VERBOSE) {
            Log.d(TAG, "pushing thread exiting!");
        }

        synchronized (mSync) {
            mRequestStop = true;
            mIsPushing = false;
        }
    }

    /**
     * 准备方法，用于准备推流器
     * @throws IOException
     */
    abstract void prepare() throws IOException;

    /**
     * 开始推流
     */
    void startPushing() {
        if (VERBOSE) {
            Log.d(TAG, "startPushing");
        }
        synchronized (mSync) {
            mIsPushing = true;
            mRequestStop = false;
            mSync.notifyAll();
        }
    }

    /**
     * 停止推流
     */
    void stopPusing() {
        if (VERBOSE) {
            Log.d(TAG, "stopPusing");
        }
        synchronized (mSync) {
            if (!mIsPushing || mRequestStop) {
                return;
            }
            mRequestStop = true;
            mSync.notifyAll();
        }

        synchronized (mOperation) {
            try {
                mOperation.wait();
            } catch (InterruptedException e) {

            }
        }
    }

    /**
     * 释放资源
     */
    protected void release() {
        if (VERBOSE) {
            Log.d(TAG, "release:");
        }
        if (mListener != null) {
            mListener.onStopped(this);
        }
        mIsPushing = false;
        if (mMediaCodec != null) {
            try {
                mMediaCodec.stop();
                mMediaCodec.release();
                mMediaCodec = null;
            } catch (final Exception e) {
                Log.e(TAG, "failed release MediaCodec", e);
            }
        }
        if (mMuxerStarted) {
            final MediaRtmpMuxer muxer = mWeakMuxer != null ? mWeakMuxer.get() : null;
            if (muxer != null) {
                try {
                    muxer.stop();
                } catch (final Exception e) {
                    Log.e(TAG, "failed stopping muxer", e);
                }
            }
        }
        mBufferInfo = null;
        if (mListener != null) {
            mListener.onReleased(this);
        }
    }

    /**
     * 停止推流信号
     */
    protected void sendEndOfPushing() {
        if (VERBOSE) {
            Log.d(TAG, "send EOS to pusher");
        }
        encode(null, 0, getPTSUs());
    }

    /**
     * 便阿妈
     * @param buffer
     * @param length
     * @param presentationTimeus
     */
    protected void encode(final ByteBuffer buffer, final int length, final long presentationTimeus) {
        if (!mIsPushing) {
            return;
        }
        final ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
        while (mIsPushing) {
            final int inputBufferIndex = mMediaCodec.dequeueInputBuffer(TIMEOUT_USEC);
            if (inputBufferIndex >= 0) {
                final ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                if (buffer != null) {
                    inputBuffer.put(buffer);
                }
                if (length <= 0) {
                    mIsEOS = true;
                    if (VERBOSE) {
                        Log.i(TAG, "send BUFFER_FLAG_END_OF_STREAM");
                    }
                    // 写入终止信号
                    mMediaCodec.queueInputBuffer(inputBufferIndex, 0, 0,
                            presentationTimeus, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    break;
                } else {
                    mMediaCodec.queueInputBuffer(inputBufferIndex, 0, length,
                            presentationTimeus, 0);
                }
                break;
            }
        }
    }

    /**
     * 编码后推流
     */
    protected void drain() {
        if (mMediaCodec == null) {
            return;
        }
        ByteBuffer[] encoderOutputBuffers = mMediaCodec.getOutputBuffers();
        int encoderStatus, count = 0;
        final MediaRtmpMuxer muxer = mWeakMuxer.get();
        if (muxer == null) {
            Log.w(TAG, "muxer is unexpectedly null");
            return;
        }
        LOOP:
        while (mIsPushing) {
            encoderStatus = mMediaCodec.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);

            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!mIsEOS) {
                    if (++count > 5) { // 超时
                        break LOOP;
                    }
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                if (VERBOSE) {
                    Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                }
                encoderOutputBuffers = mMediaCodec.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (VERBOSE) {
                    Log.d(TAG, "INFO_OUTPUT_FORMAT_CHANGED");
                }
                if (mMuxerStarted) {
                    throw new RuntimeException("format changed twice");
                }
                final MediaFormat format = mMediaCodec.getOutputFormat();
                if (mIsVideo) {
                    if (VERBOSE) {
                        Log.d(TAG, "MediaCodec format: " + format.toString());
                    }
                }

                mTrackIndex = muxer.addTrack(format);
                mMuxerStarted = true;
                if (!muxer.start()) {
                    synchronized (muxer) {
                        while (!muxer.isStarted()) {
                            try {
                                muxer.wait(100);
                            } catch (final InterruptedException e) {
                                break LOOP;
                            }
                        }
                    }
                }
            } else if (encoderStatus < 0) {
                if (VERBOSE) {
                    Log.w(TAG, "drain:unexpected result from encoder#dequeueOutputBuffer: "
                            + encoderStatus);
                }
            } else {
                final ByteBuffer encoderData = encoderOutputBuffers[encoderStatus];
                if (encoderData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus + " was null");
                }
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    if (VERBOSE) {
                        Log.d(TAG, "drain: BUFFER_FLAG_CODEC_CONFIG");
                    }
                    mBufferInfo.size = 0;
                }

                if (mBufferInfo.size != 0) {
                    count = 0;
                    if (!mMuxerStarted) {
                        throw new RuntimeException("drain:muxer hasn't started");
                    }
                    mBufferInfo.presentationTimeUs = getPTSUs();
                    muxer.writeSampleData(mTrackIndex, encoderData, mBufferInfo);
                }

                mMediaCodec.releaseOutputBuffer(encoderStatus, false);

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    mIsPushing = false;
                    break;
                }
            }
        }
    }

    /**
     * 获取下一个编码的显示时间，需要减去暂停的时间
     * @return
     */
    protected long getPTSUs() {
        long result = System.nanoTime();
        return (result - totalNans) / 1000L;
    }
}
