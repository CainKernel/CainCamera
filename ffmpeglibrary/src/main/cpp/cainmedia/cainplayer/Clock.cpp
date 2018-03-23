//
// Created by Administrator on 2018/3/23.
//

#include <math.h>
#ifdef __cplusplus
extern "C" {
#endif

#include <libavutil/time.h>

#ifdef __cplusplus
};
#endif

#include "Clock.h"

Clock::Clock() {
    speed = 1.0;
    mAbortRequest = false;
    setClock(NAN);
}

Clock::~Clock() {

}

/**
 * 获取时间
 * @return
 */
double Clock::getClock() {
    if (mAbortRequest) {
        return pts;
    } else {
        double time = av_gettime_relative() / 1000000.0;
        return pts_drift + time - (time - last_updated) * (1.0 - speed);
    }
}

/**
 * 设置时间
 * @param pts
 */
void Clock::setClock(double pts) {
    double time = av_gettime_relative() / 1000000.0;
    setClock(pts, time);
}

/**
 * 设置时间
 * @param pts
 * @param time
 */
void Clock::setClock(double pts, double time) {
    this->pts = pts;
    this->last_updated = time;
    pts_drift = pts - time;
}

/**
 * 设置速度
 * @param speed
 */
void Clock::setSpeed(double speed) {
    setClock(getClock());
    this->speed = speed;
}

/**
 * 同步到从属时钟
 * @param slave
 */
void Clock::syncToSlave(Clock *slave) {
    double clock = getClock();
    double slaveClock = slave->getClock();
    if (!isnan(slaveClock) && isnan(clock) || fabs(clock - slaveClock) > AV_NOSYNC_THRESHOLD) {
        setClock(slaveClock);
    }
}
