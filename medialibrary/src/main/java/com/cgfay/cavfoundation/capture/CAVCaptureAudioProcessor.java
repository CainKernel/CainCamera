package com.cgfay.cavfoundation.capture;

import android.util.Log;

import androidx.annotation.NonNull;

import com.cgfay.cavfoundation.codec.CAVAudioInfo;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 音频录制器处理逻辑
 */
class CAVCaptureAudioProcessor implements Runnable {

    private final String TAG = getClass().getSimpleName();

    private static final int MAX_BUFFER_SIZE = 8192;
    private final Object mSync = new Object();

    private int mBufferSize;

    // 音频倍速转码器
    private CAVAudioTranscoder mAudioTranscoder;

    // 录制器
    private final WeakReference<CAVCaptureRecorder> mWeakRecorder;

    // 处理回调监听器
    private OnAudioProcessorListener mListener;

    // 开始标志
    private volatile boolean mStarted;

    private long mVideoStartTimeUs;
    // 视频的时钟pts，处理倍速之前的时钟
    private volatile long mVideoTimeUs;
    // 读取音频的总长度和总时长
    private int mTotalBytesRead;
    private long mPresentationTimeUs;

    public CAVCaptureAudioProcessor(CAVCaptureRecorder recorder) {
        mWeakRecorder = new WeakReference<>(recorder);
        mStarted = false;
        mBufferSize = MAX_BUFFER_SIZE;
        mVideoStartTimeUs = 0;
    }

    /**
     * 开始处理
     */
    public void start() {
        synchronized (mSync) {
            if (mStarted) {
                return;
            }
            new Thread(this, TAG).start();
            try {
                mSync.wait();
            } catch (InterruptedException ex) {
                // ignore
            }
        }
    }

    /**
     * 停止处理
     */
    public void stop() {
        synchronized (mSync) {
            if (!mStarted) {
                return;
            }
            mStarted = false;
            mSync.notifyAll();
        }
    }

    /**
     * 释放资源
     */
    private void release() {
        if (mAudioTranscoder != null) {
            mAudioTranscoder.flush();
            mAudioTranscoder = null;
        }
    }

    /**
     * 更新录制视频的pts
     * @param timeUs 倍速处理前的时钟，用于同步音频解码/静音解码
     */
    public void updateVideoRecordTimeUs(long timeUs) {
        synchronized (mSync) {
            if (mVideoStartTimeUs <= 0) {
                mVideoStartTimeUs = timeUs;
            }
            mVideoTimeUs = (timeUs - mVideoStartTimeUs);
            mSync.notifyAll();
        }
    }

    @Override
    public void run() {
        synchronized (mSync) {
            mStarted = true;
            mSync.notify();
        }
        mTotalBytesRead = 0;
        mPresentationTimeUs = 0;
        CAVCaptureRecorder recorder = mWeakRecorder.get();
        if (recorder == null) {
            return;
        }
        CAVCaptureAudioInput audioReader = recorder.getAudioReader();
        if (audioReader == null) {
            return;
        }
        float speed = recorder.getSpeed();
        CAVAudioInfo audioInfo = recorder.getAudioInfo();
        if (speed != 1.0f && audioInfo != null) {
            // 当速度小于1.0f时，重新计算出缓冲区大小
            if (speed < 1.0f) {
                mBufferSize = MAX_BUFFER_SIZE / 4;
            }
            initAudioTranscoder(audioInfo, speed);
        }

        // 初始化音频读取器
        boolean needToStart = true;
        while (mStarted && needToStart) {
            if (!audioReader.isInitialized()) {
                audioReader.start();
            }
            synchronized (mSync) {
                try {
                    mSync.wait(10);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
            // 是否已经初始化
            if (audioReader.isInitialized()) {
                needToStart = false;
            }
        }

        // 不断读取数据
        int size = 0;
        byte[] pcmData = new byte[mBufferSize];
        while (mStarted) {
            // 判断是否需要同步到视频流时钟
            if (audioReader.isSyncToVideo()) {
                while (mVideoTimeUs < mPresentationTimeUs) {
                    synchronized (mSync) {
                        try {
                            mSync.wait(50);
                        } catch (InterruptedException e) {
                            // ignore
                        }
                    }
                }
            }

            size = audioReader.readAudio(pcmData, mBufferSize);
            calculateAudioReadTimeUs(size, audioReader.getAudioInfo());
            if (size <= 0) {
                synchronized (mSync) {
                    try {
                        mSync.wait(50);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
            } else if (mListener != null) {
                if (speed != 1.0f && mAudioTranscoder != null) {
                    ByteBuffer inBuffer = ByteBuffer.wrap(pcmData,0, size).order(ByteOrder.LITTLE_ENDIAN);
                    mAudioTranscoder.queueInput(inBuffer);
                    // 音频倍速转码输出
                    ByteBuffer outBuffer = mAudioTranscoder.getOutput();
                    if (outBuffer != null && outBuffer.hasRemaining()) {
                        byte[] outData = new byte[outBuffer.remaining()];
                        outBuffer.get(outData);
                        mListener.onAudioDataProvide(outData, outData.length);
                    }
                    if (audioReader instanceof CAVCaptureAudioMuteInput) {
                        mAudioTranscoder.flush();
                    }
                } else {
                    mListener.onAudioDataProvide(pcmData, size);
                }
            }
        }

        // 刷新缓冲区
        synchronized (this) {
            if (mAudioTranscoder != null && mListener != null) {
                mAudioTranscoder.endOfStream();
                ByteBuffer outBuffer = mAudioTranscoder.getOutput();
                if (outBuffer != null && outBuffer.hasRemaining()) {
                    byte[] output = new byte[outBuffer.remaining()];
                    outBuffer.get(output);
                    synchronized (this) {
                        if (mListener != null) {
                            mListener.onAudioDataProvide(output, output.length);
                        }
                    }
                }
            }
            if (mListener != null) {
                mListener.onAudioDataProvide(null, 0);
            }
        }

        // 停止回调
        if (mListener != null) {
            mListener.onAudioProcessFinish();
        }

        release();
    }

    /**
     * 计算出音频读取的总时间
     */
    private void calculateAudioReadTimeUs(int audioSize, @NonNull CAVAudioInfo info) {
        // 记录音频读取的总长度和总时间戳
        mTotalBytesRead += audioSize;
        mPresentationTimeUs = 1000000L * (mTotalBytesRead / info.getChannelCount() / 2) / info.getSampleRate();
        Log.d(TAG, "calculateAudioReadTimeUs: " + mPresentationTimeUs);
    }

    /**
     * 初始化音频倍速处理器
     */
    private void initAudioTranscoder(@NonNull CAVAudioInfo audioInfo, float speed) {
        if (mAudioTranscoder == null) {
            try {
                mAudioTranscoder = new CAVAudioTranscoder();
                mAudioTranscoder.setSpeed(speed);
                mAudioTranscoder.configure(audioInfo.getSampleRate(), audioInfo.getChannelCount(), audioInfo.getAudioFormat());
                mAudioTranscoder.setOutputSampleRateHz(audioInfo.getSampleRate());
                mAudioTranscoder.flush();
            } catch (Exception e) {
                mAudioTranscoder = null;
            }
        }
    }

    public interface OnAudioProcessorListener {

        void onAudioDataProvide(byte[] data, int length);

        void onAudioProcessFinish();
    }

    public void setOnAudioProcessorListener(OnAudioProcessorListener listener) {
        mListener = listener;
    }
}
