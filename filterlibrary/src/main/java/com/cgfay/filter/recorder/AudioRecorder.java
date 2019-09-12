package com.cgfay.filter.recorder;

import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;

import com.cgfay.media.SoundTouch;
import com.cgfay.uitls.utils.FileUtils;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 音频AAC录制器
 * @author CainHuang
 * @date 2019/6/30
 */
public final class AudioRecorder implements Runnable {

    private static final int SECOND_IN_US = 1000000;

    private static final int BUFFER_TIME_OUT = 10000;

    // 录音器
    private AudioRecord mRecorder;
    // 录音Handler
    private Handler mRecordHandler;
    // 编码Handler
    private Handler mEncodeHandler;
    // 编码器
    private MediaCodec mMediaCodec;
    // 缓冲区
    private MediaCodec.BufferInfo mBufferInfo;
    // 音频参数
    private AudioParams mAudioParams;
    // SoundTouch
    private SoundTouch mSoundTouch;
    // 文件流
    private BufferedOutputStream mFileStream;
    // 录制状态监听器
    private OnRecordListener mRecordListener;
    // 采样时长
    private long mSampleDuration;
    // 时长
    private long mDuration;

    public MediaType getMediaType() {
        return MediaType.AUDIO;
    }

    public void setOnRecordListener(OnRecordListener listener) {
        mRecordListener = listener;
    }

    /**
     * 绑定Handler线程
     * @param tag
     * @return
     */
    private static Handler bindHandlerThread(String tag) {
        HandlerThread thread = new HandlerThread(tag);
        thread.start();
        return new Handler(thread.getLooper());
    }

    /**
     * 循环遍历
     */
    private void loop() {
        mRecordHandler.post(this);
    }

    /**
     * 开始录制
     */
    public void startRecord() {
        mRecorder.startRecording();
        mMediaCodec.start();
        loop();
        if (mRecordListener != null) {
            mRecordListener.onRecordStart(MediaType.AUDIO);
        }
    }

    /**
     * 停止录制
     */
    public void stopRecord() {
        if (mRecordHandler == null) {
            return;
        }
        mRecordHandler.post(() -> {
            if (mRecorder == null) {
                return;
            }
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        });
    }

