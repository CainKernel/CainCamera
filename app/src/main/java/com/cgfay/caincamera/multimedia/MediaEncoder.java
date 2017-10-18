package com.cgfay.caincamera.multimedia;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

/**
 * Created by cain on 2017/10/13.
 */

public class MediaEncoder implements Runnable {

    private static final String TAG = "MediaEncoder";
    private static final boolean VERBOSE = true;

    protected static final int TIMEOUT_USEC = 10000;
    protected static final int MSG_FRAME_AVAILABLE = 0x01;
    protected static final int MSG_STOP_RECORDING = 0x02;

    public interface EncoderListener {
        void onPrepared(MediaEncoder encoder);
        void onStopped(MediaEncoder encoder);
    }

    protected final Object mSync = new Object();

    protected volatile boolean mIsCapturing;

    private int mRequestDrain;

    protected volatile boolean mRequestStop;

    protected boolean mIsEOS;

    protected boolean mMuxerStarted;

    protected int mTrackIndex;

    protected MediaCodec mMediaCodec;

    protected final WeakReference<MediaEncoderMuxer> mWeakMuxer;

    private MediaCodec.BufferInfo mBufferInfo;

    protected final EncoderListener mListener;

    public MediaEncoder(MediaEncoderMuxer muxer, EncoderListener listener) {
        if (listener == null) {
            throw new NullPointerException("EncoderListener is null!");
        }
        if (muxer == null) {
            throw new NullPointerException("MediaEncoderMuxer is null!");
        }
        mWeakMuxer = new WeakReference<MediaEncoderMuxer>(muxer);
        muxer.addEncoder(this);
        mListener = listener;
        synchronized (mSync) {
            mBufferInfo = new MediaCodec.BufferInfo();
            new Thread(this, getClass().getSimpleName()).start();
            try {
                mSync.wait();
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void run() {
        synchronized (mSync) {
            mRequestStop = false;
            mRequestDrain = 0;
            mSync.notify();
        }

        final boolean isRunning = true;
        boolean localRequestStop;
        boolean localRequestDrain;
        while (isRunning) {
            synchronized (mSync) {
                localRequestStop = mRequestStop;
                localRequestDrain = (mRequestDrain > 0);
                if (localRequestDrain) {
                    mRequestDrain--;
                }
            }
            if (localRequestStop) {
                drain();
                endOfInputStream();
                drain();
                release();
                break;
            }
            if (localRequestDrain) {
                drain();
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

        synchronized (mSync) {
            mRequestStop = true;
            mIsCapturing = false;
        }
    }

    /**
     * 帧可用
     * @return
     */
    public boolean frameAvailable() {
        synchronized (mSync) {
            if (!mIsCapturing || mRequestStop) {
                return false;
            }
            mRequestDrain++;
            mSync.notifyAll();
        }
        return true;
    }


    protected void prepare() throws IOException {

    }

    /**
     * 开始录制
     */
    void startRecording() {
        if (VERBOSE) {
            Log.d(TAG, "startRecording");
        }
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
            Log.d(TAG, "stopRecording");
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
        try {
            mListener.onStopped(this);
        } catch (Exception e) {
            Log.e(TAG, "failed on stop:", e);
        }
        mIsCapturing = false;
        if (mMediaCodec != null) {
            try {
                mMediaCodec.stop();
                mMediaCodec.release();
                mMediaCodec = null;
            } catch (Exception e) {
                Log.e(TAG, "failed to release MediaCodec:", e);
            }
        }
        if (mMuxerStarted) {
            MediaEncoderMuxer muxer = getMuxer();
            if (muxer != null) {
                try {
                    muxer.stop();
                } catch (final Exception e) {
                    Log.e(TAG, "failed to stop muxer:", e);
                }
            }
        }
        mBufferInfo = null;
    }

    /**
     * 获取输出路径
     * @return
     */
    public String getOutputPath() {
        MediaEncoderMuxer muxer = mWeakMuxer.get();
        return muxer != null ? muxer.getOutputPath() : null;
    }

    private MediaEncoderMuxer getMuxer() {
        return mWeakMuxer != null ? mWeakMuxer.get() : null;
    }

    protected void drain() {
        if (mMediaCodec == null) {
            return;
        }
        ByteBuffer[] outputBuffer = mMediaCodec.getOutputBuffers();
        int encoderStatus = 0;
        int count = 0;
        boolean endOfStream = false;
        MediaEncoderMuxer muxer = getMuxer();
        if (muxer == null) {
            Log.w(TAG, "muxer is null!");
            return;
        }
        while (mIsCapturing) {
            encoderStatus = mMediaCodec.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                if (endOfStream) {
                    break;      // out of while
                } else {
                    count++;
                    if (count > 5) {
                        endOfStream = true;
                    }
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                outputBuffer = mMediaCodec.getOutputBuffers();
                if (VERBOSE) {
                    Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (VERBOSE) {
                    Log.d(TAG, "INFO_OUTPUT_FORMAT_CHANGED");
                }
                if (mMuxerStarted) {
                    throw new RuntimeException("format changed twice!");
                }
                final MediaFormat format = mMediaCodec.getOutputFormat();
                mMuxerStarted = true;
                if (!muxer.start()) {
                    synchronized (muxer) {
                        while (!muxer.isStarted()){
                            try {
                                muxer.wait(100);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                    }
                }
            } else if (encoderStatus < 0) {
                if (VERBOSE) {
                    Log.d(TAG, "drain:unexpected result from encoder#dequeueOutputBuffer: "
                            + encoderStatus);
                }
            } else {
                ByteBuffer encoderData = outputBuffer[encoderStatus];
                if (encoderData == null) {
                    throw new RuntimeException("outputBuffer " + encoderStatus + "is null");
                }
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    mBufferInfo.size = 0;
                }
                if (mBufferInfo.size != 0) {
                    count = 0;
                    if (!mMuxerStarted) {
                        throw new RuntimeException("muxer hasn't started!");
                    }
                    mBufferInfo.presentationTimeUs = getPTSUs();
                    muxer.writeSampleData(mTrackIndex, encoderData, mBufferInfo);
                    mPTSUs = mBufferInfo.presentationTimeUs;
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
     * 终止
     */
    protected void endOfInputStream() {
        encode(null, 0, getPTSUs());
    }

    /**
     * 编码
     * @param buffer
     * @param length
     * @param presentationTimeUs
     */
    protected void encode(ByteBuffer buffer, int length, long presentationTimeUs) {
        if (!mIsCapturing) {
            return;
        }
        ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
        while (mIsCapturing) {
            int bufferIndex = mMediaCodec.dequeueInputBuffer(TIMEOUT_USEC);
            if (bufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[bufferIndex];
                inputBuffer.clear();
                if (buffer != null) {
                    inputBuffer.put(buffer);
                }
                if (length <= 0) {
                    mIsEOS = true;
                    mMediaCodec.queueInputBuffer(bufferIndex, 0, 0,
                            presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    break;
                } else {
                    mMediaCodec.queueInputBuffer(bufferIndex, 0,
                            length, presentationTimeUs, 0);
                }
                break;
            } else if (bufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {

            }
        }
    }

    private long mPTSUs = 0;
    protected long getPTSUs() {
        long result = System.nanoTime() / 1000L;
        if (result < mPTSUs) {
            result = (mPTSUs - result) + result;
        }
        return result;
    }
}
