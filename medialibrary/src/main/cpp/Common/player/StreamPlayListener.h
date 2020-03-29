//
// Created by erenhuang on 2020-02-27.
//

#ifndef STREAMPLAYLISTENER_H
#define STREAMPLAYLISTENER_H

#ifdef __cplusplus
extern "C" {
#endif

#include <libavutil/avutil.h>

#ifdef __cplusplus
};
#endif

class StreamPlayListener {
public:
    virtual ~StreamPlayListener() = default;

    virtual void onPrepared(AVMediaType type) = 0;

    virtual void onPlaying(AVMediaType type, float pts) = 0;

    virtual void onSeekComplete(AVMediaType type) = 0;

    virtual void onCompletion(AVMediaType type) = 0;

    virtual void onError(AVMediaType type, int errorCode, const char *msg) = 0;
};

/**
 * 播放器操作类型
 */
enum player_message_type {
    MSG_FLUSH                   = 0x00,     // 默认
    MSG_ERROR                   = 0x10,     // 出错回调
    MSG_PREPARED                = 0x20,     // 准备完成回调
    MSG_STARTED                 = 0x30,     // 已经开始
    MSG_COMPLETED               = 0x40,     // 播放完成回调
    MSG_OPEN_INPUT              = 0x50,     // 打开文件
    MSG_FIND_STREAM_INFO        = 0x51,     // 查找媒体流信息
    MSG_PREPARE_DECODER         = 0x52,     // 准备解码器
    MSG_VIDEO_SIZE_CHANGED      = 0x53,     // 视频大小变化
    MSG_SAR_CHANGED             = 0x54,     // 长宽比变化
    MSG_AUDIO_START             = 0x55,     // 音频解码开始
    MSG_AUDIO_RENDERING_START   = 0x56,     // 音频播放开始
    MSG_VIDEO_START             = 0x57,     // 视频解码开始
    MSG_VIDEO_RENDERING_START   = 0x58,     // 视频渲染开始
    MSG_VIDEO_ROTATION_CHANGED  = 0x59,     // 旋转角度变化
    MSG_BUFFERING_START         = 0x60,     // 缓冲开始
    MSG_BUFFERING_END           = 0x61,     // 缓冲结束
    MSG_BUFFERING_UPDATE        = 0x62,     // 缓冲更细你

    MSG_SEEK_COMPLETE           = 0x70,     // 跳转完成

    MSG_REQUEST_PREPARE         = 0x200,    // 准备播放器
    MSG_REQUEST_START           = 0x201,    // 开始播放器
    MSG_REQUEST_PAUSE           = 0x202,    // 暂停播放器
    MSG_REQUEST_STOP            = 0x203,    // 停止播放器
    MSG_REQUEST_SEEK            = 0x204,    // 跳转到某个位置

    MSG_CURRENT_POSITION        = 0x300,    // 当前时钟
};

#endif //STREAMPLAYLISTENER_H
