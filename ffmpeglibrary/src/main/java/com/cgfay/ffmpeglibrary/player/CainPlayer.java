package com.cgfay.ffmpeglibrary.player;

import android.graphics.SurfaceTexture;
import android.text.TextUtils;
import android.view.Surface;

public class CainPlayer {
    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("cainplayer");
    }

    /**
     * 初始化
     */
    private native void nativeSetup();

    /**
     * 释放资源
     */
    private native void nativeRelease();

    /**
     * 准备解码器
     * @param dataSource    数据源
     */
    private native void nativePrepare(String dataSource);

    /**
     * 设置Surface
     * @param surface
     */
    private native void nativeSetSurface(Surface surface);

    /**
     * 开始
     */
    private native void nativeStart();

    /**
     * 停止
     */
    private native void nativeStop();

    /**
     * 暂停
     */
    private native void nativePause();

    /**
     * 再启动
     */
    private native void nativeResume();

    /**
     * 定位
     * @param seconds
     */
    private native void nativeSeek(int seconds);

    /**
     * 设置音轨
     * @param index
     */
    private native void nativeSetAudioChannel(int index);

    /**
     * 获取时长
     * @return
     */
    private native int nativeGetDuration();

    /**
     * 获取音轨数量
     * @return
     */
    private native int nativeGetAudioChannels();

    /**
     * 获取视频宽度
     * @return
     */
    private native int nativeGetVideoWidth();

    /**
     * 获取视频高度
     * @return
     */
    private native int nativeGetVideoHeight();

    /**
     * 播放完成监听器
     */
    private OnCompletionListener mCompletionListener;

    /**
     * 出错监听器
     */
    private OnErrorListener mErrorListener;

    /**
     * 帧可用监听器
     */
    private OnFrameAvailableListener mFrameAvailableListener;

    /**
     * 加载监听器
     */
    private OnLoadListener mLoadListener;

    /**
     * 准备完成监听器
     */
    private OnPreparedListener mPreparedListener;

    /**
     * 截屏监听器
     */
    private OnScreenCatchListener mScreenCatchListener;

    /**
     * 播放时间信息监听器
     */
    private OnTimeInfoListener mTimeInfoListener;

    /**
     * 数据源
     */
    private String mDataSource;

    /**
     * 用于渲染的Surface
     */
    private Surface mSurface;

    /**
     * 是否已经准备好了
     */
    private boolean mPrepared = false;

    private int mPrevTime;
    private TimeInfo mTimeInfo;

    public CainPlayer() {
        nativeSetup();
    }

    @Override
    protected void finalize() throws Throwable {
        nativeRelease();
        super.finalize();
    }

    /**
     * 设置数据源
     * @param dataSource
     */
    public void setDataSource(String dataSource) {
        mDataSource = dataSource;
    }

    /**
     * 设置Surface
     * @param surface
     */
    public void setSurface(Surface surface) {
        mSurface = surface;
        nativeSetSurface(surface);
    }

    /**
     * 设置SurfaceTexture
     * @param texture
     */
    public void setSurfaceTexture(SurfaceTexture texture) {
        if (texture == null) {
            mSurface = null;
        }
        mSurface = new Surface(texture);
    }

    /**
     * 设置Surface大小发生变化
     * @param width
     * @param height
     */
    public void setSurfaceChanged(int width, int height) {

    }

    /**
     * 准备
     */
    public void prepare() {
        if (TextUtils.isEmpty(mDataSource)) {
            onError(MediaMessage.DATASOURCE_NULL, "data source is null");
            return;
        }
        nativePrepare(mDataSource);
    }

    /**
     * 开始
     */
    public void start() {
        if (TextUtils.isEmpty(mDataSource)) {
            onError(MediaMessage.DATASOURCE_NULL, "data source is null");
            return;
        }
        if (mSurface == null) {
            onError(MediaMessage.SURFACE_NULL, "data source is null");
            return;
        }
        mTimeInfo = new TimeInfo();
        nativeStart();
    }

    /**
     * 停止
     */
    public void stop() {
        nativeStop();
    }

    /**
     * 暂停
     */
    public void pause() {
        nativePause();
    }

    /**
     * 启动
     */
    public void resume() {
        nativeResume();
    }

    /**
     * 定位
     * @param seconds
     */
    public void seekTo(int seconds) {
        nativeSeek(seconds);
    }

    /**
     * 设置音轨
     * @param index
     */
    public void setAudioChannel(int index) {
        nativeSetAudioChannel(index);
    }

    /**
     * 获取媒体总时长
     * @return
     */
    public int getDuration() {
        return nativeGetDuration();
    }

    /**
     * 获取音轨数
     * @return
     */
    public int getAudioChannels() {
        return nativeGetAudioChannels();
    }

    /**
     * 获取视频宽度
     * @return
     */
    public int getVideoWidth() {
        return nativeGetVideoWidth();
    }

    /**
     * 获取视频高度
     * @return
     */
    public int getVideoHeight() {
        return nativeGetVideoHeight();
    }

    /**
     * 设置播放完成回调
     * @param listener
     */
    public void setOnCompletionListener(OnCompletionListener listener) {
        mCompletionListener = listener;
    }

    /**
     * 设置出错回调
     * @param listener
     */
    public void setOnErrorListener(OnErrorListener listener) {
        mErrorListener = listener;
    }

    /**
     * 设置帧可用回调
     * @param listener
     */
    public void setOnFrameAvailableListener(OnFrameAvailableListener listener) {
        mFrameAvailableListener = listener;
    }

    /**
     * 设置加载回调
     * @param listener
     */
    public void setOnLoadListener(OnLoadListener listener) {
        mLoadListener = listener;
    }

    /**
     * 设置准备完成回调
     * @param listener
     */
    public void setOnPreparedListener(OnPreparedListener listener) {
        mPreparedListener = listener;
    }

    /**
     * 设置截屏回调
     * @param listener
     */
    public void setOnScreenCatchListener(OnScreenCatchListener listener) {
        mScreenCatchListener = listener;
    }

    /**
     * 设置播放信息回调
     * @param listener
     */
    public void setOnTimeInfoListener(OnTimeInfoListener listener) {
        mTimeInfoListener = listener;
    }

    /**
     * 播放完成回调，jni层调用
     */
    private void onCompletion() {
        if (mCompletionListener != null) {
            onTimeInfo(getDuration(), getDuration());
            mTimeInfo = null;
            mCompletionListener.onComplete();
        }
    }

    /**
     * 出错回调，jni层调用
     * @param code
     * @param msg
     */
    private void onError(int code, String msg) {
        if (mErrorListener != null) {
            mErrorListener.onError(code, msg);
        }
        stop();
    }

    /**
     * 加载回调，jni层调用
     * @param load
     */
    private void onLoad(boolean load) {
        if (mLoadListener != null) {
            mLoadListener.onLoad(load);
        }
    }

    /**
     * 准备完成回调，jni层调用
     */
    private void onPrepared() {
        if (mPreparedListener != null) {
            mPreparedListener.onPrepared();
        }
    }

    /**
     * 播放信息回调，jni层调用
     * @param current
     * @param duration
     */
    private void onTimeInfo(int current, int duration) {
        if (mTimeInfoListener != null) {
            if (mTimeInfo == null) {
                mTimeInfo = new TimeInfo();
            }
            if (mPrevTime > current) {
                current = mPrevTime;
            }
            mTimeInfo.setCurrent(current);
            mTimeInfo.setDuration(duration);
            mPrevTime = current;
            mTimeInfoListener.onTimeInfo(mTimeInfo);
        }
    }

    /**
     * 定位完成回调，jni层调用
     * @param current 当前时间
     */
    private void onSeekCompletion(int current) {
        mPrevTime = current;
    }
}

