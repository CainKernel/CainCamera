package com.cgfay.media;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

/**
 * 视频播放器
 */
public class VideoPlayer {

    private static final String TAG = "VideoPlayer";

    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("yuv");
        System.loadLibrary("videoplayer");
    }

    // 初始化
    private native long nativeInit();
    // 释放资源
    private native void nativeRelease(long handle);
    // 播放监听器
    private native void setOnPlayListener(long handle, Object listener);
    // 设置路径
    private native void setDataSource(long handle, String path);
    // 设置音频解码器名称
    private native void setAudioDecoder(long handle, String decoder);
    // 设置视频解码器名称
    private native void setVideoDecoder(long handle, String decoder);
    // 设置Surface
    private native void setVideoSurface(long handle, Surface surface);
    // 设置播放速度
    private native void setSpeed(long handle, float speed);
    // 设置是否重新播放
    private native void setLooping(long handle, boolean looping);
    // 设置播放区间
    private native void setRange(long handle, float start, float end);
    // 设置播放声音
    private native void setVolume(long handle, float leftVolume, float rightVolume);
    // 准备
    private native void prepare(long handle);
    // 开始播放
    private native void start(long handle);
    // 暂停播放
    private native void pause(long handle);
    // 停止播放
    private native void stop(long handle);
    // 设置处于暂停状态下，是否可以解码，用于连续seek场景使用
    private native void setDecodeOnPause(long handle, boolean decodeOnPause);
    // 定位
    private native void seekTo(long handle, float timeMs);
    // 获取时长
    private native float getDuration(long handle);
    // 获取视频宽度
    private native int getVideoWidth(long handle);
    // 获取视频高度
    private native int getVideoHeight(long handle);
    // 是否循环播放
    private native boolean isLooping(long handle);
    // 是否正在播放中
    private native boolean isPlaying(long handle);

    private long handle;
    private String mPath;

    private EventHandler mEventHandler;
    private OnPlayListener mPlayListener;

    public VideoPlayer() {
        Looper looper;
        if ((looper = Looper.myLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else if ((looper = Looper.getMainLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else {
            mEventHandler = null;
        }

        mPlayListener = null;
        handle = nativeInit();
        if (handle != 0) {
            setOnPlayListener(handle, new OnPlayListener() {

                @Override
                public void onPrepared() {
                    if (mEventHandler != null) {
                        mEventHandler.sendEmptyMessage(PLAYER_PREPARED);
                    }
                }

                @Override
                public void onPlaying(float pts) {
                    if (mEventHandler != null) {
                        mEventHandler.sendMessage(mEventHandler.obtainMessage(PLAYER_PLAYING, pts));
                    }
                }

                @Override
                public void onSeekComplete() {
                    if (mEventHandler != null) {
                        mEventHandler.sendEmptyMessage(PLAYER_SEEKCOMPLETE);
                    }
                }

                @Override
                public void onCompletion() {
                    if (mEventHandler != null) {
                        mEventHandler.sendEmptyMessage(PLAYER_COMPLETION);
                    }
                }

                @Override
                public void onError(int errorCode, String msg) {
                    if (mEventHandler != null) {
                        mEventHandler.sendMessage(mEventHandler.obtainMessage(PLAYER_ERROR, errorCode, -1, msg));
                    }
                }
            });
        }
    }

    public void release() {
        if (handle != 0) {
            nativeRelease(handle);
            handle = 0;
        }
        mPlayListener = null;
    }

    public void setOnPlayListener(OnPlayListener listener) {
        mPlayListener = listener;
    }

    public void setDataSource(String path) {
        mPath = path;
        setDataSource(handle, path);
    }

    public void setAudioDecoder(String decoder) {
        setAudioDecoder(handle, decoder);
    }

    public void setVideoDecoder(String decoder) {
        setVideoDecoder(handle, decoder);
    }

    public void setSurface(Surface surface) {
        setVideoSurface(handle, surface);
    }

    public void setSpeed(float speed) {
        setSpeed(handle, speed);
    }

    public void setLooping(boolean looping) {
        setLooping(handle, looping);
    }

    public void setRange(float startMs, float endMs) {
        setRange(handle, startMs, endMs);
    }

    public void setVolume(float leftVolume, float rightVolume) {
        setVolume(handle, leftVolume, rightVolume);
    }

    public void prepare() {
        prepare(handle);
    }

    public void start() {
        start(handle);
    }

    public void pause() {
        pause(handle);
    }

    public void stop() {
        stop(handle);
    }

    public void setDecodeOnPause(boolean decodeOnPause) {
        setDecodeOnPause(handle, decodeOnPause);
    }

    public void seekTo(float timeMs) {
        seekTo(handle, timeMs);
    }

    public float getDuration() {
        return getDuration(handle);
    }

    public int getVideoWidth() {
        return getVideoWidth(handle);
    }

    public int getVideoHeight() {
        return getVideoHeight(handle);
    }

    public boolean isLooping() {
        return isLooping(handle);
    }

    public boolean isPlaying() {
        return isPlaying(handle);
    }

    public interface OnPlayListener {

        void onPrepared();

        void onPlaying(float pts);

        void onSeekComplete();

        void onCompletion();

        void onError(int errorCode, String msg);
    }

    private static final int PLAYER_PREPARED = 1;
    private static final int PLAYER_PLAYING = 2;
    private static final int PLAYER_SEEKCOMPLETE = 3;
    private static final int PLAYER_COMPLETION = 4;
    private static final int PLAYER_ERROR = 5;

    private class EventHandler extends Handler {
        private VideoPlayer mVideoPlayer;

        public EventHandler(VideoPlayer mp, Looper looper) {
            super(looper);
            mVideoPlayer = mp;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (mVideoPlayer == null) {
                return;
            }

            switch (msg.what) {
                case PLAYER_PREPARED: {
                    if (mPlayListener != null) {
                        mPlayListener.onPrepared();
                    }
                    break;
                }

                case PLAYER_PLAYING: {
                    if (mPlayListener != null) {
                        mPlayListener.onPlaying((float)msg.obj);
                    }
                    break;
                }

                case PLAYER_SEEKCOMPLETE: {
                    if (mPlayListener != null) {
                        mPlayListener.onSeekComplete();
                    }
                    break;
                }

                case PLAYER_COMPLETION: {
                    if (mPlayListener != null) {
                        mPlayListener.onCompletion();
                    }
                    break;
                }

                case PLAYER_ERROR: {
                    if (mPlayListener != null) {
                        mPlayListener.onError(msg.arg1, (String) msg.obj);
                    }
                    break;
                }

                default: {
                    Log.e(TAG, "Unknown message type " + msg.what);
                }
            }
        }
    }
}
