//
// Created by Administrator on 2018/2/12.
//

#ifndef CAINCAMERA_PLAYERDEFINTION_H
#define CAINCAMERA_PLAYERDEFINTION_H

#ifdef __cplusplus
extern "C" {
#endif

#include <inttypes.h>
#include <math.h>
#include <limits.h>
#include <signal.h>
#include <stdint.h>

#include "libavutil/avstring.h"
#include "libavutil/eval.h"
#include "libavutil/mathematics.h"
#include "libavutil/pixdesc.h"
#include "libavutil/imgutils.h"
#include "libavutil/dict.h"
#include "libavutil/parseutils.h"
#include "libavutil/samplefmt.h"
#include "libavutil/avassert.h"
#include "libavutil/time.h"
#include "libavformat/avformat.h"
#include "libswscale/swscale.h"
#include "libavutil/opt.h"
#include "libavcodec/avfft.h"
#include "libswresample/swresample.h"

#ifdef __cplusplus
}
#endif

#include "native_log.h"
#include "Mutex.h"

#define MAX_QUEUE_SIZE (15 * 1024 * 1024)
#define MIN_FRAMES 25
#define EXTERNAL_CLOCK_MIN_FRAMES 2
#define EXTERNAL_CLOCK_MAX_FRAMES 10

/* Minimum SDL audio buffer size, in samples. */
// 最小音频缓冲
#define SDL_AUDIO_MIN_BUFFER_SIZE 512
/* Calculate actual buffer size keeping in mind not cause too frequent audio callbacks */
// 计算实际音频缓冲大小，并不需要太频繁回调，这里设置的是最大音频回调次数是每秒30次
#define SDL_AUDIO_MAX_CALLBACKS_PER_SEC 30

/* Step size for volume control in dB */
// 音频控制 以db为单位的步进
#define SDL_VOLUME_STEP (0.75)

/* no AV sync correction is done if below the minimum AV sync threshold */
// 最低同步阈值，如果低于该值，则不需要同步校正
#define AV_SYNC_THRESHOLD_MIN 0.04
/* AV sync correction is done if above the maximum AV sync threshold */
// 最大同步阈值，如果大于该值，则需要同步校正
#define AV_SYNC_THRESHOLD_MAX 0.1
/* If a frame duration is longer than this, it will not be duplicated to compensate AV sync */
// 帧补偿同步阈值，如果帧持续时间比这更长，则不用来补偿同步
#define AV_SYNC_FRAMEDUP_THRESHOLD 0.1
/* no AV correction is done if too big error */
// 同步阈值。如果误差太大，则不进行校正
#define AV_NOSYNC_THRESHOLD 10.0

/* maximum audio speed change to get correct sync */
// 正确同步的最大音频速度变化值(百分比)
#define SAMPLE_CORRECTION_PERCENT_MAX 10

/* external clock speed adjustment constants for realtime sources based on buffer fullness */
// 根据实时码流的缓冲区填充时间做外部时钟调整
// 最小值
#define EXTERNAL_CLOCK_SPEED_MIN  0.900
// 最大值
#define EXTERNAL_CLOCK_SPEED_MAX  1.010
// 步进
#define EXTERNAL_CLOCK_SPEED_STEP 0.001

/* we use about AUDIO_DIFF_AVG_NB A-V differences to make the average */
// 使用差值来实现平均值
#define AUDIO_DIFF_AVG_NB   20

/* polls for possible required screen refresh at least this often, should be less than 1/fps */
// 刷新频率 应该小于 1/fps
#define REFRESH_RATE 0.01

/* NOTE: the size must be big enough to compensate the hardware audio buffersize size */
/* TODO: We assume that a decoded and resampled frame fits into this buffer */
// 采样大小
#define SAMPLE_ARRAY_SIZE (8 * 65536)
#define FRAME_QUEUE_SIZE 16

// 待解码包列表结构体
typedef struct MyAVPacketList {
    AVPacket pkt;                               // 待解码包对象
    struct MyAVPacketList *next;                // 下一个待解码包指针
    int serial;                                 // 序列标志
} MyAVPacketList;

/**
 * 解码帧结构体
 */
typedef struct Frame {
    AVFrame *frame;                              // 帧数据
    int serial;                                  // 序列
    double pts;                                  // 帧的显示时间戳
    double duration;                             // 帧显示时长
    int64_t pos;                                 // 文件中的位置
    int width;                                   // 帧的宽度
    int height;                                  // 帧的高度
    int format;                                  // 格式
    AVRational sar;                              // 额外参数
    int uploaded;                                // 上载
    int flip_v;                                  // 反转
} Frame;

#endif //CAINCAMERA_PLAYERDEFINTION_H
