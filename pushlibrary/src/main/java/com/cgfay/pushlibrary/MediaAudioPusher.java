package com.cgfay.pushlibrary;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

/**
 * 音频硬编码推流器
 * Created by cain on 2018/1/22.
 */

public class MediaAudioPusher extends MediaPusher {

    private static final boolean VERBOSE = false;
    private static final String TAG = "MediaAudioPusher";

    private static final String MIME_TYPE = "audio/mp4a-latm";
    private static final int SAMPLE_RATE = 44100;
    private static final int BIT_RATE = 64000;
    public static final int SAMPLES_PER_FRAME = 1024;
    public static final int FRAMES_PER_BUFFER = 25;

    private AudioThread mAudioThread = null;

    public MediaAudioPusher(final MediaRtmpMuxer muxer, final MediaPusherListener listener) {
        super(muxer, listener, false);
    }

    @Override
    void prepare() throws IOException {
        mTrackIndex = -1;
        mMuxerStarted = mIsEOS = false;
        final MediaCodecInfo audioCodecInfo = selectAudioCodec(MIME_TYPE);
        if (audioCodecInfo == null) {
            Log.e(TAG, "Unable to find an appropriate codec for " + MIME_TYPE);
            return;
        }
        if (VERBOSE) {
            Log.i(TAG, "selected codec: " + audioCodecInfo.getName());
        }
        final MediaFormat audioFormat = MediaFormat.createAudioFormat(MIME_TYPE, SAMPLE_RATE,
                1);
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO);
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
        if (VERBOSE) {
            Log.i(TAG, "format: " + audioFormat);
        }
        mMediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
        mMediaCodec.configure(audioFormat, null, null,
                MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec.start();
        if (VERBOSE) {
            Log.i(TAG, "prepare finishing");
        }
        if (mListener != null) {
            try {
                mListener.onPrepared(this);
            } catch (final Exception e) {
                Log.e(TAG, "prepare:", e);
            }
        }
    }

    @Override
    void startPushing() {
        super.startPushing();
        if (mAudioThread == null) {
            mAudioThread = new AudioThread();
            mAudioThread.start();
        }
        if (mListener != null) {
            try {
                mListener.onStarted(this);
            } catch (final Exception e) {
                Log.e(TAG, "startPushing: ", e);
            }
        }
    }

    @Override
    protected void release() {
        if (mAudioThread != null) {
            mAudioThread.stopRecording();
            mAudioThread = null;
        }
        super.release();
    }

    /**
     * 录制线程
     */
    private class AudioThread extends Thread {

        private Object mSync = new Object();
        private volatile boolean mAudioStarted = false;
        private WeakReference<AudioRecord> mWeakRecorder;

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            try {
                final int min_buffer_size = AudioRecord.getMinBufferSize(
                        SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);
                int buffer_size = SAMPLES_PER_FRAME * FRAMES_PER_BUFFER;
                if (buffer_size < min_buffer_size)
                    buffer_size = ((min_buffer_size / SAMPLES_PER_FRAME) + 1) * SAMPLES_PER_FRAME * 2;

                AudioRecord audioRecord = null;
                for (final int source : AUDIO_SOURCES) {
                    try {
                        audioRecord = new AudioRecord(
                                source, SAMPLE_RATE,
                                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, buffer_size);
                        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED)
                            audioRecord = null;
                    } catch (final Exception e) {
                        audioRecord = null;
                    }
                    if (audioRecord != null) break;
                }
                if (audioRecord != null) {
                    mWeakRecorder = new WeakReference<AudioRecord>(audioRecord);
                    try {
                        if (mIsPushing) {
                            if (VERBOSE) {
                                Log.d(TAG, "AudioThread:start audio recording");
                            }
                            final ByteBuffer buf = ByteBuffer.allocateDirect(SAMPLES_PER_FRAME);
                            int readBytes;
                            synchronized (mSync) {
                                audioRecord.startRecording();
                                mAudioStarted = true;
                            }
                            try {
                                for (; mIsPushing && !mRequestStop && !mIsEOS && mAudioStarted;) {
                                    buf.clear();
                                    readBytes = audioRecord.read(buf, SAMPLES_PER_FRAME);
                                    if (readBytes > 0 ) {
                                        // 音频编码
                                        buf.position(readBytes);
                                        buf.flip();
                                        encode(buf, readBytes, getPTSUs());
                                        frameAvailable();
                                    }
                                }
                                frameAvailable();
                            } finally {
                                synchronized (mSync) {
                                    mAudioStarted = false;
                                    audioRecord.stop();
                                }
                            }
                        }
                    } finally {
                        synchronized (mSync) {
                            mAudioStarted = false;
                            audioRecord.release();
                            if (mWeakRecorder != null) {
                                mWeakRecorder.clear();
                                mWeakRecorder = null;
                            }
                        }
                    }
                } else {
                    Log.e(TAG, "failed to initialize AudioRecord");
                }
            } catch (final Exception e) {
                Log.e(TAG, "AudioThread#run", e);
            }
            if (VERBOSE) {
                Log.d(TAG, "AudioThread:finished");
            }
        }

        /**
         * 开始录音
         */
        public void startRecording() {
            if (mAudioStarted) {
                return;
            }
            synchronized (mSync) {
                if (mWeakRecorder != null && mWeakRecorder.get() != null) {
                    mWeakRecorder.get().startRecording();
                    mAudioStarted = true;
                }
            }
        }

        /**
         * 停止录音
         */
        public void stopRecording() {
            if (!mAudioStarted) {
                return;
            }
            synchronized (mSync) {
                if (mWeakRecorder != null && mWeakRecorder.get() != null) {
                    mWeakRecorder.get().stop();
                    mAudioStarted = false;
                }
            }
        }

    }

    /**
     * 音频来源
     */
    private static final int[] AUDIO_SOURCES = new int[] {
            MediaRecorder.AudioSource.MIC,
            MediaRecorder.AudioSource.DEFAULT,
            MediaRecorder.AudioSource.CAMCORDER,
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
    };

    /**
     * 选择合适的音频编码器
     * @param mimeType
     * @return
     */
    private static final MediaCodecInfo selectAudioCodec(final String mimeType) {
        if (VERBOSE) {
            Log.d(TAG, "selectAudioCodec:");
        }

        MediaCodecInfo result = null;
        // 获取可用的编码器
        final int numCodecs = MediaCodecList.getCodecCount();
        LOOP:	for (int i = 0; i < numCodecs; i++) {
            final MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {	// skipp decoder
                continue;
            }
            final String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (VERBOSE) {
                    Log.i(TAG, "supportedType:" + codecInfo.getName() + ", MIME=" + types[j]);
                }
                if (types[j].equalsIgnoreCase(mimeType)) {
                    if (result == null) {
                        result = codecInfo;
                        break LOOP;
                    }
                }
            }
        }
        return result;
    }
}
