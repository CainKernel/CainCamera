//
// Created by Administrator on 2018/1/29.
//

#ifndef CAINCAMERA_CAINSDL_THREAD_H
#define CAINCAMERA_CAINSDL_THREAD_H

#include <stdint.h>
#include <pthread.h>

// 线程优先级
typedef enum {
    SDL_THREAD_PRIORITY_LOW,
    SDL_THREAD_PRIORITY_NORMAL,
    SDL_THREAD_PRIORITY_HIGH
} SDL_ThreadPriority;

// 线程结构
typedef struct SDL_Thread
{
    pthread_t id;
    int (*func)(void *);
    void *data;
    char name[32];
    int retval;
} SDL_Thread;

// 创建线程
SDL_Thread *SDL_CreateThread(SDL_Thread *thread, int (*fun)(void *), void *data, const char *name);
// 设置线程优先级
int SDL_SetThreadPriority(SDL_ThreadPriority priority);
// 等待线程
void SDL_WaitThread(SDL_Thread *thread, int *status);
// 解绑线程
void SDL_DetachThread(SDL_Thread *thread);

#endif //CAINCAMERA_CAINSDL_THREAD_H
