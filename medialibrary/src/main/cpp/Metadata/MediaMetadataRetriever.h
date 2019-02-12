//
// Created by cain on 2018/11/29.
//

#ifndef MEDIAMETADATARETRIEVER_H
#define MEDIAMETADATARETRIEVER_H

#include <cstdint>
#include <Mutex.h>
#include "Metadata.h"

extern "C" {
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>
#include <libavutil/dict.h>
#include <libavutil/opt.h>
#include <libavutil/imgutils.h>
};

typedef struct MetadataState {
    AVFormatContext *pFormatCtx;
    int             audioStreamIndex;
    int             videoStreamIndex;
    AVStream        *audioStream;
    AVStream        *videoStream;
    int             fd;
    int64_t         offset;
    const char      *headers;
    struct SwsContext *pSwsContext;
    AVCodecContext  *pCodecContext;

    struct SwsContext *pScaleSwsContext;
    AVCodecContext  *pScaleCodecContext;
} MetadataState;

struct AVDictionary {
    int count;
    AVDictionaryEntry *elements;
};

class MediaMetadataRetriever {
public:
    MediaMetadataRetriever();

    virtual ~MediaMetadataRetriever();

    // 释放资源
    void release();

    // 设置数据源
    int setDataSource(const char *url);

    status_t setDataSource(const char *url, int64_t offset, const char *headers);

    // 提取metadata数据
    const char *getMetadata(const char *key);

    // 提取Metadata数据
    const char *getMetadata(const char *key, int chapter);

    // 提取Metadat数据
    int getMetadata(AVDictionary **metadata);

    // 提取专辑/封面图片
    int getEmbeddedPicture(AVPacket *pkt);

    // 取得某个时刻的图像
    int getFrame(int64_t timeUs, AVPacket *pkt);

    // 取得某个时刻的图像
    int getFrame(int64_t timeus, AVPacket *pkt, int width, int height);

private:
    Mutex mLock;
    MetadataState *state;
    Metadata *mMetadata;

    // 内部处理方法
private:
    // 设置数据源
    int setDataSource(MetadataState **ps, const char *path, const char *headers);

    // 解析metadata数据
    const char* extractMetadata(MetadataState **ps, const char *key);

    // 解析metadata数据
    const char* extractMetadata(MetadataState **ps, const char *key, int chapter);

    // 获取metadata数据
    int getMetadata(MetadataState **ps, AVDictionary **metadata);

    // 获取专辑/封面图片
    int getCoverPicture(MetadataState **ps, AVPacket *pkt);

    // 获取视频帧
    int getFrame(MetadataState **ps, int64_t timeUs, AVPacket *pkt);

    // 获取视频帧
    int getFrame(MetadataState **ps, int64_t timeUs, AVPacket *pkt, int width, int height);

    // 释放资源
    void release(MetadataState **ps);

    // 初始化
    void init(MetadataState **ps);

    // 设置数据源
    int setDataSource(MetadataState **ps, const char* path);

    // 判断格式是否支持
    int formatSupport(int codec_id, int pix_fmt);

    // 初始化缩放转码上下文
    int initScaleContext(MetadataState *s, AVCodecContext *pCodecCtx, int width, int height);

    // 打开媒体流
    int openStream(MetadataState *s, int streamIndex);

    // 解码视频帧
    void decodeFrame(MetadataState *state, AVPacket *pkt, int *got_frame,
                     int64_t desired_frame_number, int width, int height);

    // 转码图片
    void encodeImage(MetadataState *state, AVCodecContext *pCodecCtx, AVFrame *pFrame,
                     AVPacket *packet, int *got_packet, int width, int height);
};


#endif //MEDIAMETADATARETRIEVER_H
