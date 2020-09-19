package com.cgfay.cavfoundation.codec;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.io.IOException;

/**
 * 视频编码器
 */
public class CAVVideoEncoder extends CAVMediaEncoder {

    private MediaCodec mMediaCodec;
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private Surface mInputSurface;

    // 视频参数
    private CAVVideoInfo mVideoInfo;

    private long mStartTimeStamp;
    private long mLastTimeStamp;

    public CAVVideoEncoder(@NonNull CAVVideoInfo info) {
        mVideoInfo = info;
    }

    /**
     * 准备准备编码器
     * @throws Exception
     */
    public void prepare() throws IOException {
        if (VERBOSE) {
            Log.d(TAG, "prepare: ");
        }
        int videoWidth = (mVideoInfo.getWidth() % 2 == 0) ? mVideoInfo.getWidth()
                : mVideoInfo.getWidth() - 1;
        int videoHeight = (mVideoInfo.getHeight() % 2 == 0) ? mVideoInfo.getHeight()
                : mVideoInfo.getHeight() - 1;

        MediaFormat format = MediaFormat.createVideoFormat(mVideoInfo.getMimeType(),
                videoWidth, videoHeight);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, mVideoInfo.getBitRate());
        format.setInteger(MediaFormat.KEY_FRAME_RATE, mVideoInfo.getFrameRate());
        int frameInterval = mVideoInfo.getFrameRate() / mVideoInfo.getGopSize();
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, frameInterval);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            format.setInteger(MediaFormat.KEY_PROFILE, mVideoInfo.getProfile());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                format.setInteger(MediaFormat.KEY_LEVEL, mVideoInfo.getLevel());
            }
        }
        mMediaCodec = MediaCodec.createEncoderByType(mVideoInfo.getMimeType());
        mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mInputSurface = mMediaCodec.createInputSurface();
        mStartTimeStamp = 0;
        mLastTimeStamp = 0;
        mIsEOS = false;
    }

    @Override
    public void signalEndOfInputStream() {
        if (VERBOSE) {
            Log.d(TAG, "signalEndOfInputStream: ");
        }
        if (mMediaCodec != null) {
            mMediaCodec.signalEndOfInputStream();
        }
        mIsEOS = true;
    }

    @Override
    protected int getTrackIndex() {
        return mVideoInfo.getTrack();
    }

    /**
     * 计算pts
     */
    @Override
    protected void calculateTimeUs(MediaCodec.BufferInfo info) {
        // 计算录制时钟，如果当前pts小于上一个pts，则加上一个frame rate 的时长间隔
        if (mLastTimeStamp > 0 && mBufferInfo.presentationTimeUs < mLastTimeStamp) {
            mBufferInfo.presentationTimeUs = mLastTimeStamp + (int)(1000f / mVideoInfo.getFrameRate()) * 1000;
        }
        mLastTimeStamp = info.presentationTimeUs;
        if (mStartTimeStamp == 0) {
            mStartTimeStamp = info.presentationTimeUs;
        }
    }

    /**
     * 获取编码输入Surface
     */
    public Surface getInputSurface() {
        return mInputSurface;
    }

}
