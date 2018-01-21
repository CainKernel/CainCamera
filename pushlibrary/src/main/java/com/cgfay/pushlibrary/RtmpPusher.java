package com.cgfay.pushlibrary;

/**
 * RTMP推流器
 * Created by cain on 2018/1/21.
 */

public class RtmpPusher {

    static {
        System.loadLibrary("pusher");
    }

    /**
     * 初始化视频编码
     * @param url       rtmp地址
     * @param width     宽度
     * @param height    高度
     * @param bitRate   比特率
     * @return 返回0表示成功，非零表示失败
     */
    public native int initVideo(String url, int width, int height, int bitRate);

    /**
     * 推送视频编码
     * @param data nv21的原始YUV数据
     * @param index 前置摄像头还是后置摄像头还是横屏摄像
     */
    public native void pushYUV(byte[] data, int index);

    /**
     * 初始化音频编码
     * @param sampleRate 采样频率
     * @param channel   声道数
     */
    public native void initAudio(int sampleRate, int channel);

    /**
     * 推送PCM数据
     * @param data PCM数据
     */
    public native void pushPCM(byte[] data);

    /**
     * 停止推流
     */
    public native void stop();

}
