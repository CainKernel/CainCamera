//
// Created by cain on 2017/12/27.
//

#ifndef CAINCAMERA_COMMON_ENCODER_H
#define CAINCAMERA_COMMON_ENCODER_H


extern "C"
{
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
#include <libavutil/avassert.h>
#include <libavutil/channel_layout.h>
#include "libavutil/opt.h"
#include "libavutil/time.h"
#include "libavutil/avutil.h"
#include <libavutil/mathematics.h>
#include "libswscale/swscale.h"
#include "libswresample/swresample.h"
}

#include <jni.h>
#include <string>

typedef int32_t status_t;

// 状态
enum Status {
    OK                = 0,
    UNKNOWN_ERROR     = 0x80000000,
};

enum RecordState {
    // 录制状态
    RECORDER_IDLE     = 1,
    RECORDER_STARTED  = 2,
    RECORDER_STOPPED  = 3,
    RECORDER_RELEASED = 4,
};

// 帧类型
enum FrameType {
    YUV,
    PCM
};

#define MAX_STRING_PATH_LEN 1024

// 输出码流
typedef struct OutputStream {
    AVStream *st;
    AVCodecContext *enc;
    // 下一个pts
    int64_t next_pts;
    int samples_count;

    AVFrame *frame;
    AVFrame *tmp_frame;

    struct SwsContext *sws_ctx;
    struct SwrContext *swr_ctx;
} OutputStream;

typedef struct EncodeFrame {
    uint8_t  *data; // 记录帧数据
    FrameType type; // 记录帧的类型
    int size;       // 记录帧数据的size
};

#endif //CAINCAMERA_COMMON_ENCODER_H