    /**
     * 准备编码器
     * @throws IOException
     */
    public void prepare(AudioParams params) throws IOException {

        mAudioParams = params;
        int bufferSize = AudioRecord.getMinBufferSize(
                params.getSampleRate(), AudioParams.CHANNEL,
                AudioParams.BITS_PER_SAMPLE);
        mRecorder = new AudioRecord(
                MediaRecorder.AudioSource.MIC, params.getSampleRate(),
                AudioParams.CHANNEL, AudioParams.BITS_PER_SAMPLE, bufferSize);
        mRecordHandler = bindHandlerThread("AudioRecordThread");
        mEncodeHandler = bindHandlerThread("AudioEncodeThread");

        // 创建变速变调处理库
        mSoundTouch = new SoundTouch();
        mSoundTouch.setChannels(AudioParams.CHANNEL_COUNT);
        mSoundTouch.setSampleRate(mAudioParams.getSampleRate());

        // 变速不变调
        float rate = params.getSpeedMode().getSpeed();
        mSoundTouch.setRate(rate);
        mSoundTouch.setPitch(1.0f / rate);

        mSampleDuration = params.getNbSamples() * SECOND_IN_US / params.getSampleRate();
        mDuration = 0;


        MediaFormat audioFormat = MediaFormat.createAudioFormat(AudioParams.MIME_TYPE,
                AudioParams.SAMPLE_RATE, AudioParams.CHANNEL_COUNT);
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioParams.CHANNEL);
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, AudioParams.BIT_RATE);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, AudioParams.CHANNEL_COUNT);
        audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1024 * 4);
        mMediaCodec = MediaCodec.createEncoderByType(AudioParams.MIME_TYPE);
        mMediaCodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mBufferInfo = new MediaCodec.BufferInfo();
        mFileStream = new BufferedOutputStream(new FileOutputStream(mAudioParams.getAudioPath()));
    }

    /**
     * 释放数据
     */
    public void release() {
        if (mEncodeHandler != null) {
            mEncodeHandler.getLooper().quitSafely();
            mEncodeHandler = null;
        }
        if (mRecordHandler != null) {
            mRecordHandler.getLooper().quitSafely();
            mRecordHandler = null;
        }
    }

    @Override
    public void run() {
        if (mRecorder == null) {
            //通知编码线程退出
            stopEncode();
            mRecordHandler.getLooper().quitSafely();
            mRecordHandler = null;
            return;
        }
        byte[] buffer = new byte[mAudioParams.getNbSamples()];
        int bytes = mRecorder.read(buffer, 0, buffer.length);
        if (bytes > 0) {
            pcmEncode(buffer, bytes);
        }
        loop();
    }

    /**
     * 停止编码
     */
    private void stopEncode() {
        mEncodeHandler.post(() -> {
            if (mMediaCodec == null) {
                return;
            }
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
            FileUtils.closeSafely(mFileStream);
            mFileStream = null;
            mSoundTouch.close();
            mSoundTouch = null;
            if (mRecordListener != null) {
                mRecordListener.onRecordFinish(new RecordInfo(mAudioParams.getAudioPath(),
                        mDuration, getMediaType()));
            }
            mEncodeHandler.getLooper().quitSafely();
            mEncodeHandler = null;
        });
    }

    /**
     * 编码一帧数据
     * @param data
     * @param length
     */
    private void pcmEncode(final byte[] data, final int length) {
        mEncodeHandler.post(() -> {
            if (mSoundTouch != null) {
                mSoundTouch.putSamples(data);
                while (true) {
                    byte[] modified = new byte[4096];
                    int count = mSoundTouch.receiveSamples(modified);
                    if (count > 0) {
                        onEncode(modified, count * 2);
                        drain();
                    } else {
                        break;
                    }
                }
            } else {
                onEncode(data, length);
                drain();
            }
        });
    }

    /**
     * 获取时钟
     * @return
     */
    private long getTimeUs() {
        return System.nanoTime() / 1000L;
    }

    /**
     * 编码一帧数据
     * @param data
     * @param length
     */
    private void onEncode(byte[] data, int length) {
        final ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
        while (true) {
            final int inputBufferIndex = mMediaCodec.dequeueInputBuffer(BUFFER_TIME_OUT);
            if (inputBufferIndex >= 0) {
                final ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                inputBuffer.position(0);
                if (data != null) {
                    inputBuffer.put(data, 0, length);
                }
                if (length <= 0) {
                    mMediaCodec.queueInputBuffer(inputBufferIndex, 0, 0,
                            getTimeUs(), MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    break;
                } else {
                    mMediaCodec.queueInputBuffer(inputBufferIndex, 0, length,
                            getTimeUs(), 0);
                }
                break;
            }
        }
    }

    /**
     * 将音频数据写入文件中
     */
    private void drain() {
        mBufferInfo = new MediaCodec.BufferInfo();
        ByteBuffer[] encoderOutputBuffers = mMediaCodec.getOutputBuffers();
        int encoderStatus = mMediaCodec.dequeueOutputBuffer(mBufferInfo, BUFFER_TIME_OUT);
        while (encoderStatus >= 0) {
            ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
            int outSize = mBufferInfo.size;
            encodedData.position(mBufferInfo.offset);
            encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
            byte[] data = new byte[outSize + 7];
            addADTSHeader(data, outSize + 7);
            encodedData.get(data, 7, outSize);
            try {
                mFileStream.write(data, 0, data.length);
                mFileStream.flush();
                mDuration += mSampleDuration;
            } catch (IOException e) {
               e.printStackTrace();
            }

            if (mDuration >= mAudioParams.getMaxDuration()) {
                stopRecord();
            }
            mMediaCodec.releaseOutputBuffer(encoderStatus, false);
            encoderStatus = mMediaCodec.dequeueOutputBuffer(mBufferInfo, BUFFER_TIME_OUT);
        }
    }

    /**
     * 添加AAC的ADTS头部信息
     * @param packet
     * @param length
     */
    private void addADTSHeader(byte[] packet, int length) {
        int audioType = 2;  // AAC LC, Audio Object Type
        int freqIndex = 4;  // 44.1KHz, Sampling Frequency Index
        int channels = 1;   // 1 channel, Channel Configuration
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((audioType - 1) << 6) + (freqIndex << 2) + (channels >> 2));
        packet[3] = (byte) (((channels & 3) << 6) + (length >> 11));
        packet[4] = (byte) ((length & 0x7FF) >> 3);
        packet[5] = (byte) (((length & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

}
