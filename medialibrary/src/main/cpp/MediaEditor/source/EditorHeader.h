//
// Created by CainHuang on 2019/8/11.
//

#ifndef EDITOR_HEADER_H
#define EDITOR_HEADER_H

#if defined(__ANDROID__)

#include <android/log.h>

#define JNI_TAG "MediaEditor"

#define LOGE(format, ...) __android_log_print(ANDROID_LOG_ERROR, JNI_TAG, format, ##__VA_ARGS__)
#define LOGI(format, ...) __android_log_print(ANDROID_LOG_INFO,  JNI_TAG, format, ##__VA_ARGS__)
#define LOGD(format, ...) __android_log_print(ANDROID_LOG_DEBUG, JNI_TAG, format, ##__VA_ARGS__)
#define LOGW(format, ...) __android_log_print(ANDROID_LOG_WARN,  JNI_TAG, format, ##__VA_ARGS__)
#define LOGV(format, ...) __android_log_print(ANDROID_LOG_VERBOSE,  JNI_TAG, format, ##__VA_ARGS__)

#else

#define LOGE(format, ...) {}
#define LOGI(format, ...) {}
#define LOGD(format, ...) {}
#define LOGW(format, ...) {}
#define LOGV(format, ...) {}

#endif


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
}
#endif

#endif //EDITOR_HEADER_H
