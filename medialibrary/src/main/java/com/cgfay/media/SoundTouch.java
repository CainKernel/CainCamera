package com.cgfay.media;

/**
 * SoundTouch库
 * @author CainHuang
 * @date 2019/6/30
 */
public class SoundTouch {

    static {
        System.loadLibrary("soundtouch");
    }

    // 初始化
    private native static long nativeInit();
    // 销毁对象
    private native void nativeRelease(long handle);
    // 设置频率
    private native void setRate(long handle, double rate);
    // 设置节拍
    private native void setTempo(long handle, float tempo);
    // 设置频率改变
    private native void setRateChange(long handle, double rate);
    // 设置节拍改变
    private native void setTempoChange(long handle, float tempo);
    // 设置音高
    private native void setPitch(long handle, float pitch);
    // 设置八度音
    private native void setPitchOctaves(long handle, float pitch);
    // 设置半音
    private native void setPitchSemiTones(long handle, float pitch);
    // 设置声道数
    private native void setChannels(long handle, int channels);
    // 设置采样率
    private native void setSampleRate(long handle, int sampleRate);
    // 清空缓冲区
    private native void flush(long handle);
    // 入队PCM数据
    private native void putSamples(long handle, byte[] input, int offset, int length);
    // 取出SoundTouch处理后的PCM数据
    private native int receiveSamples(long handle, byte[] output, int length);

    private long handle;

    public SoundTouch() {
        handle = nativeInit();
    }

    public void setRate(double rate) {
        setRate(handle, rate);
    }

    public void setTempo(float tempo) {
        setTempo(handle, tempo);
    }

    public void setRateChange(double rate) {
        setRateChange(handle, rate);
    }

    public void setTempoChange(float tempo) {
        setTempoChange(handle, tempo);
    }

    public void setPitch(float pitch) {
        setPitch(handle, pitch);
    }

    public void setPitchOctaves(float pitch) {
        setPitchOctaves(handle, pitch);
    }

    public void setPitchSemiTones(float pitch) {
        setPitchSemiTones(handle, pitch);
    }

    public void setChannels(int channels) {
        setChannels(handle, channels);
    }

    public void setSampleRate(int sampleRate) {
        setSampleRate(handle, sampleRate);
    }

    public void flush() {
        flush(handle);
    }

    public void putSamples(byte[] input) {
        putSamples(handle, input, 0, input.length);
    }

    public int receiveSamples(byte[] output) {
        return receiveSamples(handle, output, output.length);
    }

    public void close() {
        nativeRelease(handle);
        handle = 0;
    }

    @Override
    protected void finalize() throws Throwable {
        if (handle != 0) {
            close();
        }
        super.finalize();
    }
}
