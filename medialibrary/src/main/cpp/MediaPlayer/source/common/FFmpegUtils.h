//
// Created by cain on 2018/12/29.
//

#ifndef FFMPEGUTILS_H
#define FFMPEGUTILS_H


#ifdef __cplusplus
extern "C" {
#endif

#include <libavutil/dict.h>
#include <libavutil/eval.h>
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavutil/opt.h>


AVDictionary *filterCodecOptions(AVDictionary *opts, enum AVCodecID codec_id,
                                 AVFormatContext *s, AVStream *st, AVCodec *codec);

// 检查媒体参数
int checkStreamSpecifier(AVFormatContext *s, AVStream *st, const char *spec);

// 设置媒体流额外参数
AVDictionary **setupStreamInfoOptions(AVFormatContext *s, AVDictionary *codec_opts);

// 打印出错信息
void printError(const char *filename, int err);

// 获取旋转角度
double getRotation(AVStream *st);

// 是否实时流
int isRealTime(AVFormatContext *s);

#ifdef __cplusplus
};
#endif

#endif //FFMPEGUTILS_H
