package com.cgfay.pushlibrary;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;

/**
 * Created by cain on 2018/1/22.
 */

public class MediaVideoPusher extends MediaPusher {

    private static final boolean VERBOSE = false;
    private static final String TAG = "MediaVideoPusher";

    private static final String MIME_TYPE = "video/avc";
    private static final int FRAME_RATE = 24;
    private static final float BPP = 0.25f;
    private static final int I_FRAME_INTERVAL = 1;
    private static final int HDValue = 4;

    private int mBitRate = 0;
    private boolean enableHD = false;

    private final int mWidth;
    private final int mHeight;

    private Surface mSurface;

    public MediaVideoPusher(final MediaRtmpMuxer muxer, final MediaPusherListener listener,
                            final int width, final int height) {
        super(muxer, listener, true);
        if (VERBOSE) {
            Log.i(TAG, "MediaVideoPusher: ");
        }
        mWidth = width;
        mHeight = height;
    }

    public MediaVideoPusher(final MediaRtmpMuxer muxer, final MediaPusherListener listener,
                            final int width, final int height, final boolean enableHD) {
        super(muxer, listener, true);
        if (VERBOSE) {
            Log.i(TAG, "MediaVideoPusher: ");
        }
        mWidth = width;
        mHeight = height;
        this.enableHD = enableHD;
    }


    @Override
    void prepare() throws IOException {
        if (VERBOSE) {
            Log.i(TAG, "prepare: ");
        }
        mTrackIndex = -1;
        mMuxerStarted = mIsEOS = false;
        final MediaCodecInfo videoCodeInfo = selectVideoCodec(MIME_TYPE);
        if (videoCodeInfo == null) {
            Log.e(TAG, "unable to find an appropriate codec for" + MIME_TYPE);
            return;
        }
        if (VERBOSE) {
            Log.i(TAG, "selected codec: " + videoCodeInfo.getName());
        }

        int videoWidth = mWidth % 2 == 0 ? mWidth : mWidth - 1;
        int videoHeight = mHeight % 2 == 0 ? mHeight : mHeight - 1;
        final MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, videoWidth, videoHeight);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate > 0 ? mBitRate : calcBitRate());
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL);
        if (VERBOSE) {
            Log.i(TAG, "format: " + format.toString());
        }

        mMediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
        mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mSurface = mMediaCodec.createInputSurface();
        mMediaCodec.start();
        if (VERBOSE) {
            Log.i(TAG, "prepare: finishing");
        }
        if (mListener != null) {
            try {
                mListener.onPrepared(this);
            } catch (final Exception e) {
                Log.e(TAG, "prepare: ", e);
            }
        }
    }

    @Override
    void startPushing() {
        super.startPushing();
        if (mListener != null) {
            try {
                mListener.onStarted(this);
            } catch (final Exception e) {
                Log.e(TAG, "startPushing: ", e);
            }
        }
    }

    /**
     * 获取编码器输入的surface
     * @return
     */
    public Surface getInputSurface() {
        return mSurface;
    }

    @Override
    protected void release() {
        if (VERBOSE) {
            Log.i(TAG, "release: ");
        }
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        super.release();
    }

    /**
     * 计算帧率
     * @return
     */
    private int calcBitRate() {
        int bitrate = (int) (BPP * FRAME_RATE * mWidth * mHeight);
        if (enableHD) {
            bitrate *= HDValue;
        } else {
            bitrate *= 2;
        }
        Log.i(TAG, String.format("bitrate = % 5.2f[Mbps]", bitrate / 1024f / 1024f));
        return bitrate;
    }

    /**
     * 设置比特率
     * @param bitRate
     */
    public void setBitRate(int bitRate) {
        mBitRate = bitRate;
    }

    /**
     * 选择mimeType对应视频编码器
     * @param mimeType
     * @return
     */
    protected static final MediaCodecInfo selectVideoCodec(final String mimeType) {
        if (VERBOSE) {
            Log.d(TAG, "selectVideoCodec:");
        }
        // 获取可用的编码器
        final int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            final MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder()) {    // skipp decoder
                continue;
            }
            // 选择跟mimeType相同的编码器和对应的颜色格式
            final String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    if (VERBOSE) {
                        Log.i(TAG, "codec: " + codecInfo.getName() + ", MIME = " + types[j]);
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
     * 选择可用颜色
     * @param codecInfo
     * @param mimeType
     * @return
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
            if (isRecognizedViewoFormat(colorFormat)) {
                if (result == 0)
                    result = colorFormat;
                break;
            }
        }
        if (result == 0)
            Log.e(TAG, "couldn't find a good color format for " + codecInfo.getName() + " / " + mimeType);
        return result;
    }

    /**
     * 可用的颜色格式
     */
    protected static int[] recognizedFormats;

    static {
        recognizedFormats = new int[]{
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface,
        };
    }

    private static final boolean isRecognizedViewoFormat(final int colorFormat) {
        if (VERBOSE) {
            Log.i(TAG, "isRecognizedViewoFormat:colorFormat=" + colorFormat);
        }
        final int n = recognizedFormats != null ? recognizedFormats.length : 0;
        for (int i = 0; i < n; i++) {
            if (recognizedFormats[i] == colorFormat) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void sendEndOfPushing() {
        if (VERBOSE) {
            Log.d(TAG, "sending EOS to encoder");
        }
        try {
            mMediaCodec.signalEndOfInputStream();    // API >= 18
        } catch (Exception e) {}
        mIsEOS = true;
    }

    /**
     * 是否允许录制高清视频
     * @param enable
     */
    public void enableHighDefinition(boolean enable) {
        enableHD = enable;
    }
}
