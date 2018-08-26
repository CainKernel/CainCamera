//
// Created by Administrator on 2018/2/24.
//

#ifndef CAINCAMERA_THREAD_H
#define CAINCAMERA_THREAD_H

#ifdef __cplusplus
extern "C" {

#endif

#include "Mutex.h"

// 线程优先级
typedef enum {
    THREAD_PRIORITY_LOW,
    THREAD_PRIORITY_NORMAL,
    THREAD_PRIORITY_HIGH
} ThreadPriority;

// 线程结构
typedef struct Thread
{
    pthread_t id;
    int (*func)(void *);
    void *data;
    char name[32];
    int retval;
} Thread;


// 创建线程
Thread *ThreadCreate(int (*fun)(void *), void *data, const char *name);
// 设置线程优先级
int ThreadSetPriority(ThreadPriority priority);
// 等待线程
void ThreadWait(Thread *thread, int *status);
// 解绑线程
void ThreadDetach(Thread *thread);

// 销毁线程
void ThreadDestroy(Thread *thread);

#ifdef __cplusplus
};
#endif

#endif //CAINCAMERA_THREAD_H
