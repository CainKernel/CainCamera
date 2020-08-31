package com.cgfay.cavfoundation.capture;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import androidx.annotation.NonNull;


import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

/**
 * 录制编码器
 */
abstract class CAVCaptureEncoder implements Runnable {

    private final String TAG = getClass().getSimpleName();
    protected static final boolean VERBOSE = true;

    protected static final int TIMEOUT_USEC = 10000;

    public interface OnCaptureEncoderListener {
        // 准备完成回调
        void onPrepared(@NonNull CAVCaptureEncoder encoder);
        // 编码回调
        void onEncoding(@NonNull CAVCaptureEncoder encoder, long presentationUs);
        // 暂停回调
        void onStopped(@NonNull CAVCaptureEncoder encoder);
    }

    protected final Object mSync = new Object();

    /**
     * Flag that indicate this encoder is capturing now.
     */
    protected volatile boolean mIsCapturing;

    /**
     * Flag that indicate the frame data will be available soon.
     */
    private int mRequestDrain;

    /**
     * Flag to request stop capturing
     */
    protected volatile boolean mRequestStop;

    /**
     * Flag that indicate encoder received EOS(End Of Stream)
     */
    protected boolean mIsEOS;

    /**
     * Flag the indicate the muxer is running
     */
    protected boolean mMuxerStarted;

    /**
     * Track Number
     */
    protected int mTrackIndex;

    /**
     * MediaEncoder instance for encoding
     */
    protected MediaCodec mMediaCodec;

    /**
     * Weak refarence of CAVCaptureMuxer instance
     */
    protected final WeakReference<CAVCaptureMuxer> mWeakMuxer;

    /**
     * BufferInfo instance for dequeuing
     */
    private MediaCodec.BufferInfo mBufferInfo;

    /**
     * listener
     */
    protected OnCaptureEncoderListener mListener;

    /**
     * encode start time
     */
    protected long mStartTimeUs;

    public CAVCaptureEncoder(@NonNull CAVCaptureMuxer muxer, @NonNull OnCaptureEncoderListener listener) {
        mWeakMuxer = new WeakReference<>(muxer);
        mListener = listener;
        muxer.addCaptureEncoder(this);
        mStartTimeUs = 0;
        synchronized (mSync) {
            mBufferInfo = new MediaCodec.BufferInfo();
            new Thread(this, getClass().getSimpleName()).start();
            try {
                mSync.wait();
            } catch (InterruptedException ex) {

            }
        }
    }

    /**
     * notify frame data is available
     * @return return true if encoder is ready to encode
     */
    public boolean frameAvailable() {
        if (VERBOSE) {
            Log.d(TAG, "frameAvailable: ");
        }
        synchronized (mSync) {
            if (!mIsCapturing || mRequestStop) {
                return false;
            }
            mRequestDrain++;
            mSync.notifyAll();
        }
        return true;
    }

