//
// Created by Administrator on 2018/2/8.
//

#ifndef CAINCAMERA_CAINTHREAD_H
#define CAINCAMERA_CAINTHREAD_H

#ifdef __cplusplus
extern "C" {

#endif

#include "CainMutex.h"

// 线程优先级
typedef enum {
    SDL_THREAD_PRIORITY_LOW,
    SDL_THREAD_PRIORITY_NORMAL,
    SDL_THREAD_PRIORITY_HIGH
} CainThreadPriority;

// 线程结构
typedef struct CainThread
{
    pthread_t id;
    int (*func)(void *);
    void *data;
    char name[32];
    int retval;
} CainThread;


// 创建线程
CainThread *Cain_CreateThread(CainThread *thread, int (*fun)(void *), void *data, const char *name);
// 设置线程优先级
int Cain_SetThreadPriority(CainThreadPriority priority);
// 等待线程
void Cain_WaitThread(CainThread *thread, int *status);
// 解绑线程
void Cain_DetachThread(CainThread *thread);

#ifdef __cplusplus
};
#endif

#endif //CAINCAMERA_CAINTHREAD_H

