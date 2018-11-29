//
// Created by cain on 2018/11/29.
//

#ifndef CAINPLAYER_METADATA_H
#define CAINPLAYER_METADATA_H

extern "C" {
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>
#include <libavutil/opt.h>
};

#include <stdio.h>

class Metadata {
public:
    Metadata();

    virtual ~Metadata();

    // 设置网络广播流metadata数据
    void setShoutcastMetadata(AVFormatContext *pFormatCtx);

    // 设置时长
    void setDuration(AVFormatContext *pFormatCtx);

    // 设置编解码器信息
    void setCodec(AVFormatContext *pFormatCtx, int streamIndex);

    // 设置旋转角度
    void setRotation(AVFormatContext *pFormatCtx, AVStream *audioStream, AVStream *videoStream);

    // 设置帧率
    void setFrameRate(AVFormatContext *pFormatCtx, AVStream *audioStream, AVStream *videoStream);

    // 设置文件大小
    void setFileSize(AVFormatContext *pFormatCtx);

    // 设置Chapter数量
    void setChapterCount(AVFormatContext *pFormatCtx);

    // 设置视频尺寸
    void setVideoSize(AVFormatContext *pFormatCtx, AVStream *videoStream);

    // 解码metadata
    const char* extractMetadata(AVFormatContext *pFormatCtx, AVStream *audioStream, AVStream *videoStream,
                                const char *key);
    // 解码metadata
    const char* extractMetadata(AVFormatContext *pFormatCtx, AVStream *audioStream, AVStream *videoStream,
                                const char *key, int chapter);

    // 提取metadata
    int getMetadata(AVFormatContext *pFormatCtx, AVDictionary **metadata);
};


#endif //CAINPLAYER_METADATA_H
