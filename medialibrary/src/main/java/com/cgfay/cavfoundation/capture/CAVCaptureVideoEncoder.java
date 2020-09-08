package com.cgfay.cavfoundation.capture;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.cgfay.cavfoundation.codec.CAVVideoInfo;

import java.io.IOException;

/**
 * 录制视频编码器
 */
class CAVCaptureVideoEncoder extends CAVCaptureEncoder {
    private static final String TAG = "CAVCaptureVideoEncoder";

    private CAVVideoInfo mVideoInfo;

    private Surface mSurface;

    public CAVCaptureVideoEncoder(@NonNull CAVCaptureMuxer muxer, @NonNull OnCaptureEncoderListener listener) {
        super(muxer, listener);
    }

    /**
     *
     * @param info
     */
    public void setVideoInfo(@NonNull CAVVideoInfo info) {
        mVideoInfo = info;
    }

    /**
     * 准备视频编码器
     * @throws IOException
     */
    @Override
    protected void prepare() throws IOException {
        if (mVideoInfo == null) {
            return;
        }
        if (VERBOSE) {
            Log.d(TAG, "prepare: ");
        }
        mTrackIndex = -1;
        mMuxerStarted = mIsEOS = false;

        final MediaCodecInfo videoCodecInfo = selectVideoCodec(mVideoInfo.getMimeType());
        if (videoCodecInfo == null) {
            Log.e(TAG, "Unable to find an appropriate codec for " + mVideoInfo.getMimeType());
            return;
        }
        if (VERBOSE) {
            Log.d(TAG, "selected codec: " + videoCodecInfo.getName());
        }

        final MediaFormat format = MediaFormat.createVideoFormat(mVideoInfo.getMimeType(),
                mVideoInfo.getWidth(), mVideoInfo.getHeight());
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, mVideoInfo.getBitRate());
        format.setInteger(MediaFormat.KEY_FRAME_RATE, mVideoInfo.getFrameRate());
        int iFrameInterval = mVideoInfo.getFrameRate() / mVideoInfo.getGopSize();
        if (iFrameInterval <= 0) {
            iFrameInterval = 1;
        }
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iFrameInterval);
        // 录制时禁用B帧
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            format.setInteger(MediaFormat.KEY_MAX_B_FRAMES, 0);
        }

        if (VERBOSE) {
            Log.d(TAG, "prepare: " + format);
        }

        mMediaCodec = MediaCodec.createEncoderByType(mVideoInfo.getMimeType());
        mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mSurface = mMediaCodec.createInputSurface();
        mMediaCodec.start();
        if (VERBOSE) {
            Log.d(TAG, "prepare finishing");
        }
        // 准备完成回调
        if (mListener != null) {
            mListener.onPrepared(this);
        }
    }

    @Override
    protected void release() {
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        super.release();
    }

    @Override
    protected void signalEndOfInputStream() {
        if (VERBOSE) {
            Log.d(TAG, "signalEndOfInputStream: ");
        }
        if (mMediaCodec != null) {
            mMediaCodec.signalEndOfInputStream();
        }
        mIsEOS = true;
    }

    /**
     * 获取编码输入Surface
     */
    public Surface getInputSurface() {
        return mSurface;
    }

    /**
     * select the first codec that match a specific MIME type
     * @param mimeType
     * @return null if no codec matched
     */
    protected static final MediaCodecInfo selectVideoCodec(final String mimeType) {
        if (VERBOSE) {
            Log.v(TAG, "selectVideoCodec:");
        }

        // get the list of available codecs
        final int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            final MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder()) {	// skipp decoder
                continue;
            }
            // select first codec that match a specific MIME type and color format
            final String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    if (VERBOSE) {
                        Log.i(TAG, "codec:" + codecInfo.getName() + ",MIME=" + types[j]);
                    }
                    final int format = selectColorFormat(codecInfo, mimeType);
                    if (format > 0) {
                        return codecInfo;
                    }
                }
            }
        }
        return null;
    }

    /**
     * select color format available on specific codec and we can use.
     * @return 0 if no colorFormat is matched
     */
    protected static final int selectColorFormat(final MediaCodecInfo codecInfo, final String mimeType) {
        if (VERBOSE) {
            Log.i(TAG, "selectColorFormat: ");
        }
        int result = 0;
        final MediaCodecInfo.CodecCapabilities caps;
        try {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            caps = codecInfo.getCapabilitiesForType(mimeType);
        } finally {
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
        }
        int colorFormat;
        for (int i = 0; i < caps.colorFormats.length; i++) {
            colorFormat = caps.colorFormats[i];
            if (isRecognizedVideoFormat(colorFormat)) {
                result = colorFormat;
                break;
            }
        }
        if (result == 0) {
            Log.e(TAG, "couldn't find a good color format for " + codecInfo.getName() + " / " + mimeType);
        }
        return result;
    }

    /**
     * color formats that we can use in this class
     */
    protected static int[] recognizedFormats;
    static {
        recognizedFormats = new int[] {
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface,
        };
    }

    private static boolean isRecognizedVideoFormat(final int colorFormat) {
        if (VERBOSE) {
            Log.i(TAG, "isRecognizedVideoFormat: colorFormat = " + colorFormat);
        }
        final int size = recognizedFormats != null ? recognizedFormats.length : 0;
        for (int i = 0; i < size; i++) {
            if (recognizedFormats[i] == colorFormat) {
                return true;
            }
        }
        return false;
    }

}