    @Override
    public void run() {
        synchronized (mSync) {
            mRequestStop = false;
            mRequestDrain = 0;
            mSync.notify();
        }

        boolean localRequestStop;
        boolean localRequestDrain;
        while (true) {
            synchronized (mSync) {
                localRequestStop = mRequestStop;
                localRequestDrain = (mRequestDrain > 0);
                if (localRequestDrain) {
                    mRequestDrain--;
                }
            }

            if (localRequestStop) {
                signalEndOfInputStream();
                drainEncoder();
                release();
                break;
            }
            if (localRequestDrain) {
                drainEncoder();
            } else {
                synchronized (mSync) {
                    try {
                        mSync.wait();
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }

        if (VERBOSE) {
            Log.d(TAG, "Encoder thread exiting...");
        }
        synchronized (mSync) {
            mRequestStop = true;
            mIsCapturing = false;
        }
    }

    /**
     * 准备编码器
     * @throws IOException
     */
    abstract void prepare() throws IOException;

    /**
     * 开始录制
     */
    void startRecording() {
        synchronized (mSync) {
            mIsCapturing = true;
            mRequestStop = false;
            mSync.notifyAll();
        }
    }

    /**
     * 停止录制
     */
    void stopRecording() {
        if (VERBOSE) {
            Log.d(TAG, "stopRecording: ");
        }
        synchronized (mSync) {
            if (!mIsCapturing || mRequestStop) {
                return;
            }
            mRequestStop = true;
            mSync.notifyAll();
        }
    }

    /**
     * 释放资源
     */
    protected void release() {
        if (VERBOSE) {
            Log.d(TAG, "release: ");
        }
        mIsCapturing = false;
        if (mMediaCodec != null) {
            try {
                mMediaCodec.stop();
                mMediaCodec.release();
                mMediaCodec = null;
            } catch (final Exception e) {
                Log.e(TAG, "failed releasing MediaCodec", e);
            }
        }

        // 释放完成回调
        if (mListener != null) {
            mListener.onStopped(this);
        }

        // 停止封装器(停止会比较耗时，放在回调之后做处理)
        if (mMuxerStarted) {
            final CAVCaptureMuxer muxer = mWeakMuxer != null ? mWeakMuxer.get() : null;
            if (muxer != null) {
                try {
                    muxer.stop();
                } catch (final Exception e) {
                    Log.e(TAG, "failed stopping muxer", e);
                }
            }
        }
    }

    protected abstract void signalEndOfInputStream();

    /**
     * drain encoded data and write to muxer
     */
    protected void drainEncoder() {
        if (mMediaCodec == null) {
            return;
        }
        int encoderStatus;
        final CAVCaptureMuxer muxer = mWeakMuxer.get();
        if (muxer == null) {
            Log.w(TAG, "muxer is unexpectedly null");
            return;
        }

LOOP:   while (mIsCapturing) {
            encoderStatus = mMediaCodec.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                break;
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                if (VERBOSE) {
                    Log.d(TAG, "drainEncoder: INFO_OUTPUT_BUFFERS_CHANGED");
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (VERBOSE) {
                    Log.d(TAG, "drainEncoder: INFO_OUTPUT_FORMAT_CHANGED");
                }
                if (mMuxerStarted) {
                    throw new RuntimeException("format changed twice");
                }
                final MediaFormat format = mMediaCodec.getOutputFormat();
                mTrackIndex = muxer.addTrack(format);
                mMuxerStarted = true;
                // 打开Muxer
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
                    Log.d(TAG, "drainEncoder: unexpected result from encoder#dequeueOutputBuffer： " + encoderStatus);
                }
            } else {
                final ByteBuffer encodedData = mMediaCodec.getOutputBuffer(encoderStatus);
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus + " was null");
                }

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    if (VERBOSE) {
                        Log.d(TAG, "drainEncoder: BUFFER_FLAG_CODEC_CONFIG");
                    }
                    mBufferInfo.size = 0;
                }

                if (mBufferInfo.size != 0) {
                    if (!mMuxerStarted) {
                        throw new RuntimeException("drainEncoder: muxer hasn't started");
                    }
                    mBufferInfo.presentationTimeUs = getPresentationTimeUs();
                    muxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo);
                    if (VERBOSE) {
                        Log.d(TAG, "drainEncoder: write sample time: " + mBufferInfo.presentationTimeUs);
                    }
                    // 回调
                    if (mListener != null) {
                        mListener.onEncoding(this, mBufferInfo.presentationTimeUs);
                    }
                }
                mMediaCodec.releaseOutputBuffer(encoderStatus, false);
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    mIsCapturing = false;
                    break;
                }
            }
        }

    }

    /**
     * 获取pts
     */
    protected long getPresentationTimeUs() {
        if (mStartTimeUs <= 0) {
            mStartTimeUs = mBufferInfo.presentationTimeUs;
        }
        long timeUs = mBufferInfo.presentationTimeUs - mStartTimeUs;
        if (timeUs < 0) {
            timeUs = 0;
        }
        return timeUs;
    }
}
