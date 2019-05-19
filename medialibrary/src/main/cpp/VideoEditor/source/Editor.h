//
// Created by CainHuang on 2019/2/26.
//

#ifndef EDITOR_H
#define EDITOR_H

#include "editor_log.h"

extern "C" {
#include <libavcodec/avcodec.h>
#include <libavfilter/avfilter.h>
#include <libavfilter/buffersink.h>
#include <libavfilter/buffersrc.h>
#include <libavformat/avformat.h>
#include <libavutil/audio_fifo.h>
#include <libavutil/avassert.h>
#include <libavutil/avstring.h>
#include <libavutil/dict.h>
#include <libavutil/imgutils.h>
#include <libavutil/opt.h>
#include <libavutil/samplefmt.h>
#include <libavutil/timestamp.h>
#include <libswresample/swresample.h>
#include <libswscale/swscale.h>
};

enum editor_event_type {
    EDITOR_PROCESSING = 1,
    EDITOR_ERROR = 100,
};


class Editor {
public:
    Editor();

    virtual ~Editor() {

    }

    virtual void reset() {

    }

    // 处理方法
    virtual int process() = 0;

protected:
    // 打开输入文件
    int openInputFile(const char *filename, AVFormatContext **fmt_ctx);
    // 获取解码上下文
    int getDecodeContext(AVFormatContext *fmt_ctx, AVCodecContext **dec_ctx, AVMediaType mediaType);
    // 获取音频解码上下文
    int getAudioDecodeContext(AVFormatContext *fmt_ctx, AVCodecContext **dec_ctx);
    // 获取视频解码上下文
    int getVideoDecodeContext(AVFormatContext *fmt_ctx, AVCodecContext **dec_ctx);
    // 解码一个数据包
    AVFrame *decodePacket(AVCodecContext *codec_ctx, AVPacket *packet);
    // 编码一帧数据
    AVPacket *encodeFrame(AVCodecContext *codec_ctx, AVFrame *frame);
    // 获取音频索引
    int getAudioIndex(AVFormatContext *fmt_ctx);
    // 获取视频索引
    int getVideoIndex(AVFormatContext *fmt_ctx);

    // 初始化输出上下文
    int initOutput(const char *url, AVFormatContext **fmt_ctx);
    // 初始化输出上下文，带格式
    int initOutput(const char *url, AVFormatContext **fmt_ctx, const char *format);
    // 添加输出视频流
    int addVideoStream(AVFormatContext *fmt_ctx, AVCodecContext **codec_ctx, AVCodecParameters codecpar);
    // 添加输出音频流
    int addAudioStream(AVFormatContext *fmt_ctx, AVCodecContext **codec_ctx, AVCodecParameters codecpar);
    // 写入文件头部信息
    int writeHeader(AVFormatContext *fmt_ctx, const char *url);
    // 写入文件尾部信息
    int writeTailer(AVFormatContext *fmt_ctx);
    // 获取输出音频流索引
    int getOutAudioIndex();
    // 获取输出视频流索引
    int getOutVideoIndex();

protected:
    int audio_index;
    int video_index;
    int out_audio_index;
    int out_video_index;
    int out_frame_rate;
    AVRational time_base;
};

#endif //EDITOR_H
