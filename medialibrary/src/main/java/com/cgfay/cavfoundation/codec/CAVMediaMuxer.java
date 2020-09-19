package com.cgfay.cavfoundation.codec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 基于FFmpeg的媒体封装器
 */
public class CAVMediaMuxer {

    private static final String TAG = "CAVMediaMuxer";

    // 输出路径
    private String mOutputPath;
    // 音频参数
    private CAVAudioInfo mAudioInfo;
    // 视频参数
    private CAVVideoInfo mVideoInfo;

    // 封装器是否已经开始
    private volatile boolean mStarted;

    public CAVMediaMuxer() {
        mOutputPath = null;
        mAudioInfo = null;
        mVideoInfo = null;
        mStarted = false;
        native_setup();
    }

    /**
     * 设置封装输出的路径
     */
    public void setOutputPath(@NonNull String path) {
        mOutputPath = path;
        _setOutputPath(path);
    }

    /**
     * 设置音频参数
     */
    public void setAudioInfo(@Nullable CAVAudioInfo info) {
        mAudioInfo = info;
    }

    /**
     * 设置视频参数
     */
    public void setVideoInfo(@Nullable CAVVideoInfo info) {
        mVideoInfo = info;
    }

    public void prepare() throws IOException {
        if (TextUtils.isEmpty(mOutputPath)) {
            throw new IOException("Failed to prepare before set output path!");
        }
        if (mVideoInfo == null && mAudioInfo == null) {
            throw new IOException("Failed to prepare befor set audio of video infor");
        }
        if (mVideoInfo != null) {
            _setVideoParam(mVideoInfo.getWidth(), mVideoInfo.getHeight(),
                    mVideoInfo.getMimeType(), mVideoInfo.getFrameRate(), mVideoInfo.getBitRate(),
                    mVideoInfo.getProfile(), mVideoInfo.getLevel());
        }
        if (mAudioInfo != null) {
            _setAudioParam(mAudioInfo.getSampleRate(),
                    mAudioInfo.getChannelCount(), mAudioInfo.getBitRate(),
                    mAudioInfo.getAudioFormat());
        }
        _prepareMuxer();
    }

    public void start() {
        if (mStarted) {
            return;
        }
        mStarted = _start();
        Log.d(TAG, "media muxer is started ? " + mStarted);
    }

    public void stop() {
//        if (!mStarted) {
//            Log.d(TAG, "MediaMuxer is not started!");
//            return;
//        }
        _stop();
    }

    public void release() {
        _release();
    }

    /**
     * 写入额外参数
     * @param trackIndex    轨道索引
     * @param extraData     额外参数
     * @param bufferInfo    参数信息
     */
    public void writeExtraData(int trackIndex, @NonNull ByteBuffer extraData,
                               @NonNull MediaCodec.BufferInfo bufferInfo) {
        if (trackIndex >= 0) {
            // 将额外参数写入到muxer中
            byte[] data = new byte[bufferInfo.size];
            extraData.position(bufferInfo.offset);
            extraData.limit(bufferInfo.offset + bufferInfo.size);
            extraData.get(data, 0, bufferInfo.size);
            extraData.position(bufferInfo.offset);
            _writeExtraData(trackIndex, data, bufferInfo.size);
        }
    }

    /**
     * 根据类型获取轨道信息
     * @param format    媒体信息
     * @return
     */
    public int addTrack(@NonNull MediaFormat format) {
        if (format.containsKey(MediaFormat.KEY_MIME)) {
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (!TextUtils.isEmpty(mime)) {
                if (mime.contains("audio/")) {
                    return mAudioInfo.getTrack();
                } else if (mime.contains("video/")) {
                    return mVideoInfo.getTrack();
                }
            }
        }
        return -1;
    }

