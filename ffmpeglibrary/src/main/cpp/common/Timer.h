//
// Created by Administrator on 2018/2/27.
//

#ifndef CAINCAMERA_TIMER_H
#define CAINCAMERA_TIMER_H

#include <cstdint>
#include <bits/timespec.h>
#include <errno.h>
#include <time.h>

static void DelayMs(uint32_t ms) {
    int was_error;
    struct timespec elapsed, tv;
    elapsed.tv_sec = (time_t)(ms / 1000);
    elapsed.tv_nsec = (long)(ms % 1000) * 1000000;
    do {
        errno = 0;
        tv.tv_sec = elapsed.tv_sec;
        tv.tv_nsec = elapsed.tv_nsec;
        was_error = nanosleep(&tv, &elapsed);
    } while (was_error && (errno == EINTR));
}

#endif //CAINCAMERA_TIMER_H
