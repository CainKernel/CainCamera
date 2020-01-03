package com.cgfay.filter.recorder;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 视频编码器
 * @author CainHuang
 * @date 2019/6/30
 */
final class VideoEncoder {

    private static final String TAG = "VideoEncoder";
    private static final boolean VERBOSE = true;

    private Surface mInputSurface;
    private MediaMuxer mMediaMuxer;
    private MediaCodec mMediaCodec;
    private MediaCodec.BufferInfo mBufferInfo;
    private int mTrackIndex;
    private boolean mMuxerStarted;
    private VideoParams mVideoParams;
    private OnEncodingListener mRecordingListener;
    // 录制起始时间戳
    private long mStartTimeStamp;
    // 记录上一个时间戳
    private long mLastTimeStamp;
    // 录制时长
    private long mDuration;

    /**
     * 配置编码器和复用器等参数
     */
    public VideoEncoder(@NonNull VideoParams params, OnEncodingListener listener) throws IOException {
        mVideoParams = params;
        mRecordingListener = listener;

        mBufferInfo = new MediaCodec.BufferInfo();

        // 设置编码格式
        int videoWidth = (mVideoParams.getVideoWidth() % 2 == 0) ? mVideoParams.getVideoWidth()
                : mVideoParams.getVideoWidth() - 1;
        int videoHeight = (mVideoParams.getVideoHeight() % 2 == 0) ? mVideoParams.getVideoHeight()
                : mVideoParams.getVideoHeight() - 1;
        MediaFormat format = MediaFormat.createVideoFormat(VideoParams.MIME_TYPE, videoWidth, videoHeight);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, params.getBitRate());
        format.setInteger(MediaFormat.KEY_FRAME_RATE, VideoParams.FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, VideoParams.I_FRAME_INTERVAL);
        if (Build.VERSION.SDK_INT >= 21) {
            int profile = 0;
            int level = 0;
            if (VideoParams.MIME_TYPE.equals("video/avc")) {
                profile = MediaCodecInfo.CodecProfileLevel.AVCProfileHigh;
                if (videoWidth * videoHeight >= 1920 * 1080) {
                    level = MediaCodecInfo.CodecProfileLevel.AVCLevel4;
                } else {
                    level = MediaCodecInfo.CodecProfileLevel.AVCLevel31;
                }
            } else if (VideoParams.MIME_TYPE.equals("video/hevc")) {
                profile = MediaCodecInfo.CodecProfileLevel.HEVCProfileMain;
                if (videoWidth * videoHeight >= 1920 * 1080) {
                    level = MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel4;
                } else {
                    level = MediaCodecInfo.CodecProfileLevel.HEVCHighTierLevel31;
                }
            }
            format.setInteger(MediaFormat.KEY_PROFILE, profile);
            // API 23以后可以设置AVC的编码level，低于23设置了但不生效
//            if (Build.VERSION.SDK_INT >= 23) {
                format.setInteger(MediaFormat.KEY_LEVEL, level);
//            }
        }
        if (VERBOSE) {
            Log.d(TAG, "format: " + format);
        }
        // 创建编码器
        mMediaCodec = MediaCodec.createEncoderByType(VideoParams.MIME_TYPE);
        mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mInputSurface = mMediaCodec.createInputSurface();
        mMediaCodec.start();

        // 创建复用器
        mMediaMuxer = new MediaMuxer(params.getVideoPath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        mTrackIndex = -1;
        mMuxerStarted = false;
    }

    /**
     * 返回编码器的Surface
     */
    public Surface getInputSurface() {
        return mInputSurface;
    }

    /**
     * 释放编码器资源
     */
    public void release() {
        if (VERBOSE) {
            Log.d(TAG, "releasing encoder objects");
        }
        if (mMediaCodec != null) {
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
        }
        if (mMediaMuxer != null) {
            if (mMuxerStarted) {
                mMediaMuxer.stop();
            }
            mMediaMuxer.release();
            mMediaMuxer = null;
        }
    }

    /**
     * 编码一帧数据到复用器中
     * @param endOfStream
     */
    public void drainEncoder(boolean endOfStream) {
        final int TIMEOUT_USEC = 10000;
        if (VERBOSE) {
            Log.d(TAG, "drainEncoder(" + endOfStream + ")");
        }

        if (endOfStream) {
            if (VERBOSE) Log.d(TAG, "sending EOS to encoder");
            mMediaCodec.signalEndOfInputStream();
        }

        ByteBuffer[] encoderOutputBuffers = mMediaCodec.getOutputBuffers();
        while (true) {
            int encoderStatus = mMediaCodec.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) {
                    break;      // out of while
                } else {
                    if (VERBOSE) Log.d(TAG, "no output available, spinning to await EOS");
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                encoderOutputBuffers = mMediaCodec.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (mMuxerStarted) {
                    throw new RuntimeException("format changed twice");
                }
                MediaFormat newFormat = mMediaCodec.getOutputFormat();
                if (VERBOSE) {
                    Log.d(TAG, "encoder output format changed: " + newFormat.getString(MediaFormat.KEY_MIME));
                }
                // 提取视频轨道并打开复用器
                mTrackIndex = mMediaMuxer.addTrack(newFormat);
                mMediaMuxer.start();
                mMuxerStarted = true;
            } else if (encoderStatus < 0) {
                Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                        encoderStatus);
            } else {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                            " was null");
                }

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    if (VERBOSE) {
                        Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                    }
                    mBufferInfo.size = 0;
                }

                if (mBufferInfo.size != 0) {
                    if (!mMuxerStarted) {
                        throw new RuntimeException("muxer hasn't started");
                    }

                    // 计算录制时钟
                    if (mLastTimeStamp > 0 && mBufferInfo.presentationTimeUs < mLastTimeStamp) {
                        mBufferInfo.presentationTimeUs = mLastTimeStamp + 10 * 1000;
                    }
                    calculateTimeUs(mBufferInfo);
                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    encodedData.position(mBufferInfo.offset);
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
                    // 将编码数据写入复用器中
                    mMediaMuxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo);
                    if (VERBOSE) {
                        Log.d(TAG, "sent " + mBufferInfo.size + " bytes to muxer, ts=" +
                                mBufferInfo.presentationTimeUs);
                    }

                    // 录制时长回调
                    if (mRecordingListener != null) {
                        mRecordingListener.onEncoding(mDuration);
                    }

                }

                mMediaCodec.releaseOutputBuffer(encoderStatus, false);

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        Log.w(TAG, "reached end of stream unexpectedly");
                    } else {
                        if (VERBOSE) {
                            Log.d(TAG, "end of stream reached");
                        }
                    }
                    break;      // out of while
                }
            }
        }
    }

    /**
     * 计算pts
     * @param info
     */
    private void calculateTimeUs(MediaCodec.BufferInfo info) {
        mLastTimeStamp = info.presentationTimeUs;
        if (mStartTimeStamp == 0) {
            mStartTimeStamp = info.presentationTimeUs;
        } else {
            mDuration = info.presentationTimeUs - mStartTimeStamp;
        }
    }

    /**
     * 获取编码的时长
     * @return
     */
    public long getDuration() {
        return mDuration;
    }


    /**
     * 获取视频参数
     * @return
     */
    public VideoParams getVideoParams() {
        return mVideoParams;
    }

    /**
     * 编码监听器
     */
    public interface OnEncodingListener {

        void onEncoding(long duration);
    }

}