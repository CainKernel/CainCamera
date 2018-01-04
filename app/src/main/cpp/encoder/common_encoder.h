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
#include "libavutil/opt.h"
#include "libavutil/time.h"
#include "libavutil/avutil.h"
#include "libswscale/swscale.h"
#include "libswresample/swresample.h"
}

#include "safety_queue.cpp"
#include <jni.h>
#include <string>

#define FRAME_YUV 1
#define FRAME_PCM 2

// 录制状态
#define RECORDER_IDLE    0
#define RECORDER_STARTED 1
#define RECORDER_STOPPED 2
#define RECORDER_RELEASE 3

#endif //CAINCAMERA_COMMON_ENCODER_H
