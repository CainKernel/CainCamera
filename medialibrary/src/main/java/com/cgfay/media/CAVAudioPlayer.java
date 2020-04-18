package com.cgfay.media;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.cgfay.media.annotations.AccessedByNative;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * 音频播放器
 */
public class CAVAudioPlayer {

    private static final String TAG = "CAVAudioPlayer";

    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("audio_player");
        native_init();
    }

    private static native void native_init();
    // 初始化
    private native void native_setup(Object mediaplayer_this);
    private native void native_finalize();
    // 释放资源
    private native void _release();
    // 设置音乐路径
    private native void _setDataSource(String path);
    // 设置音乐播放速度
    private native void _setSpeed(float speed);
    // 设置是否重新播放
    private native void _setLooping(boolean looping);
    // 设置播放区间
    private native void _setRange(float start, float end);
    // 设置播放声音
    private native void _setVolume(float leftVolume, float rightVolume);
    // 准备播放器
    private native void _prepare();
    // 开始播放
    private native void _start();
    // 暂停播放
    private native void _pause();
    // 停止播放
    private native void _stop();
    // 定位
    private native void _seekTo(float timeMs);
    // 获取时长
    private native float _getDuration();
    // 是否循环播放
    private native boolean _isLooping();
    // 是否正在播放中
    private native boolean _isPlaying();

    @AccessedByNative
    private long mNativeContext;
    private EventHandler mEventHandler;

    public CAVAudioPlayer() {
        Looper looper;
        if ((looper = Looper.myLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else if ((looper = Looper.getMainLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else {
            mEventHandler = null;
        }

        /* Native setup requires a weak reference to our object.
         * It's easier to create it here than in C++.
         */
        native_setup(new WeakReference<CAVAudioPlayer>(this));
    }

    public void release() {
        mOnPreparedListener = null;
        mOnCompletionListener = null;
        mOnSeekCompleteListener = null;
        mOnErrorListener = null;
        mOnCurrentPositionListener = null;
        _release();
    }

    /**
     * 设置音乐路径
     * @param path
     */
    public void setDataSource(String path)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        _setDataSource(path);
    }

    /**
     * 设置播放速度
     * @param speed
     */
    public void setSpeed(float speed) {
        _setSpeed(speed);
    }

    /**
     * 设置是否循环播放
     * @param looping
     */
    public void setLooping(boolean looping) {
        _setLooping(looping);
    }

    /**
     * 设置播放区间
     * @param startMs   播放起始位置
     * @param endMs     播放结束位置
     */
    public void setRange(float startMs, float endMs) {
        _setRange(startMs, endMs);
    }

    /**
     * 设置音量
     * @param leftVolume    左声道音量
     * @param rightVolume   右声道音量
     */
    public void setVolume(float leftVolume, float rightVolume) {
        _setVolume(leftVolume, rightVolume);
    }

    /**
     * 准备播放器
     */
    public void prepare() throws IllegalStateException {
        _prepare();
    }

    /**
     * 开始
     */
    public void start() throws IllegalStateException {
        _start();
    }

    /**
     * 暂停
     */
    public void pause() throws IllegalStateException {
        _pause();
    }

    /**
     * 停止
     */
    public void stop() throws IllegalStateException {
        _stop();
    }

    /**
     * 跳转到某个时间戳
     * @param timeMs
     */
    public void seekTo(float timeMs) throws IllegalStateException {
        _seekTo(timeMs);
    }

    /**
     * 获取时长
     * @return
     */
    public float getDuration() {
        return _getDuration();
    }

    /**
     * 是否循环播放
     * @return
     */
    public boolean isLooping() {
        return _isLooping();
    }

    /**
     * 是否正在播放
     * @return
     */
    public boolean isPlaying() {
        return _isPlaying();
    }

    @Override
    protected void finalize() throws Throwable {
        native_finalize();
        super.finalize();
    }

    /* Do not change these values without updating their counterparts
     * in CAVAudioPlayer.h!
     */
    private static final int MEDIA_PREPARED = 1;
    private static final int MEDIA_STARTED = 2;
    private static final int MEDIA_PLAYBACK_COMPLETE = 3;
    private static final int MEDIA_SEEK_COMPLETE = 4;
    private static final int MEDIA_ERROR = 100;
    private static final int MEDIA_INFO = 200;
    private static final int MEDIA_CURRENT = 300;

    private class EventHandler extends Handler {
        private final CAVAudioPlayer mCAVAudioPlayer;

        public EventHandler(CAVAudioPlayer mp, Looper looper) {
            super(looper);
            mCAVAudioPlayer = mp;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (mCAVAudioPlayer.mNativeContext == 0) {
                Log.w(TAG, "musicplayer went away with unhandled events");
                return;
            }

            switch (msg.what) {
                // 准备完成
                case MEDIA_PREPARED: {
                    if (mOnPreparedListener != null) {
                        mOnPreparedListener.onPrepared(mCAVAudioPlayer);
                    }
                    break;
                }

                // 播放完成回调
                case MEDIA_PLAYBACK_COMPLETE: {
                    if (mOnCompletionListener != null) {
                        mOnCompletionListener.onCompletion(mCAVAudioPlayer);
                    }
                    break;
                }

                // 播放开始回调
                case MEDIA_STARTED: {
                    Log.d(TAG, "music player is started!");
                    break;
                }

                // 跳转完成回调
                case MEDIA_SEEK_COMPLETE: {
                    if (mOnSeekCompleteListener != null) {
                        mOnSeekCompleteListener.onSeekComplete(mCAVAudioPlayer);
                    }
                    break;
                }

                // 播放出错回调
                case MEDIA_ERROR: {
                    Log.e(TAG, "Error (" + msg.arg1 + "," + msg.arg2 + ")");
                    boolean error_was_handled = false;
                    if (mOnErrorListener != null) {
                        error_was_handled = mOnErrorListener.onError(mCAVAudioPlayer, msg.arg1, msg.arg2);
                    }
                    if (mOnCompletionListener != null && !error_was_handled) {
                        mOnCompletionListener.onCompletion(mCAVAudioPlayer);
                    }
                    break;
                }

                // 播放过程中的信息回调
                case MEDIA_INFO: {
                    break;
                }

                // 当前播放进度回调
                case MEDIA_CURRENT: {
                    if (mOnCurrentPositionListener != null) {
                        mOnCurrentPositionListener.onCurrentPosition(mCAVAudioPlayer, msg.arg1, msg.arg2);
                    }
                    break;
                }

                default: {
                    Log.e(TAG, "Unknown message type " + msg.what);
                    break;
                }
            }
        }
    }

    /**
     * Called from native code when an interesting event happens.  This method
     * just uses the EventHandler system to post the event back to the main app thread.
     * We use a weak reference to the original CAVAudioPlayer object so that the native
     * code is safe from the object disappearing from underneath it.  (This is
     * the cookie passed to native_setup().)
     */
    private static void postEventFromNative(Object mediaplayer_ref,
                                            int what, int arg1, int arg2, Object obj) {
        final CAVAudioPlayer mp = (CAVAudioPlayer)((WeakReference) mediaplayer_ref).get();
        if (mp == null) {
            return;
        }

        if (mp.mEventHandler != null) {
            Message m = mp.mEventHandler.obtainMessage(what, arg1, arg2, obj);
            mp.mEventHandler.sendMessage(m);
        }
    }

    /**
     * Interface definition for a callback to be invoked when the media
     * source is ready for playback.
     */
    public interface OnPreparedListener {
        /**
         * Called when the media file is ready for playback.
         *
         * @param mp the CAVAudioPlayer that is ready for playback
         */
        void onPrepared(CAVAudioPlayer mp);
    }

    /**
     * Register a callback to be invoked when the media source is ready
     * for playback.
     *
     * @param listener the callback that will be run
     */
    public void setOnPreparedListener(OnPreparedListener listener) {
        mOnPreparedListener = listener;
    }

    private OnPreparedListener mOnPreparedListener;

    /**
     * Interface definition for a callback to be invoked when playback of
     * a media source has completed.
     */
    public interface OnCompletionListener {
        /**
         * Called when the end of a media source is reached during playback.
         *
         * @param mp the MediaPlayer that reached the end of the file
         */
        void onCompletion(CAVAudioPlayer mp);
    }

    /**
     * Register a callback to be invoked when the end of a media source
     * has been reached during playback.
     *
     * @param listener the callback that will be run
     */
    public void setOnCompletionListener(OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    private OnCompletionListener mOnCompletionListener;

    /**
     * Interface definition of a callback to be invoked indicating
     * the completion of a seek operation.
     */
    public interface OnSeekCompleteListener {
        /**
         * Called to indicate the completion of a seek operation.
         *
         * @param mp the MediaPlayer that issued the seek operation
         */
        void onSeekComplete(CAVAudioPlayer mp);
    }

    /**
     * Register a callback to be invoked when a seek operation has been
     * completed.
     *
     * @param listener the callback that will be run
     */
    public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
        mOnSeekCompleteListener = listener;
    }

    private OnSeekCompleteListener mOnSeekCompleteListener;

    /* Do not change these values without updating their counterparts
     * in CAVAudioPlayer.h!
     */
    /**
     * Unspecified media player error.
     */
    public static final int MEDIA_ERROR_UNKNOWN = 1;

    /** Media server died. In this case, the application must release the
     * MediaPlayer object and instantiate a new one.
     */
    public static final int MEDIA_ERROR_SERVER_DIED = 100;

    /** The video is streamed and its container is not valid for progressive
     * playback i.e the video's index (e.g moov atom) is not at the start of the
     * file.
     */
    public static final int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;

    /**
     * Interface definition of a callback to be invoked when there
     * has been an error during an asynchronous operation (other errors
     * will throw exceptions at method call time).
     */
    public interface OnErrorListener {
        /**
         * Called to indicate an error.
         *
         * @param mp      the MediaPlayer the error pertains to
         * @param what    the type of error that has occurred:
         * <ul>
         * <li>{@link #MEDIA_ERROR_UNKNOWN}
         * <li>{@link #MEDIA_ERROR_SERVER_DIED}
         * </ul>
         * @param extra an extra code, specific to the error. Typically
         * implementation dependant.
         * @return True if the method handled the error, false if it didn't.
         * Returning false, or not having an OnErrorListener at all, will
         * cause the OnCompletionListener to be called.
         */
        boolean onError(CAVAudioPlayer mp, int what, int extra);
    }

    /**
     * Register a callback to be invoked when an error has happened
     * during an asynchronous operation.
     *
     * @param listener the callback that will be run
     */
    public void setOnErrorListener(OnErrorListener listener) {
        mOnErrorListener = listener;
    }
    
    private OnErrorListener mOnErrorListener;

    /**
     * Interface definition of a callback to be invoked to playing position.
     */
    public interface OnCurrentPositionListener {

        void onCurrentPosition(CAVAudioPlayer mp, float current, float duration);
    }

    /**
     * Register a callback to be invoked on playing position.
     * @param listener
     */
    public void setOnCurrentPositionListener(OnCurrentPositionListener listener) {
        mOnCurrentPositionListener = listener;
    }
    private OnCurrentPositionListener mOnCurrentPositionListener;
}
