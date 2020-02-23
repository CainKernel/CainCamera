package com.cgfay.media;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

public class MusicPlayer {

    private static final String TAG = "MusicPlayer";

    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("musicplayer");
    }

    // 初始化
    private native long nativeInit();
    // 释放资源
    private native void nativeRelease(long handle);
    // 播放监听器
    private native void setOnPlayListener(long handle, Object listener);
    // 设置音乐路径
    private native void setDataSource(long handle, String path);
    // 设置音乐播放速度
    private native void setSpeed(long handle, float speed);
    // 设置是否重新播放
    private native void setLooping(long handle, boolean looping);
    // 设置播放区间
    private native void setRange(long handle, float start, float end);
    // 设置播放声音
    private native void setVolume(long handle, float leftVolume, float rightVolume);
    // 开始播放
    private native void start(long handle);
    // 暂停播放
    private native void pause(long handle);
    // 停止播放
    private native void stop(long handle);
    // 定位
    private native void seekTo(long handle, float timeMs);
    // 获取时长
    private native float getDuration(long handle);
    // 是否循环播放
    private native boolean isLooping(long handle);
    // 是否正在播放中
    private native boolean isPlaying(long handle);

    private long handle;
    private String mPath;

    private EventHandler mEventHandler;
    private OnPlayListener mPlayListener;

    public MusicPlayer() {
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

    @Override
    protected void finalize() throws Throwable {
        release();
        super.finalize();
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

    public void start() {
        start(handle);
    }

    public void pause() {
        pause(handle);
    }

    public void stop() {
        stop(handle);
    }

    public void seekTo(float timeMs) {
        seekTo(handle, timeMs);
    }

    public float getDuration() {
        return getDuration(handle);
    }

    public boolean isLooping() {
        return isLooping(handle);
    }

    public boolean isPlaying() {
        return isPlaying(handle);
    }

    public interface OnPlayListener {

        void onPlaying(float pts);

        void onSeekComplete();

        void onCompletion();

        void onError(int errorCode, String msg);
    }

    private static final int PLAYER_PLAYING = 0;
    private static final int PLAYER_SEEKCOMPLETE = 1;
    private static final int PLAYER_COMPLETION = 2;
    private static final int PLAYER_ERROR = 3;

    private class EventHandler extends Handler {
        private MusicPlayer mMusicPlayer;

        public EventHandler(MusicPlayer mp, Looper looper) {
            super(looper);
            mMusicPlayer = mp;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (mMusicPlayer == null) {
                return;
            }

            switch (msg.what) {
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
