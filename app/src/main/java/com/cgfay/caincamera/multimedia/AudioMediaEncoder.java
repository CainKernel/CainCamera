package com.cgfay.caincamera.multimedia;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Process;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 音频编码器
 * Created by cain on 2017/10/14.
 */
public class AudioMediaEncoder extends MediaEncoder {

    private static final boolean VERBOSE = false;
    private static final String TAG = "AudioMediaEncoder";

    private static final String MIME_TYPE = "audio/mp4a-latm";
    private static final int SAMPLE_RATE = 44100;
    private static final int BIT_RATE = 64000;
    public static final int SAMPLES_PER_FRAME = 1024;
    public static final int FRAMES_PER_BUFFER = 25;

    private static final int CHANNEL_COUNT = 1;


    private AudioThread mAudioThread = null;

    public AudioMediaEncoder(MediaEncoderMuxer muxer, EncoderListener listener) {
        super(muxer, listener);
    }

    @Override
    protected void prepare() throws IOException {
        mTrackIndex = -1;
        mMuxerStarted = mIsEOS = false;
        MediaCodecInfo info = getAudioCodecInfo(MIME_TYPE);
        if (info == null) {
            return;
        }

        MediaFormat format = MediaFormat.createAudioFormat(MIME_TYPE, SAMPLE_RATE, CHANNEL_COUNT);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        format.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO);
        format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, CHANNEL_COUNT);

        mMediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
        mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec.start();
        if (mListener != null) {
            mListener.onPrepared(this);
        }
    }

    @Override
    void startRecording() {
        super.startRecording();
        if (mAudioThread == null) {
            mAudioThread = new AudioThread();
            mAudioThread.start();
        }
    }

    @Override
    protected void release() {
        mAudioThread = null;
        super.release();
    }

    /**
     * 获取音频Codec信息
     * @param mimeType
     * @return
     */
    private static final MediaCodecInfo getAudioCodecInfo(final String mimeType) {
        MediaCodecInfo result = null;
        int count = MediaCodecList.getCodecCount();
        for (int i = 0; i < count; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            // 跳过解码的
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    result = codecInfo;
                    return result;
                }
            }
        }
        return null;
    }

    // 声音输入源
    private static final int[] AUDIO_SOURCES = new int[] {
            MediaRecorder.AudioSource.MIC,
            MediaRecorder.AudioSource.DEFAULT,
            MediaRecorder.AudioSource.CAMCORDER,
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
    };

    // 录制线程
    private class AudioThread extends Thread {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
            try {
                int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                int bufferSize = SAMPLES_PER_FRAME * FRAMES_PER_BUFFER;
                if (bufferSize < minBufferSize) {
                    bufferSize = ((minBufferSize / SAMPLES_PER_FRAME) + 1) * SAMPLES_PER_FRAME * 2;
                }
                
                AudioRecord record = null;
                for (int source : AUDIO_SOURCES) {
                    try {
                        record = new AudioRecord(source, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                                AudioFormat.ENCODING_PCM_16BIT, bufferSize);
                        if (record.getState() != AudioRecord.STATE_INITIALIZED) {
                            record = null;
                        }
                    } catch (Exception e) {
                        record = null;
                    }
                    if (record != null) {
                        break;
                    }
                }
                if (record != null) {
                    try {
                        if (mIsCapturing) {
                            ByteBuffer buffer = ByteBuffer.allocateDirect(SAMPLES_PER_FRAME);
                            int readBytes;
                            record.startRecording();
                            try {
                                while (mIsCapturing && !mRequestStop && !mIsEOS) {
                                    buffer.clear();
                                    readBytes = record.read(buffer, SAMPLES_PER_FRAME);
                                    if (readBytes > 0) {
                                        buffer.position(readBytes);
                                        buffer.flip();
                                        encode(buffer, readBytes, getPTSUs());
                                        frameAvailable();
                                    }
                                }
                                frameAvailable();
                            } finally {
                                record.stop();
                            }
                        }
                    } finally {
                        record.release();
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "AudioThread#run", e);
            }
        }
    }
}
