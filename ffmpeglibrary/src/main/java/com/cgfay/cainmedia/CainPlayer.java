package com.cgfay.cainmedia;

import android.graphics.SurfaceTexture;
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

    public CainPlayer() {
        nativeInit();
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
        setNativeSurface(surface);
    }

    /**
     * 设置SurfaceTexture
     * @param surfaceTexture
     */
    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        Surface surface = new Surface(surfaceTexture);
        setNativeSurface(surface);
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
}
