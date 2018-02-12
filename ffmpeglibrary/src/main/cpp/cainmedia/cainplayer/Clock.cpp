//
// Created by Administrator on 2018/2/12.
//

#include <math.h>
#include "Clock.h"

Clock::Clock(int *queue_serial) {
    init(queue_serial);
}

double Clock::getClock() {
    if (*queue_serial != serial) {
        return NAN;
    }
    if (paused) {
        return pts;
    } else {
        double time = av_gettime_relative() / 1000000.0;
        return pts_drift + time - (time - last_updated) * (1.0 - speed);
    }
}

void Clock::setClockAt(double pts, int serial, double time) {
    this->pts = pts;
    this->last_updated = time;
    this->pts_drift = pts - time;
    this->serial = serial;
}


void Clock::setClock(double pts, int serial) {
    double time = av_gettime_relative() / 1000000.0;
    setClockAt(pts, serial, time);
}

void Clock::setClockSpeed(double speed) {
    setClock(getClock(), serial);
    this->speed = speed;
}

void Clock::init(int *queue_serial) {
    speed = 1.0;
    paused = 0;
    this->queue_serial = queue_serial;
    setClock(NAN, -1);
}

void Clock::syncToSlave(Clock *slave) {
    double  clock = getClock();
    double slaveClock = slave->getClock();
    if (!isnan(slaveClock) && (isnan(clock) || fabs(clock - slaveClock) > AV_NOSYNC_THRESHOLD)) {
        setClock(slaveClock, slave->serial);
    }
}