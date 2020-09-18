package com.cgfay.cavfoundation.capture;

import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;

import androidx.annotation.NonNull;

import com.cgfay.cavfoundation.codec.CAVAudioInfo;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 录制音频编码器
 */
class CAVCaptureAudioEncoder extends CAVCaptureEncoder {

    private static final String TAG = "CAVCaptureAudioEncoder";
    private static final int MAX_BUFFER_SIZE = 102400;

    private int mMaxBufferSize = MAX_BUFFER_SIZE;
    private int mTotalBytesRead;
    private long mPresentationTimeUs;

    /**
     * 音频参数
     */
    private CAVAudioInfo mAudioInfo;

    public CAVCaptureAudioEncoder(@NonNull CAVCaptureMuxer muxer, @NonNull OnCaptureEncoderListener listener) {
        super(muxer, listener);
    }

    /**
     * 设置音频参数
     */
    public void setAudioInfo(@NonNull CAVAudioInfo info) {
        mAudioInfo = info;
    }

    /**
     * 准备编码器
     *
     * @throws IOException
     */
    @Override
    void prepare() throws IOException {
        if (mAudioInfo == null) {
            return;
        }
        mTotalBytesRead = 0;
        mPresentationTimeUs = 0;
        mTrackIndex = -1;
        mMuxerStarted = mIsEOS = false;
        mStartTimeUs = 0;
        mLastTimeUs = 0;
        final MediaCodecInfo audioCodecInfo = selectAudioCodec(mAudioInfo.getMimeType());
        if (audioCodecInfo == null) {
            Log.e(TAG, "Unable to find an appropriate codec for " + mAudioInfo.getMimeType());
            return;
        }

        if (VERBOSE) {
            Log.d(TAG, "selected codec: " + audioCodecInfo.getName());
        }

        final MediaFormat format = MediaFormat.createAudioFormat(mAudioInfo.getMimeType(),
                mAudioInfo.getSampleRate(), mAudioInfo.getChannelCount());
        format.setInteger(MediaFormat.KEY_AAC_PROFILE,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        format.setInteger(MediaFormat.KEY_CHANNEL_MASK,
                mAudioInfo.getChannelCount() == 2 ? AudioFormat.CHANNEL_IN_STEREO
                        : AudioFormat.CHANNEL_IN_MONO);
        format.setInteger(MediaFormat.KEY_BIT_RATE, mAudioInfo.getBitRate());
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, mMaxBufferSize);

        if (VERBOSE) {
            Log.d(TAG, "prepare: " + format);
        }

        mMediaCodec = MediaCodec.createEncoderByType(mAudioInfo.getMimeType());
        mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaCodec.start();
        if (VERBOSE) {
            Log.d(TAG, "prepare: prepare finish");
        }
        if (mListener != null) {
            mListener.onPrepared(this);
        }
    }

    @Override
    protected void signalEndOfInputStream() {
        encodeData(null, 0);
    }

    /**
     * 编码PCM数据
     * @param pcmData   pcm数据
     * @param length    pcm数据长度
     */
    public void encodeData(byte[] pcmData, int length) {
        if (!mIsCapturing) {
            return;
        }
        final ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
        while (mIsCapturing) {
            final int inputBufferIndex = mMediaCodec.dequeueInputBuffer(TIMEOUT_USEC);
            if (inputBufferIndex >= 0) {
                final ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                if (length <= 0 || pcmData == null) {
                    mIsEOS = true;
                    if (VERBOSE) {
                        Log.i(TAG, "encodeData: BUFFER_FLAG_END_OF_STREAM");
                    }
                    mMediaCodec.queueInputBuffer(inputBufferIndex, 0, 0,
                            mPresentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                } else {
                    mTotalBytesRead += length;
                    inputBuffer.put(pcmData, 0, length);
                    mMediaCodec.queueInputBuffer(inputBufferIndex, 0, length,
                            mPresentationTimeUs, 0);
                    mPresentationTimeUs = 1000000L * (mTotalBytesRead / mAudioInfo.getChannelCount() / 2)
                            / mAudioInfo.getSampleRate();
                }
                frameAvailable();
                break;
            } else if (inputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (VERBOSE) {
                    Log.d(TAG, "encodeData: INFO_TRY_AGAIN_LATER");
                }
            }
        }
    }

    /**
     * select the first codec that match a specific MIME type
     * @param mimeType
     * @return
     */
    private static final MediaCodecInfo selectAudioCodec(final String mimeType) {
        if (VERBOSE) {
            Log.v(TAG, "selectAudioCodec:");
        }
        MediaCodecInfo result = null;
        final int numCodecs = MediaCodecList.getCodecCount();
LOOP:	for (int i = 0; i < numCodecs; i++) {
            final MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {	// skipp decoder
                continue;
            }
            final String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (VERBOSE) {
                    Log.i(TAG, "supportedType:" + codecInfo.getName() + ",MIME=" + types[j]);
                }
                if (types[j].equalsIgnoreCase(mimeType)) {
                    result = codecInfo;
                    break LOOP;
                }
            }
        }
        return result;
    }
}
