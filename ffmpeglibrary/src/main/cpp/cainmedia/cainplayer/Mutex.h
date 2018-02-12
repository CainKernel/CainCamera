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

#define MUTEX_TIMEDOUT  1
#define MUTEX_MAXWAIT   (~(uint32_t)0)

// 互斥锁结构
typedef struct Mutex {
    pthread_mutex_t id;
} Mutex;

// 创建互斥锁
Mutex *MutexCreate(void);
// 销毁互斥锁
void MutexDestroy(Mutex *mutex);
// 销毁互斥锁指针
void MutexDestroyPointer(Mutex **pMutex);
// 上锁
int MutexLock(Mutex *mutex);
// 解锁
int MutexUnlock(Mutex *mutex);

// 条件锁结构
typedef struct Cond {
    pthread_cond_t id;
} Cond;

// 创建条件锁
Cond *CondCreate(void);
// 销毁条件锁
void CondDestroy(Cond *cond);
// 销毁条件锁指针
void CondDestroyPointer(Cond **pCond);
// 条件锁信号
int CondSignal(Cond *cond);
// 条件锁广播
int CondBroadcast(Cond *cond);
// 等待条件锁
int CondWait(Cond *cond, Mutex *mutex);
// 等待条件锁多少秒
int CondWaitTimeout(Cond *cond, Mutex *mutex, uint32_t msec);

// 出错信息
const char *GetError(void);

#ifdef __cplusplus
}
#endif
#endif //CAINCAMERA_MUTEX_H
