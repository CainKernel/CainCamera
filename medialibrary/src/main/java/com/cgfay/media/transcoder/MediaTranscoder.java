package com.cgfay.media.transcoder;

import androidx.annotation.NonNull;

/**
 * 媒体转码器
 */
public class MediaTranscoder {

    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("yuv");
        System.loadLibrary("cainfilter");
        System.loadLibrary("transcoder");
    }

    // 初始化
    private native long nativeInit();
    // 释放资源
    private native void nativeRelease(long handle);
    // 设置转码监听器
    private native void setTranscodeListener(long handle, Object listener);
    // 设置输出文件
    private native void setOutputPath(long handle, String dstPath);
    // 指定是否使用硬解码(默认使用硬解硬编)
    private native void setUseHardCodec(long handle, boolean hardCodec);
    // 设置视频旋转角度
    private native void setVideoRotate(long handle, int rotate);
    // 设置视频输出参数
    private native void setVideoParams(long handle, int width, int height, int frameRate,
                                       int pixelFormat, long maxBitrate, int quality);
    // 设置音频输出参数
    private native void setAudioParams(long handle, int sampleRate, int sampleFormat, int channelCount);
    // 开始转码
    private native void startTranscode(long handle);
    // 停止转码
    private native void stopTranscode(long handle);

    private long handle;
    private String dstPath;

    MediaTranscoder() {
        handle = nativeInit();
    }

    @Override
    protected void finalize() throws Throwable {
        release();
        super.finalize();
    }

    /**
     * 释放资源
     */
    public void release() {
        if (handle != 0) {
            nativeRelease(handle);
            handle = 0;
        }
    }

    /**
     * 设置转码监听器
     * @param listener
     */
    public void setTranscodeListener(OnTranscodeListener listener) {
        setTranscodeListener(handle, listener);
    }

    /**
     * 设置输出文件
     * @param dstPath
     */
    public void setOutput(String dstPath) {
        this.dstPath = dstPath;
        setOutputPath(handle, dstPath);
    }

    /**
     * 获取输出路径
     * @return
     */
    public String getOutput() {
        return dstPath;
    }

    /**
     * 设置是否使用硬解硬编
     */
    public void setUseHardCodec(boolean hardCodec) {
        setUseHardCodec(handle, hardCodec);
    }

    /**
     * 设置转码输出的旋转角度
     * @param rotate
     */
    public void setVideoRotate(int rotate) {
        setVideoRotate(handle, rotate);
    }

    /**
     * 设置视频输出参数
     * @param width
     * @param height
     * @param frameRate
     * @param pixelFormat
     * @param maxBitrate
     * @param quality
     */
    public void setVideoParams(int width, int height, int frameRate, int pixelFormat,
                               long maxBitrate, int quality) {
        setVideoParams(handle, width, height, frameRate, pixelFormat, maxBitrate, quality);
    }

    /**
     * 设置音频输出参数
     * @param sampleRate
     * @param sampleFormat
     * @param channels
     */
    public void setAudioParams(int sampleRate, int sampleFormat, int channels) {
        setAudioParams(handle, sampleRate, sampleFormat, channels);
    }

    /**
     * 开始转码
     */
    public void startTranscode() {
        startTranscode(handle);
    }

    /**
     * 停止转码
     */
    public void stopTranscode() {
        stopTranscode(handle);
    }


    /**
     * 转码参数
     */
    public class Builder {

        private String mDstPath;        // 文件输出路径

        // 视频参数
        private int mWidth;             // 视频宽度
        private int mHeight;            // 视频高度
        private int mRotate;            // 旋转角度
        private int mPixelFormat;       // 像素格式
        private int mFrameRate;         // 帧率，默认30fps
        private long mMaxBitRate;       // 最大比特率，当无法达到设置的quality是，quality会自动调整
        private int mQuality;           // 视频质量系数，默认为23, 推荐17~28之间，值越小，质量越高

        // 音频参数
        private int mSampleRate;        // 采样率
        private int mSampleFormat;      // 采样格式
        private int mChannels;          // 声道数

        private boolean mUseHardCodec;  // 是否使用硬解硬编

        public Builder(@NonNull String dstPath) {
            mDstPath = dstPath;

            mWidth = -1;
            mHeight = -1;
            mRotate = 0;
            mPixelFormat = -1;
            mFrameRate = -1;
            mMaxBitRate = -1;
            mQuality = 23;

            mSampleRate = -1;
            mSampleFormat = -1;
            mChannels = -1;

            mUseHardCodec = true;
        }

        /**
         * 设置视频转码输出参数
         * @param width
         * @param height
         * @param pixelFormat
         * @param frameRate
         * @return
         */
        public Builder setVideoParams(int width, int height, int pixelFormat, int frameRate) {
            mWidth = width;
            mHeight = height;
            mPixelFormat = pixelFormat;
            mFrameRate = frameRate;
            return this;
        }

        /**
         * 设置音频转码输出参数
         * @param sampleRate
         * @param sampleFormat
         * @param channels
         * @return
         */
        public Builder setAudioParams(int sampleRate, int sampleFormat, int channels) {
            mSampleRate = sampleRate;
            mSampleFormat = sampleFormat;
            mChannels = channels;
            return this;
        }

        /**
         * 设置旋转角度
         * @param rotate
         * @return
         */
        public Builder setRotate(int rotate) {
            mRotate = rotate;
            return this;
        }

        /**
         * 设置最大比特率
         * @param maxBitRate
         * @return
         */
        public Builder setMaxBitRate(long maxBitRate) {
            mMaxBitRate = maxBitRate;
            return this;
        }

        /**
         * 设置质量系数，只有在软编码才有效
         * @param quality
         * @return
         */
        public Builder setQuality(int quality) {
            mQuality = quality;
            return this;
        }

        /**
         * 创建媒体转码器
         */
        public MediaTranscoder create() {
            MediaTranscoder transcoder = new MediaTranscoder();
            transcoder.setOutput(mDstPath);
            transcoder.setVideoParams(mWidth, mHeight, mFrameRate, mPixelFormat, mMaxBitRate, mQuality);
            transcoder.setVideoRotate(mRotate);
            transcoder.setAudioParams(mSampleRate, mSampleFormat, mChannels);
            transcoder.setUseHardCodec(mUseHardCodec);
            return transcoder;
        }
    }
}
