package com.cgfay.media.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 音频录制器
 * @author CainHuang
 * @date 2019/6/30
 */
public final class AudioRecorder implements Runnable {

    private int mBufferSize = AudioEncoder.BUFFER_SIZE;

    // 录音器
    private AudioRecord mAudioRecord;
    // 音频转码器
    private AudioTranscoder mAudioTranscoder;
    // 音频编码器
    private AudioEncoder mAudioEncoder;
    // 音频参数
    private AudioParams mAudioParams;
    // 录制标志位
    private volatile boolean mRecording;
    // 最小缓冲大小
    private int minBufferSize;
    // 录制状态监听器
    private OnRecordListener mRecordListener;

    public MediaType getMediaType() {
        return MediaType.AUDIO;
    }

    public void setOnRecordListener(OnRecordListener listener) {
        mRecordListener = listener;
    }

    /**
     * 开始录制
     */
    public void startRecord() {
        mRecording = true;
        new Thread(this).start();
    }

    /**
     * 停止录制
     */
    public void stopRecord() {
        mRecording = false;
    }

    /**
     * 准备编码器
     * @throws IOException
     */
    public void prepare(@NonNull AudioParams params) throws Exception {
        mAudioParams = params;
        if (mAudioRecord != null) {
            release();
        }
        if (mAudioEncoder != null) {
            mAudioEncoder.release();
        }

        float speed = params.getSpeedMode().getSpeed();
        try {
            minBufferSize = (int)(params.getSampleRate() * 4 * 0.02);
            if (mBufferSize < minBufferSize / speed * 2) {
                mBufferSize = (int) (minBufferSize / speed * 2);
            } else {
                mBufferSize = AudioEncoder.BUFFER_SIZE;
            }
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, params.getSampleRate(),
                    params.getChannel(), params.getAudioFormat(), minBufferSize);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        int channelCount = (params.getChannel() == AudioFormat.CHANNEL_IN_MONO)? 1 : 2;

        // 音频编码器
        mAudioEncoder = new AudioEncoder(params.getBitRate(), params.getSampleRate(), channelCount);
        mAudioEncoder.setBufferSize(mBufferSize);
        mAudioEncoder.setOutputPath(params.getAudioPath());
        mAudioEncoder.prepare();

        // 音频转码器
        mAudioTranscoder = new AudioTranscoder();
        mAudioTranscoder.setSpeed(speed);
        mAudioTranscoder.configure(params.getSampleRate(), channelCount, params.getAudioFormat());
        mAudioTranscoder.setOutputSampleRateHz(params.getSampleRate());
        mAudioTranscoder.flush();
    }

    /**
     * 释放数据
     */
    public synchronized void release() {
        if (mAudioRecord != null) {
            try {
                mAudioRecord.release();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mAudioRecord = null;
            }
        }
        if (mAudioEncoder != null) {
            mAudioEncoder.release();
            mAudioEncoder = null;
        }
    }

    @Override
    public void run() {
        long duration = 0;
        try {

            // 初始化录音器
            boolean needToStart = true;
            while (mRecording && needToStart) {
                synchronized (this) {
                    if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                        mAudioRecord.startRecording();
                        // 录制开始回调
                        if (mRecordListener != null) {
                            mRecordListener.onRecordStart(MediaType.AUDIO);
                        }
                        needToStart = false;
                    }
                }
                SystemClock.sleep(10);
            }

            byte[] pcmData = new byte[minBufferSize];
            // 录制编码
            while (mRecording) {
                int size;
                // 取出录音PCM数据
                synchronized (this) {
                    if (mAudioRecord == null) {
                        break;
                    }
                    size = mAudioRecord.read(pcmData, 0, pcmData.length);
                }
                // 将音频送去转码处理
                if (size > 0) {
                    ByteBuffer inBuffer = ByteBuffer.wrap(pcmData,0,size).order(ByteOrder.LITTLE_ENDIAN);
                    mAudioTranscoder.queueInput(inBuffer);
                } else {
                    Thread.sleep(100);
                }

                // 音频倍速转码输出
                ByteBuffer outPut = mAudioTranscoder.getOutput();
                if (outPut != null && outPut.hasRemaining()) {
                    byte[] outData = new byte[outPut.remaining()];
                    outPut.get(outData);
                    synchronized (this) {
                        if (mAudioEncoder == null) {
                            break;
                        }
                        mAudioEncoder.encodePCM(outData, outData.length);
                    }
                } else {
                    Thread.sleep(5);
                }
            }

            // 刷新缓冲区
            synchronized (this) {
                if (mAudioTranscoder != null) {
                    mAudioTranscoder.endOfStream();
                    ByteBuffer outBuffer = mAudioTranscoder.getOutput();
                    if (outBuffer != null && outBuffer.hasRemaining()) {
                        byte[] output = new byte[outBuffer.remaining()];
                        outBuffer.get(output);
                        synchronized (this) {
                            if (mAudioEncoder != null) {
                                mAudioEncoder.encodePCM(output, output.length);
                            }
                        }
                    }
                }
                if (mAudioEncoder != null) {
                    mAudioEncoder.encodePCM(null, -1);
                }
            }
            if (mAudioEncoder != null) {
                duration = mAudioEncoder.getDuration();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 录制完成回调
        if (mRecordListener != null) {
            mRecordListener.onRecordFinish(new RecordInfo(mAudioParams.getAudioPath(), duration, getMediaType()));
        }
    }

}
