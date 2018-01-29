//
// Created by Administrator on 2018/1/29.
//

#include "cainsdl_timer.h"
#include <unistd.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>
#include <sys/time.h>
/**
 * 延时
 * @param ms
 */
void SDL_Delay(uint32_t ms) {
    int error;
    struct timespec elapsed, tv;

    elapsed.tv_sec = (__kernel_time_t) (ms / 1000);
    elapsed.tv_nsec = (long) ((ms % 1000) * 1000000);
    do {
        tv.tv_sec = elapsed.tv_sec;
        tv.tv_nsec = elapsed.tv_nsec;
        error = nanosleep(&tv, &elapsed);
    } while (error);
}