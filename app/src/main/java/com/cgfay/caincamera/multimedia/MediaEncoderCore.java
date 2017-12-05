package com.cgfay.caincamera.multimedia;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
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

public class MediaEncoderCore {

    private static final String TAG = "MediaEncoder";

    private static final boolean VERBOSE = false;

    // 录音源
    private static final int[] AUDIO_SOURCES = new int[] {
            MediaRecorder.AudioSource.MIC,
            MediaRecorder.AudioSource.DEFAULT,
            MediaRecorder.AudioSource.CAMCORDER,
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
    };

    // 超时
    private static final int TIMEOUT_USEC = 10000;

    // VideoCodec参数
    private static final String VIDEO_TYPE = "video/avc";
    private static final int IFRAME_INTERVAL = 240;
    private static final int FRAME_RATE = 25;
    private static final float BPP = 0.25f;

    // AudioCodec参数
    private static final String AUDIO_TYPE = "audio/mp4a-latm";
    private static final int SAMPLE_RATE = 44100;
    private static final int BIT_RATE = 64000;
    public static final int SAMPLES_PER_FRAME = 1024;	// AAC, bytes/frame/channel
    public static final int FRAMES_PER_BUFFER = 25; 	// AAC, frame/buffer/sec

    // 录制渲染的Surface
    private Surface mInputSurface;

    // 录制视频
    private MediaCodec mVideoEncoder;
    private MediaCodec.BufferInfo mVideoBufferInfo;

    // 录制音频
    private MediaCodec mAudioEncoder;
    private MediaCodec.BufferInfo mAudioBufferInfo;

    //  AudioRecord录音器
    private AudioRecord mAudioRecord;
    // 录音缓冲大小
    private int mAudioBufferSize;

    // 复用器
    private MediaMuxer mMuxer;

    // 复用器开始标志
    private boolean mMuxerStarted;

    private int mVideoTrackIndex;
    private int mAudioTrackIndex;

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
     * @param width
     * @param height
     * @param bitRate
     * @param enableHD
     */
    public MediaEncoderCore(int width, int height, int bitRate, boolean enableHD) {
        mWidth = width;
        mHeight = height;
        mBitRate = bitRate;
        isEnableHD = enableHD;
    }

    /**
     * 构造器
     * @param width     录制视频宽度
     * @param height    录制视频高度
     * @param bitRate   比特率
     */
    public MediaEncoderCore(int width, int height, int bitRate)  {
        this(width, height, bitRate, false);
    }

    /**
     * 构造器
     * @param width
     * @param height
     * @param enableHD 是否允许高清录制
     */
    public MediaEncoderCore(int width, int height, boolean enableHD)  {
        this(width, height, 0, enableHD);
    }

    /**
     * 构造器
     * @param width
     * @param height
     */
    public MediaEncoderCore(int width, int height) {
        this(width, height, 0, false);
    }

    /**
     * 准备编码器
     * @param path 视频路径
     * @throws IOException
     */
    public void prepare(String path) throws IOException {

        mVideoBufferInfo = new MediaCodec.BufferInfo();

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

        mInputSurface = mVideoEncoder.createInputSurface();
        mVideoEncoder.start();

        if (VERBOSE) {
            Log.i(TAG, "prepare finishing");
        }

        // 初始化录制音频所需要的MediaCodec
        mAudioBufferInfo = new MediaCodec.BufferInfo();
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

        // 复位trackIndex
        mVideoTrackIndex = -1;
        mAudioTrackIndex = -1;

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
        return mInputSurface;
    }

