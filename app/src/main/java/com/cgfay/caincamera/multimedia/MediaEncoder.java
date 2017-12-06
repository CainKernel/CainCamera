package com.cgfay.caincamera.multimedia;
/*
 * AudioVideoRecordingSample
 * Sample project to cature audio and video from internal mic/camera and save as MPEG4 file.
 *
 * Copyright (c) 2014-2015 saki t_saki@serenegiant.com
 *
 * File name: MediaEncoder.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * All files in the folder are under this Apache License, Version 2.0.
*/

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

public abstract class MediaEncoder implements Runnable {

    private static final boolean DEBUG = false;    // TODO set false on release
    private static final String TAG = "MediaEncoder";

    private static final int MSG_FRAME_AVAILABLE = 0;
    private static final int MSG_START_RECORDING = 1;
    private static final int MSG_STOP_RECORDING = 2;
    private static final int MSG_QUIT = 3;

    protected static final int TIMEOUT_USEC = 10000;    // 10[msec]

    public interface MediaEncoderListener {

        // 准备好了
        public void onPrepared(MediaEncoder encoder);

        // 开始了
        public void onStarted(MediaEncoder encoder);

        // 停止状态
        public void onStopped(MediaEncoder encoder);

        // 完全释放
        void onReleased(MediaEncoder encoder);
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
     * MediaCodec instance for encoding
     */
    protected MediaCodec mMediaCodec;                // API >= 16(Android4.1.2)
    /**
     * Weak refarence of MediaMuxerWarapper instance
     */
    protected final WeakReference<MediaMuxerWrapper> mWeakMuxer;
    /**
     * BufferInfo instance for dequeuing
     */
    private MediaCodec.BufferInfo mBufferInfo;        // API >= 16(Android4.1.2)

    protected final MediaEncoderListener mListener;
    private long pauseBeginNans = 0;
    private long totalNans = 0;
    public boolean isPause;

    private boolean mbIsVideo;

    // 编码线程Handler回调
    private EncoderHandler mHandler;

    public MediaEncoder(final MediaMuxerWrapper muxer, final MediaEncoderListener listener, boolean isVideo) {
        mWeakMuxer = new WeakReference<MediaMuxerWrapper>(muxer);
        muxer.addEncoder(this);
        mListener = listener;
        mbIsVideo = isVideo;

        synchronized (mSync) {
            // create BufferInfo here for effectiveness(to reduce GC)
            mBufferInfo = new MediaCodec.BufferInfo();
            // wait for starting thread
            new Thread(this, getClass().getSimpleName()).start();
            try {
                mSync.wait();
            } catch (final InterruptedException e) {
            }
        }
    }

