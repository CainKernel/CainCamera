//
// Created by CainHuang on 2019/8/17.
//

#ifndef AVMEDIAHEADER_H
#define AVMEDIAHEADER_H

#if defined(__ANDROID__)

#include <android/log.h>

#define JNI_TAG "CainMedia"

#define LOGE(format, ...) __android_log_print(ANDROID_LOG_ERROR,   JNI_TAG, format, ##__VA_ARGS__)
#define LOGI(format, ...) __android_log_print(ANDROID_LOG_INFO,    JNI_TAG, format, ##__VA_ARGS__)
#define LOGD(format, ...) __android_log_print(ANDROID_LOG_DEBUG,   JNI_TAG, format, ##__VA_ARGS__)
#define LOGW(format, ...) __android_log_print(ANDROID_LOG_WARN,    JNI_TAG, format, ##__VA_ARGS__)
#define LOGV(format, ...) __android_log_print(ANDROID_LOG_VERBOSE, JNI_TAG, format, ##__VA_ARGS__)

#else

#define LOGE(format, ...) {}
#define LOGI(format, ...) {}
#define LOGD(format, ...) {}
#define LOGW(format, ...) {}
#define LOGV(format, ...) {}

#endif /* defined(__ANDROID__) */

#include <cstdint>

#include <libyuv.h>
#include <Thread.h>

#ifdef __cplusplus
extern "C" {
#endif

#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavformat/avio.h>
#include <libavfilter/avfilter.h>
#include <libavfilter/avfiltergraph.h>
#include <libavfilter/buffersink.h>
#include <libavfilter/buffersrc.h>
#include <libavutil/audio_fifo.h>
#include <libavutil/avutil.h>
#include <libavutil/error.h>
#include <libavutil/frame.h>
#include <libavutil/imgutils.h>
#include <libavutil/mathematics.h>
#include <libavutil/opt.h>
#include <libavutil/pixdesc.h>
#include <libavutil/pixfmt.h>
#include <libavutil/samplefmt.h>
#include <libavutil/timestamp.h>
#include <libavutil/time.h>
#include <libswscale/swscale.h>
#include <libswresample/swresample.h>
#include <libavutil/avstring.h>
#include <libavutil/eval.h>
#include <libavutil/display.h>
#include <libavutil/pixfmt.h>

#ifdef __cplusplus
};
#endif

#include <string>

#define AUDIO_MIN_BUFFER_SIZE 512

#define AUDIO_MAX_CALLBACKS_PER_SEC 30

// 获取当前时钟(ms)
inline uint64_t getCurrentTimeMs() {
    struct timeval tv;
    gettimeofday(&tv, nullptr);
    uint64_t us = (uint64_t) (tv.tv_sec) * 1000 * 1000 + (uint64_t) (tv.tv_usec);
    return us / 1000;
}

#endif //AVMEDIAHEADER_H