    /**
     * 释放资源
     */
    public void release() {
        if (VERBOSE) {
            Log.d(TAG, "releasing encoder objects");
        }

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

        // 释放录音器
        if (mAudioRecord != null) {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }

        // 释放复用器
        if (mMuxer != null) {
            mMuxer.release();
            mMuxer = null;
        }

        // 释放surface
        if (mInputSurface != null) {
            mInputSurface.release();
            mInputSurface = null;
        }
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

        // 得到解码后的数据
        ByteBuffer[] encoderOutputBuffers = mVideoEncoder.getOutputBuffers();
        // 将数据写入复用器，完成视频编码
        while (true) {
            int encoderStatus = mVideoEncoder.dequeueOutputBuffer(mVideoBufferInfo, TIMEOUT_USEC);
            // 等待
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) {
                    break;
                } else {
                    if (VERBOSE) {
                        Log.d(TAG, "no output available, spinning to await EOS");
                    }
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) { // 重新获得解码数据
                encoderOutputBuffers = mVideoEncoder.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (mMuxerStarted) {
                    throw new RuntimeException("format changed twice");
                }
                MediaFormat format = mVideoEncoder.getOutputFormat();
                Log.d(TAG, "encoder output format changed: " + format);
                // 复用器开始
                mVideoTrackIndex = mMuxer.addTrack(format);
                mMuxer.start();
                mMuxerStarted = true;

            } else if (encoderStatus < 0) {
                Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                encoderStatus);
            } else {
                // 获取编码后的缓冲数据
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer "
                            + encoderStatus + " was null");
                }

                if ((mVideoBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    if (VERBOSE) {
                        Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                    }
                    mVideoBufferInfo.size = 0;
                }

                if (mVideoBufferInfo.size != 0) {
                    if (!mMuxerStarted) {
                        throw new RuntimeException("muxer hasn't started");
                    }

                    encodedData.position(mVideoBufferInfo.offset);
                    encodedData.limit(mVideoBufferInfo.offset + mVideoBufferInfo.size);

                    // 写入数据
                    mMuxer.writeSampleData(mVideoTrackIndex, encodedData, mVideoBufferInfo);
                    if (VERBOSE) {
                        Log.d(TAG, "sent " + mVideoBufferInfo.size
                                + " bytes to muxer, ts = " + mVideoBufferInfo.presentationTimeUs);
                    }
                }

                // 释放缓冲
                mVideoEncoder.releaseOutputBuffer(encoderStatus, false);

                if ((mVideoBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        Log.w(TAG, "reached end of stream unexpectdedly");
                    } else {
                        if (VERBOSE) {
                            Log.d(TAG, "end of stream reached");
                        }
                    }
                    break;
                }
            }
        }
    }

    /**
     * 初始化录音器
     */
    private void initAudioRecord() {
        final int min_buffer_size = AudioRecord.getMinBufferSize(
                SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        mAudioBufferSize = SAMPLES_PER_FRAME * FRAMES_PER_BUFFER;
        if (mAudioBufferSize < min_buffer_size)
            mAudioBufferSize = ((min_buffer_size / SAMPLES_PER_FRAME) + 1) * SAMPLES_PER_FRAME * 2;

        AudioRecord audioRecord = null;
        for (final int source : AUDIO_SOURCES) {
            try {
                audioRecord = new AudioRecord(
                        source, SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, mAudioBufferSize);
                if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED)
                    audioRecord = null;
            } catch (final Exception e) {
                audioRecord = null;
            }
            if (audioRecord != null) break;
        }
        mAudioRecord = audioRecord;
        if (mAudioRecord != null) {
            mAudioRecord.startRecording();
        }
    }

    /**
     * 从音频编码器中提取所有待处理的数据并转发给复用器
     * @param endOfStream
     */
    private void drainAudioEncoder(boolean endOfStream) {

        if (VERBOSE) {
            Log.d(TAG, "drainVideoEncoder(" + endOfStream + ")");
        }
        if (endOfStream) {
            if (VERBOSE) {
                Log.d(TAG, "sending EOS to encoder");
            }
            mAudioEncoder.signalEndOfInputStream();
        }

        // 从AudioRecord中获取录音的字节数据
        byte[] buffer = new byte[mAudioBufferSize];
        int bufferReadResult = mAudioRecord.read(buffer, 0, mAudioBufferSize);

        if(bufferReadResult == AudioRecord.ERROR_BAD_VALUE
                || bufferReadResult == AudioRecord.ERROR_INVALID_OPERATION) {
            Log.e(TAG, "Read error");
        }

        // 填充需要编码的录音数据(数据从AudioRecord中取得)
        ByteBuffer[] mAudioInputBuffers = mAudioEncoder.getInputBuffers();
        int index = mAudioEncoder.dequeueInputBuffer(TIMEOUT_USEC);
        if (index >= 0 && mAudioRecord != null) {
            ByteBuffer inputBuffer = mAudioInputBuffers[index];
            inputBuffer.clear();
            inputBuffer.put(buffer);

            // 字节大小
            int size = inputBuffer.limit();
            // 将Buffer传递给解码器
            mAudioEncoder.queueInputBuffer(index, 0 /* offset */,
                    size, mAudioBufferInfo.presentationTimeUs /* timeUs */, 0);
        }

        // 得到解码后的数据缓冲
        ByteBuffer[] encoderOutputBuffers = mAudioEncoder.getOutputBuffers();

        // 将数据写入复用器，完成音频编码
        while (true) {
            int encoderStatus = mAudioEncoder.dequeueOutputBuffer(mAudioBufferInfo, TIMEOUT_USEC);
            // 等待
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) {
                    break;
                } else {
                    if (VERBOSE) {
                        Log.d(TAG, "no output available, spinning to await EOS");
                    }
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) { // 重新获得解码数据
                encoderOutputBuffers = mAudioEncoder.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (mMuxerStarted) {
                    throw new RuntimeException("format changed twice");
                }
                MediaFormat format = mAudioEncoder.getOutputFormat();
                Log.d(TAG, "encoder output format changed: " + format);
                // 复用器开始
                mAudioTrackIndex = mMuxer.addTrack(format);
                mMuxer.start();
                mMuxerStarted = true;

            } else if (encoderStatus < 0) {
                Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                        encoderStatus);
            } else {
                // 获取编码输出PCM数据的缓冲
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer "
                            + encoderStatus + " was null");
                }

                if ((mAudioBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    if (VERBOSE) {
                        Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                    }
                    mAudioBufferInfo.size = 0;
                }

                if (mAudioBufferInfo.size != 0) {
                    if (!mMuxerStarted) {
                        throw new RuntimeException("muxer hasn't started");
                    }

                    // 编码数据的偏移和大小限制
                    encodedData.position(mAudioBufferInfo.offset);
                    encodedData.limit(mAudioBufferInfo.offset + mAudioBufferInfo.size);
                    // 跟Video进行同步时间戳
                    mAudioBufferInfo.presentationTimeUs = mVideoBufferInfo.presentationTimeUs;
                    // 写入数据
                    mMuxer.writeSampleData(mAudioTrackIndex, encodedData, mAudioBufferInfo);
                    if (VERBOSE) {
                        Log.d(TAG, "sent " + mAudioBufferInfo.size
                                + " bytes to muxer, ts = " + mAudioBufferInfo.presentationTimeUs);
                    }
                }
                // 释放Buffer
                mAudioEncoder.releaseOutputBuffer(encoderStatus, false);
                if ((mAudioBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        Log.w(TAG, "reached end of stream unexpectdedly");
                    } else {
                        if (VERBOSE) {
                            Log.d(TAG, "end of stream reached");
                        }
                    }
                    break;
                }
            }
        }
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
