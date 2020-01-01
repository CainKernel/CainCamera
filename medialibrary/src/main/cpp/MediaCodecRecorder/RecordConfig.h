//
// Created by CainHuang on 2019/9/15.
//

#ifndef RECORDCONFIG_H
#define RECORDCONFIG_H

#if defined(__ANDROID__)

#include <cstdint>

class RecordConfig {
public:
    const char *dstFile;        // 输出文件路径
    int width;                  // 宽度
    int height;                 // 高度
    int frameRate;              // 帧率
    int pixelFormat;            // 像素格式
    int64_t maxBitRate;         // 最大比特率
    bool  enableVideo;          // 是否允许视频录制

    // 音频参数
    int sampleRate;             // 采样率
    int channels;               // 声道数
    int sampleFormat;           // 采样格式
    bool enableAudio;           // 是否允许音频录制

    int cropX;                  // 裁剪起始x坐标
    int cropY;                  // 裁剪起始y坐标
    int cropWidth;              // 裁剪宽度
    int cropHeight;             // 裁剪高度
    int rotateDegree;           // 旋转角度
    int scaleWidth;             // 缩放宽度
    int scaleHeight;            // 缩放高度
    bool mirror;                // 是否需要镜像处理

public:
    RecordConfig();

    virtual ~RecordConfig();

    // 设置输出路径
    void setOutput(const char *url);

    // 设置视频参数
    void setVideoParams(int width, int height, int frameRate, int pixelFormat, int maxBitRate);

    // 设置音频参数
    void setAudioParams(int sampleRate, int sampleFormat, int channels);

    // 设置最大比特率
    void setMaxBitRate(int64_t maxBitRate);

    // 设置裁剪区域
    void setCrop(int cropX, int cropY, int cropW, int cropH);

    // 设置旋转角度
    void setRotate(int rotateDegree);

    // 设置缩放宽高
    void setScale(int scaleW, int scaleH);

    // 设置镜像处理
    void setMirror(bool mirror);
};

#endif  /* defined(__ANDROID__) */

#endif //RECORDCONFIG_H
