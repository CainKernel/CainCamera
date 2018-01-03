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
#include "libswscale/swscale.h"
#include "libswresample/swresample.h"
}

#include "safety_queue.cpp"
#include <jni.h>
#include <string>


#endif //CAINCAMERA_COMMON_ENCODER_H
