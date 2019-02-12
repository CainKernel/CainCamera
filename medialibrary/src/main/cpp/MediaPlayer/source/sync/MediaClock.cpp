//
// Created by cain on 2018/12/30.
//
#include <player/PlayerState.h>
#include "MediaClock.h"

MediaClock::MediaClock() {
    init();
}

MediaClock::~MediaClock() {

}

void MediaClock::init() {
    speed = 1.0;
    paused = 0;
    setClock(NAN);
}

double MediaClock::getClock() {
    if (paused) {
        return pts;
    } else {
        double time = av_gettime_relative() / 1000000.0;
        return pts_drift + time - (time - last_updated) * (1.0 - speed);
    }
}

void MediaClock::setClock(double pts, double time) {
    this->pts = pts;
    this->last_updated = time;
    this->pts_drift = this->pts - time;
}

void MediaClock::setClock(double pts) {
    double time = av_gettime_relative() / 1000000.0;
    setClock(pts, time);
}

void MediaClock::setSpeed(double speed) {
    setClock(getClock());
    this->speed = speed;
}

void MediaClock::syncToSlave(MediaClock *slave) {
    double clock = getClock();
    double slave_clock = slave->getClock();
    if (!isnan(slave_clock) && (isnan(clock) || fabs(clock - slave_clock) > AV_NOSYNC_THRESHOLD)) {
        setClock(slave_clock);
    }
}

double MediaClock::getSpeed() const {
    return speed;
}

