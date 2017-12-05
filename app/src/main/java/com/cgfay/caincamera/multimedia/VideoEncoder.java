package com.cgfay.caincamera.multimedia;

import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 视频录制管理器
 * Created by cain.huang on 2017/12/5.
 */

public class VideoEncoder {

    private static final String TAG = "MediaEncoder";

    private static final boolean VERBOSE = false;

    // 超时
    private static final int TIMEOUT_USEC = 10000;

    private static final String VIDEO_TYPE = "video/avc";
    private static final int IFRAME_INTERVAL = 240;
    private static final int FRAME_RATE = 25;
    private static final float BPP = 0.25f;


    private static final String AUDIO_TYPE = "audio/mp4a-latm";
    private static final int SAMPLE_RATE = 44100;
    private static final int BIT_RATE = 64000;
    public static final int SAMPLES_PER_FRAME = 1024;	// AAC, bytes/frame/channel
    public static final int FRAMES_PER_BUFFER = 25; 	// AAC, frame/buffer/sec

    private Surface mSurface;

    // 录制视频
    private MediaCodec mVideoEncoder;

    // 录制音频
    private MediaCodec mAudioEncoder;

    // 复用器
    private MediaMuxer mMuxer;

    // 复用器开始标志
    private boolean mMuxerStarted;

    // 录制线程
    private HandlerThread mEncoderThread;
    private Handler mHandler;


    // 录制视频的宽高
    private int mWidth;
    private int mHeight;

    // 比特率
    private int mBitRate = 0;
    // 高清录制时的帧率倍数
    private static final int HDValue = 4;

    // 是否允许高清
    private boolean isEnableHD = false;


    /**
     * 构造器
     * @param width     录制视频宽度
     * @param height    录制视频高度
     * @param bitRate   比特率
     */
    public VideoEncoder(int width, int height, int bitRate)  {
        mWidth = width;
        mHeight = height;
        mBitRate = bitRate;
    }

    /**
     * 构造器
     * @param width
     * @param height
     */
    public VideoEncoder(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    /**
     * 准备编码器
     * @param path 视频路径
     * @throws IOException
     */
    public void prepare(String path) throws IOException {
        // 初始化录制视频所需要的MediaCodec
        final MediaCodecInfo videoCodecInfo = selectVideoCodec(VIDEO_TYPE);
        if (videoCodecInfo == null) {
            Log.e(TAG, "Unable to find an appropriate codec for " + VIDEO_TYPE);
            return;
        }

        if (VERBOSE) {
            Log.i(TAG, "selected codec: " + videoCodecInfo.getName());
        }

        int videoWidth = mWidth % 2 == 0 ? mWidth : mWidth - 1;
        int videoHeight = mHeight % 2 == 0 ? mHeight : mHeight - 1;

        // 获取格式
        final MediaFormat format = MediaFormat.createVideoFormat(VIDEO_TYPE, videoWidth, videoHeight);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        if (mBitRate > 0) {
            format.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate);
        } else {
            format.setInteger(MediaFormat.KEY_BIT_RATE, calcBitRate());
        }
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
        if (VERBOSE) {
            Log.i(TAG, "format: " + format);
        }

        mVideoEncoder = MediaCodec.createEncoderByType(VIDEO_TYPE);
        mVideoEncoder.configure(format, null, null,
                MediaCodec.CONFIGURE_FLAG_ENCODE);

        mSurface = mVideoEncoder.createInputSurface();
        mVideoEncoder.start();

        if (VERBOSE) {
            Log.i(TAG, "prepare finishing");
        }

        // 初始化录制音频所需要的MediaCodec
        final MediaCodecInfo audioCodecInfo = selectAudioCodec(AUDIO_TYPE);
        if (audioCodecInfo == null) {
            Log.e(TAG, "Unable to find an appropriate codec for " + AUDIO_TYPE);
            return;
        }
        if (VERBOSE) {
            Log.i(TAG, "selected codec: " + audioCodecInfo.getName());
        }

        final MediaFormat audioFormat =
                MediaFormat.createAudioFormat(AUDIO_TYPE, SAMPLE_RATE, 1);
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO);
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
        if (VERBOSE) {
            Log.i(TAG, "format: " + audioFormat);
        }
        mAudioEncoder = MediaCodec.createEncoderByType(AUDIO_TYPE);
        mAudioEncoder.configure(audioFormat, null, null,
                MediaCodec.CONFIGURE_FLAG_ENCODE);
        mAudioEncoder.start();
        if (VERBOSE) {
            Log.i(TAG, "prepare finishing");
        }