    /**
     * the method to indicate frame data is soon available or already available
     *
     * @return return true if encoder is ready to encod.
     */
    public boolean frameAvailableSoon() {
        synchronized (mSync) {
            if (!mIsCapturing || mRequestStop) {
                return false;
            }
            mRequestDrain++;
            mSync.notifyAll();
        }
        if (mHandler != null) {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_FRAME_AVAILABLE));
        }
        return true;
    }

    /**
     * encoding loop on private thread
     */
    @Override
    public void run() {
        Looper.prepare();
        synchronized (mSync) {
            mHandler = new EncoderHandler(this);
            mRequestStop = false;
            mRequestDrain = 0;
            mSync.notify();
        }
        Looper.loop();

        if (DEBUG) {
            Log.d(TAG, "Encoder thread exiting");
        }

        synchronized (mSync) {
            mRequestStop = true;
            mIsCapturing = false;
            mHandler = null;
        }
    }

    /*
    * prepareing method for each sub class
    * this method should be implemented in sub class, so set this as abstract method
    * @throws IOException
    */
   /*package*/
    abstract void prepare() throws IOException;

    /**
     * 开始录制
     */
    void startRecording() {
        if (DEBUG) {
            Log.v(TAG, "startRecording");
        }
        synchronized (mSync) {
            mIsCapturing = true;
            mRequestStop = false;
            mSync.notifyAll();
        }
        if (mHandler != null) {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_START_RECORDING));
        }
    }

    /**
     * 是否处于暂停状态
     * @param isPause
     */
    void pauseRecording(boolean isPause) {
        synchronized (mSync) {
            this.isPause = isPause;
            if (isPause) {
                pauseBeginNans = System.nanoTime();
            } else {
                totalNans += System.nanoTime() - pauseBeginNans;
            }
            mSync.notifyAll();
        }
    }

    /**
     * the method to request stop encoding
     * 停止录制
     */
    void stopRecording() {
        if (DEBUG) {
            Log.v(TAG, "stopRecording");
        }
        synchronized (mSync) {
            if (!mIsCapturing || mRequestStop) {
                return;
            }
            mRequestStop = true;
            mSync.notifyAll();
        }
        if (mHandler != null) {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_STOP_RECORDING));
            mHandler.sendMessage(mHandler.obtainMessage(MSG_QUIT));
        }
    }

    /**
     * 释放资源
     */
    protected void release() {
        if (DEBUG) Log.d(TAG, "release:");
        try {
            if(mListener!=null){
                mListener.onStopped(this);
            }
        } catch (final Exception e) {
            Log.e(TAG, "failed onStopped", e);
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
        if (mMuxerStarted) {
            final MediaMuxerWrapper muxer = mWeakMuxer != null ? mWeakMuxer.get() : null;
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
     * 绘制结束帧
     */
    protected void signalEndOfInputStream() {
        if (DEBUG) {
            Log.d(TAG, "sending EOS to encoder");
        }
        encode(null, 0, getPTSUs());
    }

    /**
     * 将byte字节数据输送给MediaCodec编码器
     * @param buffer                byte数组的Buffer缓冲
     * @param length                字节数组长度，0表示结束
     * @param presentationTimeUs    显示的时间
     */
    protected void encode(final ByteBuffer buffer, final int length, final long presentationTimeUs) {
        if (!mIsCapturing) return;
        final ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
        while (mIsCapturing) {
            final int inputBufferIndex = mMediaCodec.dequeueInputBuffer(TIMEOUT_USEC);
            if (inputBufferIndex >= 0) {
                final ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                if (buffer != null) {
                    inputBuffer.put(buffer);
                }
                if (length <= 0) {
                    // send EOS
                    mIsEOS = true;
                    if (DEBUG) {
                        Log.i(TAG, "send BUFFER_FLAG_END_OF_STREAM");
                    }
                    mMediaCodec.queueInputBuffer(inputBufferIndex, 0, 0,
                            presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    break;
                } else {
                    mMediaCodec.queueInputBuffer(inputBufferIndex, 0, length,
                            presentationTimeUs, 0);
                }
                break;
            } else if (inputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // wait for MediaCodec encoder is ready to encode
                // nothing to do here because MediaCodec#dequeueInputBuffer(TIMEOUT_USEC)
                // will wait for maximum TIMEOUT_USEC(10msec) on each call
            }
        }
    }

    /**
     * 消耗编码数据并写入复用器
     */
    protected void drain() {
        if (mMediaCodec == null) return;
        ByteBuffer[] encoderOutputBuffers = mMediaCodec.getOutputBuffers();
        int encoderStatus, count = 0;
        final MediaMuxerWrapper muxer = mWeakMuxer.get();
        if (muxer == null) {
            Log.w(TAG, "muxer is unexpectedly null");
            return;
        }
        LOOP:
        while (mIsCapturing) {
            // get encoded data with maximum timeout duration of TIMEOUT_USEC(=10[msec])
            encoderStatus = mMediaCodec.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);

            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // wait 5 counts(=TIMEOUT_USEC x 5 = 50msec) until data/EOS come
                if (!mIsEOS) {
                    if (++count > 5)
                        break LOOP;        // out of while
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                if (DEBUG) {
                    Log.v(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                }
                // this shoud not come when encoding
                encoderOutputBuffers = mMediaCodec.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (DEBUG) {
                    Log.v(TAG, "INFO_OUTPUT_FORMAT_CHANGED");
                }
                // this status indicate the output format of codec is changed
                // this should come only once before actual encoded data
                // but this status never come on Android4.3 or less
                // and in that case, you should treat when MediaCodec.BUFFER_FLAG_CODEC_CONFIG come.
                if (mMuxerStarted) {    // second time request is error
                    throw new RuntimeException("format changed twice");
                }
                // get output format from codec and pass them to muxer
                // getOutputFormat should be called after INFO_OUTPUT_FORMAT_CHANGED otherwise crash.
                final MediaFormat format = mMediaCodec.getOutputFormat(); // API >= 16

                if (mbIsVideo) {
                    Log.d("www", "MediaCodec format: " + format.toString());
                }

                mTrackIndex = muxer.addTrack(format);
                mMuxerStarted = true;
                if (!muxer.start()) {
                    // we should wait until muxer is ready
                    // 等待复用器准备完成
                    synchronized (muxer) {
                        while (!muxer.isStarted())
                            try {
                                muxer.wait(100);
                            } catch (final InterruptedException e) {
                                break LOOP;
                            }
                    }
                }
            } else if (encoderStatus < 0) {
                // unexpected status
                if (DEBUG) {
                    Log.w(TAG, "drain:unexpected result from encoder#dequeueOutputBuffer: "
                            + encoderStatus);
                }
            } else {
                final ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    // this never should come...may be a MediaCodec internal error
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus + " was null");
                }
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // You shoud set output format to muxer here when you target Android4.3 or less
                    // but MediaCodec#getOutputFormat can not call here(because INFO_OUTPUT_FORMAT_CHANGED don't come yet)
                    // therefor we should expand and prepare output format from buffer data.
                    // This sample is for API>=18(>=Android 4.3), just ignore this flag here
                    if (DEBUG) {
                        Log.d(TAG, "drain:BUFFER_FLAG_CODEC_CONFIG");
                    }
                    mBufferInfo.size = 0;
                }

                if (mBufferInfo.size != 0) {
                    // encoded data is ready, clear waiting counter
                    count = 0;
                    if (!mMuxerStarted) {
                        // muxer is not ready...this will prrograming failure.
                        throw new RuntimeException("drain:muxer hasn't started");
                    }

                    // write encoded data to muxer(need to adjust presentationTimeUs.
                    mBufferInfo.presentationTimeUs = getPTSUs();
                    muxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo);
                }
                // return buffer to encoder
                mMediaCodec.releaseOutputBuffer(encoderStatus, false);
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    // when EOS come.
                    mIsCapturing = false;
                    break;      // out of while
                }
            }
        }
    }

    /**
     * 获取下一个编码的显示时间，这里需要减去暂停的时间
     * @return
     */
    protected long getPTSUs() {
        long result = System.nanoTime();
        return (result - totalNans) / 1000L;
    }


    /**
     * 回调开始录制
     */
    void handleStartRecording() {
        /* do something you want */
    }

    /**
     * 回调停止录制
     */
    void handleStopRecording() {
        drain();
        signalEndOfInputStream();
        drain();
        release();
    }

    /**
     * 回调帧可用
     */
    void handleFrameAvailable() {
        boolean needToDrain = false;
        synchronized (mSync) {
            if (mRequestDrain > 0) {
                mRequestDrain--;
                needToDrain = true;
            }
        }
        // 是否需要将数据传给复用器，暂停状态下不需要传递
        if (needToDrain && isPause) {
            drain();
        }
    }

    /**
     * 线程Handler回调
     */
    private static class EncoderHandler extends Handler {
        private WeakReference<MediaEncoder> mWeakEncoder;

        public EncoderHandler(MediaEncoder encoder) {
            mWeakEncoder = new WeakReference<MediaEncoder>(encoder);
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;

            MediaEncoder encoder = mWeakEncoder.get();
            if (encoder == null) {
                Log.w(TAG, "EncoderHandler.handlerMessage: encoder is null");
                return;
            }

            switch (what) {
                // 帧可用
                case MSG_FRAME_AVAILABLE:
                    encoder.handleFrameAvailable();
                    break;

                // 开始录制
                case MSG_START_RECORDING:
                    encoder.handleStartRecording();
                    break;

                // 停止录制
                case MSG_STOP_RECORDING:
                    encoder.handleStopRecording();
                    break;

                // 退出线程
                case MSG_QUIT:
                    Looper.myLooper().quit();
                    break;

            }
        }
    }
}