    /**
     * 将编码后的byte数字写入的复用器
     */
    public void writeFrame(int trackIndex, @NonNull ByteBuffer encodeBuffer,
                           @NonNull MediaCodec.BufferInfo bufferInfo) {
        if (mAudioInfo != null && mAudioInfo.getTrack() != CAVAudioInfo.INVALID_TRACK
                && trackIndex == mAudioInfo.getTrack()) {
            // 将编码后的音频数据添加ADTS并写入到Muxer中
            int size = bufferInfo.size + 7;
//            int size = bufferInfo.size;
            byte[] data = new byte[size];
            encodeBuffer.position(bufferInfo.offset);
            encodeBuffer.limit(bufferInfo.offset + bufferInfo.size);
            CAVAudioUtils.addADTSToPacket(data, size, mAudioInfo);
            encodeBuffer.get(data, 7, bufferInfo.size);
            encodeBuffer.position(bufferInfo.offset);
            _writeFrame(trackIndex, data, size, bufferInfo.presentationTimeUs, false);
        } else if (mVideoInfo != null && mVideoInfo.getTrack() != CAVVideoInfo.INVALID_TRACK
                && trackIndex == mVideoInfo.getTrack()) {
            // 将编码后的视频数据复制写入到Muxer中
            byte[] data = new byte[bufferInfo.size];
            encodeBuffer.position(bufferInfo.offset);
            encodeBuffer.limit(bufferInfo.offset + bufferInfo.size);
            encodeBuffer.get(data, 0, bufferInfo.size);
            encodeBuffer.position(bufferInfo.offset);
            Log.d(TAG, "writeFrame: " + bufferInfo.presentationTimeUs);
            _writeFrame(trackIndex, data, bufferInfo.size, bufferInfo.presentationTimeUs,
                    (bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0);
        }
    }

    /**
     * 判断是否已经开始
     */
    public boolean hasStarted() {
        return mStarted;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        native_finalize();
    }

    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("cavfoundation");
        native_init();
    }

    /**
     * access by native
     */
    private long mNativeContext;

    /**
     * init CAVMediaMuxer class
     */
    private static native void native_init();

    /**
     * setup CAVMediaMuxer
     */
    private native void native_setup();

    /**
     * finalize CAVMediaMuxer
     */
    private native void native_finalize();

    /**
     * set audio track, call by native
     * @param track track index
     */
    private void setAudioTrack(int track) {
        if (mAudioInfo != null) {
            mAudioInfo.setTrack(track);
        }
    }

    /**
     * set video track, call by native
     * @param track track index
     */
    private void setVideoTrack(int track) {
        if (mVideoInfo != null) {
            mVideoInfo.setTrack(track);
        }
    }

    /**
     * 设置输出路径
     */
    private native void _setOutputPath(String path);

    /**
     * 打开封装器
     */
    private native void _prepareMuxer() throws IOException;

    /**
     * 开始封装
     */
    private native boolean _start();

    /**
     * 停止封装的
     */
    private native void _stop();

    /**
     * 释放资源
     */
    private native void _release();

    /**
     * 设置视频参数
     * @param width         宽度
     * @param height        高度
     * @param mime          媒体类型
     * @param frameRate     帧率
     * @param bitRate       比特率
     * @param profile       profile
     * @param level         level
     * @return              是否创建成功，0表示创建成功，-1表示失败
     */
    private native void _setVideoParam(int width, int height, String mime, int frameRate,
                                      int bitRate, int profile, int level);

    /**
     * 设置音频参数
     * @param sampleRate        采样率
     * @param channelCount      声道数
     * @param bitRate           比特率
     * @param audioFormat       音频格式
     */
    private native void _setAudioParam(int sampleRate, int channelCount, int bitRate,
                                       int audioFormat);

    /**
     * 写入额外参数
     * @param trackIndex    轨道信息
     * @param extraData     额外参数
     * @param size          大小
     * @return              写入大小
     */
    private native int _writeExtraData(int trackIndex, byte[] extraData, int size);

    /**
     * 写入编码后的AAC/H264数据
     * @param trackIndex    轨道索引
     * @param encodeData    编码数据
     * @param size          编码数据大小
     * @param pts           时钟
     * @param keyFrame      是否关键帧
     * @return              编码结果，0为成功，-1表示失败
     */
    private native int _writeFrame(int trackIndex, byte[] encodeData, int size, long pts,
                                  boolean keyFrame);

}
