//
// Created by cain on 2018/2/8.
//

#ifndef CAINCAMERA_MUTEX_H
#define CAINCAMERA_MUTEX_H
#ifdef __cplusplus
extern "C" {
#endif

#include <pthread.h>
#include <stdint.h>

#define SDL_MUTEX_TIMEDOUT  1
#define SDL_MUTEX_MAXWAIT   (~(uint32_t)0)

// 互斥锁结构
typedef struct Mutex {
    pthread_mutex_t id;
} Mutex;

// 创建互斥锁
Mutex *Cain_CreateMutex(void);
// 销毁互斥锁
void Cain_DestroyMutex(Mutex *mutex);
// 销毁互斥锁指针
void Cain_DestroyMutexPointer(Mutex **pMutex);
// 上锁
int Cain_LockMutex(Mutex *mutex);
// 解锁
int Cain_UnlockMutex(Mutex *mutex);

// 条件锁结构
typedef struct Cond {
    pthread_cond_t id;
} Cond;

// 创建条件锁
Cond *Cain_CreateCond(void);
// 销毁条件锁
void Cain_DestroyCond(Cond *cond);
// 销毁条件锁指针
void Cain_DestroyCondPointer(Cond **pCond);
// 条件锁信号
int Cain_CondSignal(Cond *cond);
// 条件锁广播
int Cain_CondBroadcast(Cond *cond);
// 等待条件锁
int Cain_CondWait(Cond *cond, Mutex *mutex);
// 等待条件锁多少秒
int Cain_CondWaitTimeout(Cond *cond, Mutex *mutex, uint32_t msec);

// 出错信息
const char *Cain_GetError(void);

#ifdef __cplusplus
}
#endif
#endif //CAINCAMERA_MUTEX_H
