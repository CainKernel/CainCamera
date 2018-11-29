package com.cgfay.ffmpeglibrary.MusicPlayer;

import android.text.TextUtils;

/**
 * 基于FFmpeg的音乐播放器
 */
public class AVMusicPlayer {

    private static final String TAG = "AVMusicPlayer";

    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("music_player");
    }

    private String mPath;

    private boolean mPrepared;

    // 播放完成回调
    private OnCompletionListener mCompletionListener;
    // 播放出错回调
    private OnErrorListener mErrorListener;
    // 准备完成回调
    private OnPreparedListener mPreparedListener;
    // 当前播放信息回调
    private OnCurrentInfoListener mCurrentInfoListener;
    // 音频增益回调
    private OnVolumeDBListener mVolumeDBListener;

    public AVMusicPlayer() {
        nativeSetup();
    }

    @Override
    protected void finalize() throws Throwable {
        nativeRelease();
        super.finalize();
    }

    /**
     * 设置数据源
     * @param path
     */
    public void setDataSource(String path) {
        mPath = path;
    }

    /**
     * 设置是否循环播放
     * @param looping
     */
    public void setLooping(boolean looping) {
        nativeSetLooping(looping);
    }

    /**
     * 准备
     */
    public void prepare() {
        if (TextUtils.isEmpty(mPath)) {
            return;
        }
        nativePrepare(mPath);
    }

    /**
     * 开始
     */
    public void start() {
        if (!mPrepared) {
            return;
        }
        nativeStart();
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
     * 停止
     */
    public void stop() {
        nativeStop();
    }

    /**
     * 释放资源
     */
    public void release() {
        nativeRelease();
    }

    /**
     * 定位
     * @param seconds
     */
    public void seekTo(int seconds) {
        nativeSeek(seconds);
    }

    /**
     * 设置音量
     * @param percent
     */
    public void setVolume(int percent) {
        nativeSetVolume(percent);
    }

    /**
     * 设置音频Channel
     * @param channel
     */
    public void setAudioChannel(int channel) {
        nativeSetChannelType(channel);
    }

    /**
     * 设置速度
     * @param speed
     */
    public void setSpeed(float speed) {
        nativeSetSpeed(speed);
    }

    /**
     * 设置pitch
     * @param pitch
     */
    public void setPitch(float pitch) {
        nativeSetPitch(pitch);
    }

    /**
     * 设置节拍
     * @param tempo
     */
    public void setTempo(float tempo) {
        nativeSetTempo(tempo);
    }

    /**
     * 设置速度变化值
     * @param speedChange
     */
    public void setSpeedChange(double speedChange) {
        nativeSetSpeedChange(speedChange);
    }

    /**
     * 设置节拍改变值
     * @param tempoChange
     */
    public void setTempoChange(double tempoChange) {
        nativeSetTempoChange(tempoChange);
    }

    /**
     * 八度音调节
     * @param pitchOctaves
     */
    public void setPitchOctaves(double pitchOctaves) {
        nativeSetPitchOctaves(pitchOctaves);
    }

    /**
     * 设置半音阶调节
     * @param semiTones
     */
    public void setPitchSemiTones(double semiTones) {
        nativeSetPitchSemiTones(semiTones);
    }

    /**
     * 获取采样率
     * @return
     */
    public int getSampleRate() {
        return nativeGetSampleRate();
    }

    /**
     * 获取时长
     * @return
     */
    public int getDuration() {
        return nativeGetDuration();
    }

    /**
     * 是否正在播放状态
     * @return
     */
    public boolean isPlaying() {
        return nativePlaying();
    }

    /**
     * 是否已经准备
     * @return
     */
    public boolean isPrepared() {
        return mPrepared;
    }

    // native 方法
    private native void nativeSetup();
    private native void nativeRelease();
    private native void nativeSetLooping(boolean looping);
    private native void nativePrepare(String path);
    private native void nativeStart();
    private native void nativePause();
    private native void nativeResume();
    private native void nativeStop();
    private native void nativeSeek(int seconds);
    private native void nativeSetVolume(int percent);
    private native void nativeSetChannelType(int channelType);
    private native void nativeSetSpeed(float speed);
    private native void nativeSetPitch(float pitch);
    private native void nativeSetTempo(float tempo);
    private native void nativeSetSpeedChange(double speedChange);
    private native void nativeSetTempoChange(double tempoChange);
    private native void nativeSetPitchOctaves(double pitchOctaves);
    private native void nativeSetPitchSemiTones(double semiTones);
    private native int nativeGetSampleRate();
    private native int nativeGetDuration();
    private native boolean nativePlaying();

    /**
     * 播放完成回调，jni层调用
     */
    private void onCompletion() {
        mPrepared = false;
        stop();
        if (mCompletionListener != null) {
            mCompletionListener.onCompleted();
        }
    }

    /**
     * 出错回调，jni层调用
     * @param code
     * @param msg
     */
    private void onError(int code, String msg) {
        mPrepared = false;
        stop();
        if (mErrorListener != null) {
            mErrorListener.onError(code, msg);
        }
    }

    /**
     * 准备完成回调，jni层调用
     */
    private void onPrepared() {
        mPrepared = true;
        if (mPreparedListener != null) {
            mPreparedListener.onPrepared();
        }
    }

    /**
     * 播放时间信息，jni层调用
     * @param current
     * @param duration
     */
    private void onCurrentInfo(int current, int duration) {
        if (mCurrentInfoListener != null) {
            mCurrentInfoListener.onCurrentInfo(current, duration);
        }
    }

    /**
     * 获取音频PCM数据，jni层调用
     * @param bytes
     * @param size
     */
    private void onGetPCM(byte[] bytes, int size) {

    }

    /**
     * 音量增益回调，jni层调用
     * @param db
     */
    private void onVolumeDB(int db) {
        if (mVolumeDBListener != null) {
            mVolumeDBListener.onVolumeDB(db);
        }
    }

    /**
     * 准备完成监听器
     */
    public interface OnPreparedListener {
        void onPrepared();
    }

    /**
     * 设置准备完成回调
     * @param listener
     */
    public void setOnPreparedListener(OnPreparedListener listener) {
        mPreparedListener = listener;
    }

    /**
     * 播放完成监听器
     */
    public interface OnCompletionListener {
        void onCompleted();
    }

    /**
     * 设置完成回调
     * @param listener
     */
    public void setOnCompletionListener(OnCompletionListener listener) {
        mCompletionListener = listener;
    }

    /**
     * 出错监听器
     */
    public interface OnErrorListener {
        void onError(int code, String msg);
    }

    /**
     * 设置出错回调
     * @param listener
     */
    public void setOnErrorListener(OnErrorListener listener) {
        mErrorListener = listener;
    }

    /**
     * 当前信息监听器
     */
    public interface OnCurrentInfoListener {
        void onCurrentInfo(int current, int duration);
    }

    /**
     * 设置当前信息监听器
     * @param listener
     */
    public void setOnCurrentInfoListener(OnCurrentInfoListener listener) {
        mCurrentInfoListener = listener;
    }

    /**
     * 音频增益监听器
     */
    public interface OnVolumeDBListener {
        void onVolumeDB(int db);
    }

    /**
     * 设置音量增益回调
     * @param listener
     */
    public void setOnVolumeDBListener(OnVolumeDBListener listener) {
        mVolumeDBListener = listener;
    }
}
