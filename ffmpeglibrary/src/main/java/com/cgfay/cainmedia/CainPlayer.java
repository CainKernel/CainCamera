package com.cgfay.cainmedia;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

/**
 * 给予ffmpeg的自定义播放器
 * Created by cain on 2018/2/9.
 */

public class CainPlayer {

    private static final String TAG = "CainPlayer";
    private static final boolean VERBOSE = false;

    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("cainplayer");
    }

    // native 方法
    private native static void nativeInit();
    private native static void setNativeDataSource(String path);
    private native static void setNativeSurface(Surface surface);
    private native static int getNativeCurrentPosition();
    private native static int getNativeDuration();
    private native static boolean isNativeLooping();
    private native static boolean isNativePlaying();
    private native static void nativePause();
    private native static void nativeStart();
    private native static void nativeStop();
    private native static void nativePrepare();
    private native static void nativeRelease();
    private native static void nativeSeekTo(int msec);
    private native static void nativeSetLooping(boolean loop);
    private native static void nativeSetReverse(boolean reverse);
    private native static void nativeSetPlayAudio(boolean play);
    private native static void nativeChangedSize(int width, int height);

    private EventHandler mEventHandler;

    private Surface mSurface;

    public CainPlayer() {
        nativeInit();
        Looper looper;
        if ((looper = Looper.myLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else if ((looper = Looper.getMainLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else {
            mEventHandler = null;
        }
    }

    /**
     * 设置数据源
     * @param path
     */
    public void setDataSource(String path) {
        setNativeDataSource(path);
    }

    /**
     * 设置Surface
     * @param surface
     */
    public void setSurface(Surface surface) {
        mSurface = surface;
        setNativeSurface(surface);
    }

    /**
     * 设置SurfaceTexture
     * @param surfaceTexture
     */
    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        mSurface = new Surface(surfaceTexture);
        setNativeSurface(mSurface);
    }

    /**
     * 获取当前进度
     * @return
     */
    public int getCurrentPosition() {
        return getNativeCurrentPosition();
    }

    /**
     * 获取总时长
     * @return
     */
    public int getDuration() {
        return getNativeDuration();
    }

    /**
     * 是否循环播放
     * @return
     */
    public boolean isLooping() {
        return isNativeLooping();
    }

    /**
     * 是否正在播放
     * @return
     */
    public boolean isPlaying() {
        return isNativePlaying();
    }

    /**
     * 暂停
     */
    public void pause() {
        nativePause();
    }

    /**
     * 开始播放
     */
    public void start() {
        nativeStart();
    }

    /**
     * 停止播放
     */
    public void stop() {
        nativeStop();
    }

    /**
     * 准备
     */
    public void prepare() {
        nativePrepare();
    }

    /**
     * 释放资源
     */
    public void release() {
        nativeRelease();
        mOnPreparedListener = null;
        mOnCompletionListener = null;
        mOnErrorListener = null;
        mOnSeekCompleteListener = null;
        mOnVideoSizeChangedListener = null;
        mSurface = null;
    }

    /**
     * 定位
     * @param msec
     */
    public void seekTo(int msec) {
        nativeSeekTo(msec);
    }

    /**
     * 设置是否循环播放
     * @param loop
     */
    public void setLooping(boolean loop) {
        nativeSetLooping(loop);
    }

    /**
     * 设置是否倒播
     * @param reverse
     */
    public void setReverse(boolean reverse) {
        nativeSetReverse(reverse);
    }

    /**
     * 设置是否播放音频
     * @param play
     */
    public void setPlayAudio(boolean play) {
        nativeSetPlayAudio(play);
    }

    /**
     * 是否改变大小
     * @param width
     * @param height
     */
    public void changedSize(int width, int height) {
        nativeChangedSize(width, height);
    }

    /**
     * 准备完成回调
     */
    public interface OnPreparedListener {
        void onPrepared(CainPlayer cainPlayer);
    }

    /**
     * 注册准备完成回调
     * @param listener
     */
    public void setOnPreparedListener(OnPreparedListener listener) {
        mOnPreparedListener = listener;
    }

    private OnPreparedListener mOnPreparedListener;

    /**
     * 播放完成回调
     */
    public interface OnCompletionListener {
        void onCompletion(CainPlayer player);
    }

    /**
     * 注册播放完成回调
     * @param listener
     */
    public void setOnCompletionListener(OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    private OnCompletionListener mOnCompletionListener;

    /**
     * 定位完成回调
     */
    public interface OnSeekCompleteListener {
        public void onSeekComplete(CainPlayer cainPlayer);
    }

    /**
     * 注册定位完成回调
     * @param listener
     */
    public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
        mOnSeekCompleteListener = listener;
    }

    private OnSeekCompleteListener mOnSeekCompleteListener;

    /**
     * 视频大小变化回调
     */
    public interface OnVideoSizeChangedListener {
        void onVideoSizeChanged(CainPlayer player, int width, int height);
    }

    /**
     * 注册视频大小变化回调
     * @param listener
     */
    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
        mOnVideoSizeChangedListener = listener;
    }

    private OnVideoSizeChangedListener mOnVideoSizeChangedListener;

    /**
     * 未知出错
     * @see com.cgfay.cainmedia.CainPlayer.OnErrorListener
     */
    public static final int MEDIA_ERROR_UNKNOWN = 1;

    /**
     * 媒体服务死掉了
     * @see com.cgfay.cainmedia.CainPlayer.OnErrorListener
     */
    public static final int MEDIA_ERROR_SERVICE_DIED = 100;

    /**
     * 文件操作出错
     */
    public static final int MEDIA_ERROR_IO = -1004;

    /**
     * 不支持的媒体类型
     */
    public static final int MEDIA_ERROR_UNSUPPORTED = -1010;

    /**
     * 超时
     */
    public static final int MEDIA_ERROR_TIMED_OUT = -110;

    /**
     * 出错回调
     */
    public interface OnErrorListener {
        /**
         * 出错回调
         * @param player
         * @param what
         * <ul>
         * <li> {@link #MEDIA_ERROR_UNKNOWN}
         * <li> {@link #MEDIA_ERROR_SERVICE_DIED}
         * </ul>
         * @param extra
         * <ul>
         * <li> {@link #MEDIA_ERROR_IO}
         * <li> {@link #MEDIA_ERROR_UNSUPPORTED}
         * <li> {@link #MEDIA_ERROR_TIMED_OUT}
         * </ul>
         * @return 为true表示处理出错，为false表示不处理
         * 如果返回false，或者没有出错回调，则将会调用 OnCompletionListener 回调
         */
        boolean onError(CainPlayer player, int what, int extra);
    }

    /**
     * 注册出错回调
     * @param listener
     */
    public void setOnErrorListener(OnErrorListener listener) {
        mOnErrorListener = listener;
    }

    private OnErrorListener mOnErrorListener;


    private static final int MEDIA_NOP = 0;
    private static final int MEDIA_PREPARED = 1;
    private static final int MEDIA_PLAYBACK_COMPLETE = 2;
    private static final int MEDIA_SEEK_COMPLETE = 3;
    private static final int MEDIA_SET_VIDEO_SIZE = 4;
    private static final int MEDIA_STARTED = 5;
    private static final int MEDIA_PAUSED = 6;
    private static final int MEDIA_STOPPED = 7;
    private static final int MEDIA_ERROR = 100;

    private class EventHandler extends Handler {
        private CainPlayer mCainPlayer;

        public EventHandler(CainPlayer player, Looper looper) {
            super(looper);
            mCainPlayer = player;
        }


        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 准备完成
                case MEDIA_PREPARED: {

                    OnPreparedListener listener = mOnPreparedListener;
                    if (listener != null) {
                        listener.onPrepared(mCainPlayer);
                    }
                    break;
                }

                // 播放完成
                case MEDIA_PLAYBACK_COMPLETE: {
                    OnCompletionListener listener = mOnCompletionListener;
                    if (listener != null) {
                        listener.onCompletion(mCainPlayer);
                    }
                    break;
                }

                // 停止
                case MEDIA_STOPPED: {

                    break;
                }

                // 暂停或开始
                case MEDIA_STARTED:
                case MEDIA_PAUSED: {
                    break;
                }

                // 定位完成
                case MEDIA_SEEK_COMPLETE: {
                    OnSeekCompleteListener listener = mOnSeekCompleteListener;
                    if (listener != null) {
                        listener.onSeekComplete(mCainPlayer);
                    }
                    break;
                }

                // 设置大小
                case MEDIA_SET_VIDEO_SIZE: {
                    OnVideoSizeChangedListener listener = mOnVideoSizeChangedListener;
                    if (listener != null) {
                        listener.onVideoSizeChanged(mCainPlayer, msg.arg1, msg.arg2);
                    }
                    break;
                }

                // 出错
                case MEDIA_ERROR: {
                    Log.e(TAG, "Error ( " + msg.arg1 + ", " + msg.arg2 + ")");
                    boolean error_was_handled = false;
                    OnErrorListener listener = mOnErrorListener;
                    if (listener != null) {
                        error_was_handled = listener.onError(mCainPlayer, msg.arg1, msg.arg2);
                    }
                    {
                        OnCompletionListener onCompletionListener = mOnCompletionListener;
                        if (onCompletionListener != null && !error_was_handled) {
                            onCompletionListener.onCompletion(mCainPlayer);
                        }
                    }
                    break;
                }

                case MEDIA_NOP:
                    break;

                default:
                    Log.e(TAG, "Unknown message type: " + msg.what);
                    break;
            }
        }
    }

    /**
     * native 层调用的事件处理函数
     * @param what
     * @param arg1
     * @param arg2
     */
    private void playerEventCallback(int what, int arg1, int arg2) {
        if (mEventHandler != null) {
            Message msg = new Message();
            msg.what = what;
            msg.arg1 = arg1;
            msg.arg2 = arg2;
            mEventHandler.sendMessage(msg);
        }
    }
}
