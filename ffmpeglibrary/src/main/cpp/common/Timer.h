//
// Created by Administrator on 2018/2/27.
//

#ifndef CAINCAMERA_TIMER_H
#define CAINCAMERA_TIMER_H

#include <cstdint>
#include <bits/timespec.h>
#include <errno.h>
#include <time.h>

/**
 * 延时毫秒
 * @param ms
 */
static inline void DelayMs(uint32_t ms) {
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

/**
 * 获取系统当前时间
 * @return
 */
static inline long getCurrentTime() {
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return tv.tv_sec * 1000 + tv.tv_usec / 1000;
}

/**
 * 等待多少毫秒
 * @param timeout_ms
 * @return
 */
static inline timespec waitTime(long timeout_ms) {
    struct timespec abstime;
    struct timeval now;
    gettimeofday(&now, NULL);
    long nsec = now.tv_usec * 1000 + (timeout_ms % 1000) * 1000000;
    abstime.tv_sec = now.tv_sec + nsec / 1000000000 + timeout_ms / 1000;
    abstime.tv_nsec = nsec % 1000000000;
    return abstime;
}

#endif //CAINCAMERA_TIMER_H
