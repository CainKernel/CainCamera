//
// Created by Administrator on 2018/2/27.
//

#include "Clock.h"
#include "CainPlayerDefinition.h"

Clock::Clock(int *queue_serial) {
    this->speed = 1.0;
    this->paused = 0;
    this->queue_serial = queue_serial;
    setClock(NAN, -1);
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

void Clock::setClock(double pts, int serial, double time) {
    this->pts = pts;
    this->last_updated = time;
    this->pts_drift = this->pts - time;
    this->serial = serial;
}

void Clock::setClock(double pts, int serial) {
    double time = av_gettime_relative() / 1000000.0;
    setClock(pts, serial, time);
}

void Clock::setSpeed(double speed) {
    setClock(getClock(), serial);
    this->speed = speed;
}

void Clock::syncToSlave(Clock *slave) {
    double clock = getClock();
    double slave_clock = slave->getClock();
    if (!isnan(slave_clock) && (isnan(clock) || fabs(clock - slave_clock) > AV_NOSYNC_THRESHOLD)) {
        setClock(slave_clock, slave->serial);
    }
}


