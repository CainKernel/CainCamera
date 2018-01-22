package com.cgfay.pushlibrary;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * 推流复用器
 * Created by cain on 2018/1/22.
 */

public class MediaRtmpMuxer {

    private static final boolean VERBOSE = false;
    private static final String TAG = "MediaRtmpMuxer";

    private RtmpMuxer mRtmpMuxer;
    private int mPusherCount, mStartedCount;
    private boolean mIsStarted;
    private MediaPusher mVideoPusher, mAudioPusher;

    public MediaRtmpMuxer(String url) throws IOException {
        try {
            mRtmpMuxer = new RtmpMuxer(url, RtmpMuxer.OutputFormat.MUXER_OUTPUT_RTMP);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mPusherCount = mStartedCount = 0;
        mIsStarted = false;
    }

    /**
     * 准备推流器
     */
    public void prepared() {
        if (mVideoPusher != null) {

        }
    }

    /**
     * 开始推流
     */
    public void startPushing() {

    }

    /**
     * 停止推流
     */
    public void stopPushing() {

    }

    /**
     * 是否已经开始
     * @return
     */
    public synchronized boolean isStarted() {
        return mIsStarted;
    }


    /**
     * 获取视频推流器
     * @return
     */
    public MediaPusher getVideoPusher() {
        return mVideoPusher;
    }

    /**
     * 获取音频推流器
     * @return
     */
    public MediaPusher getAudioPusher() {
        return mAudioPusher;
    }

    /**
     * 添加推流器
     * @param pusher
     */
    void addPusher(final MediaPusher pusher) {
        if (pusher instanceof MediaVideoPusher) {
            if (mVideoPusher != null) {
                throw new IllegalArgumentException("Video pusher already added.");
            }
            mVideoPusher = pusher;
        } else if (pusher instanceof MediaAudioPusher) {
            if (mAudioPusher != null) {
                throw new IllegalArgumentException("Audio pusher already added.");
            }
            mAudioPusher = pusher;
        } else {
            throw new IllegalArgumentException("unsupported pusher");
        }
        mPusherCount = (mVideoPusher != null ? 1 : 0) + (mAudioPusher != null ? 1 : 0);
    }

    /**
     * 开始推流
     * @return
     */
    synchronized boolean start() {
        if (VERBOSE) {
            Log.d(TAG, "muxer start!");
        }
        mStartedCount++;
        if ((mPusherCount > 0) && (mStartedCount == mPusherCount)) {
            mRtmpMuxer.start();
            mIsStarted = true;
            notifyAll();
            if (VERBOSE) {
                Log.d(TAG, "muxer started!");
            }
        }
        return mIsStarted;
    }

    /**
     * 停止推流
     */
    synchronized void stop() {
        if (VERBOSE) {
            Log.d(TAG, "stop: mStartedCount = " + mStartedCount);
        }
        mStartedCount--;
        if ((mPusherCount > 0) && (mStartedCount <= 0)) {
            mRtmpMuxer.stop();
            mRtmpMuxer.release();
            mIsStarted = false;
            if (VERBOSE) {
                Log.d(TAG, "muser stopped!");
            }
        }
    }

    /**
     * 添加推流轨道
     * @param format
     * @return
     */
    synchronized int addTrack(final MediaFormat format) {
        if (mIsStarted) {
            throw new IllegalArgumentException("muxer already started!");
        }
        final int trackIndex = mRtmpMuxer.addTrack(format);
        if (VERBOSE) {
            Log.i(TAG, "addTrack: trackNum =" + mPusherCount
                    + ", trackIndex = " + trackIndex + ", type = " + format);
        }
        return trackIndex;
    }

    /**
     * 写入采样数据(已经过硬编码后的数据)
     * @param trackIndex
     * @param byteBuf
     * @param bufferInfo
     */
    synchronized void writeSampleData(final int trackIndex, final ByteBuffer byteBuf,
                                      final MediaCodec.BufferInfo bufferInfo) {
        if (mStartedCount > 0) {
            mRtmpMuxer.writeSampleData(trackIndex, byteBuf, bufferInfo);
        }
    }
}