        // 初始化复用器
        mMuxer = new MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        mMuxerStarted = false;

        // 启动录制线程
        mEncoderThread = new HandlerThread("Recorder Thread");
        mEncoderThread.start();
        mHandler = new Handler(mEncoderThread.getLooper());

    }

    /**
     * 计算帧率
     * @return
     */
    private int calcBitRate() {
        int bitrate = (int) (BPP * FRAME_RATE * mWidth * mHeight);
        if (isEnableHD) {
            bitrate *= HDValue;
        } else {
            bitrate *= 2;
        }
        Log.i(TAG, String.format("bitrate = % 5.2f[Mbps]", bitrate / 1024f / 1024f));
        return bitrate;
    }

    /**
     * 获取Surface
     * @return
     */
    public Surface getInputSurface() {
        return mSurface;
    }

    /**
     * 释放资源
     */
    public void release() {
        if (VERBOSE) {
            Log.d(TAG, "releasing encoder objects");
        }
        // 停止线程
        mHandler.removeCallbacksAndMessages(null);
        mEncoderThread.quit();
        try {
            mEncoderThread.join();
        } catch (InterruptedException e) {
            Log.w(TAG, "Encoder Thread join() was interrupted", e);
        }
        mHandler = null;
        mEncoderThread = null;

        // 释放视频的MediaCodec
        if (mVideoEncoder != null) {
            mVideoEncoder.stop();
            mVideoEncoder.release();
            mVideoEncoder = null;
        }

        // 释放音频的MediaCodec
        if (mAudioEncoder != null) {
            mAudioEncoder.stop();
            mAudioEncoder.release();
            mAudioEncoder = null;
        }

        // 释放复用器
        if (mMuxer != null) {
            mMuxer.release();
            mMuxer = null;
        }

        // 释放surface
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
    }

    /**
     * 帧可用
     */
    public void frameAvailableSoon() {

    }

    /**
     * 从视频编码器中提取所有待处理的数据并转发给复用器
     * @param endOfStream
     */
    public void drainVideoEncoder(boolean endOfStream) {
        if (VERBOSE) {
            Log.d(TAG, "drainVideoEncoder(" + endOfStream + ")");
        }
        if (endOfStream) {
            if (VERBOSE) {
                Log.d(TAG, "sending EOS to encoder");
            }
            mVideoEncoder.signalEndOfInputStream();
        }
    }


    /**
     * 从音频编码器中提取所有待处理的数据并转发给复用器
     * @param endOfStream
     */
    private void drainAudioEncoder(boolean endOfStream) {

    }

    /**
     * 设置是否允许高清拍照
     * @param enable
     */
    public void setEnableHD(boolean enable) {
        isEnableHD = enable;
    }


    /**
     * 获取Audio的信息
     * @param mimeType
     * @return
     */
    private static MediaCodecInfo selectAudioCodec(final String mimeType) {
        if (VERBOSE) {
            Log.v(TAG, "selectAudioCodec:");
        }

        MediaCodecInfo result = null;
        // 获取可用的列表
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
                    if (result == null) {
                        result = codecInfo;
                        break LOOP;
                    }
                }
            }
        }
        return result;
    }

    /**
     * 选择Video的信息
     *
     * @param mimeType
     * @return null if no codec matched
     */
    private static MediaCodecInfo selectVideoCodec(final String mimeType) {
        if (VERBOSE) {
            Log.v(TAG, "selectVideoCodec:");
        }

        // get the list of available codecs
        final int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            final MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder()) {    // skipp decoder
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
     * 选择可用的颜色格式
     *
     * @return 0 if no colorFormat is matched
     */
    private static int selectColorFormat(final MediaCodecInfo codecInfo, final String mimeType) {
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
    private static int[] recognizedFormats;

    static {
        recognizedFormats = new int[]{
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface,
        };
    }

    private static boolean isRecognizedViewoFormat(final int colorFormat) {
        if (VERBOSE) {
            Log.i(TAG, "isRecognizedViewoFormat: colorFormat = " + colorFormat);
        }
        final int n = recognizedFormats != null ? recognizedFormats.length : 0;
        for (int i = 0; i < n; i++) {
            if (recognizedFormats[i] == colorFormat) {
                return true;
            }
        }
        return false;
    }

}
