//
// Created by Administrator on 2018/2/27.
//

#ifndef CAINCAMERA_CLOCK_H
#define CAINCAMERA_CLOCK_H

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

#include <math.h>

class Clock {
public:
    double pts;					// 时钟基准
    double pts_drift;			// 更新时钟的差值
    double last_updated;		// 上一次更新的时间
    double speed;				// 速度
    int serial;				    // 时钟基于当前裸数据包序列
    int paused;					// 停止标志
    int *queue_serial;			// 指向当前数据包队列序列的指针，用于过时的时钟检测

    Clock(int *queue_serial);

    // 获取时钟
    double getClock(void);
    // 设置时钟
    void setClock(double pts, int serial);
    // 设置时钟
    void setClock(double pts, int serial, double time);
    // 设置时钟速度
    void setSpeed(double speed);
    // 同步从属时钟
    void syncToSlave(Clock *slave);
};


#endif //CAINCAMERA_CLOCK_H
