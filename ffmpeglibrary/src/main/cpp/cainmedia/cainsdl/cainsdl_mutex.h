//
// Created by Administrator on 2018/1/29.
//

#ifndef CAINCAMERA_CAINSDL_MUTEX_H
#define CAINCAMERA_CAINSDL_MUTEX_H

#include <stdint.h>
#include <pthread.h>

#define SDL_MUTEX_TIMEDOUT  1
#define SDL_MUTEX_MAXWAIT   (~(uint32_t)0)

// 同步锁结构
typedef struct SDL_mutex {
  pthread_mutex_t id;
} SDL_mutex;

// 创建同步锁
SDL_mutex *SDL_CreateMutex(void);
// 销毁同步锁
void SDL_DestroyMutex(SDL_mutex *mutex);
// 销毁同步锁指针
void SDL_DestroyMutexPointer(SDL_mutex **mutex);
// 上同步锁
int SDL_LockMutex(SDL_mutex *mutex);
// 解除同步锁
int SDL_UnlockMutex(SDL_mutex *mutex);


// 条件锁结构
typedef struct SDL_cond {
    pthread_cond_t id;
} SDL_cond;

// 创建条件锁
SDL_cond *SDL_CreateCond(void);
// 销毁条件锁
void SDL_DestroyCond(SDL_cond *cond);
// 销毁条件锁指针
void SDL_DestroyCondPointer(SDL_cond **cond);
// 条件锁信号
int SDL_CondSignal(SDL_cond *cond);
// 条件锁广播
int SDL_CondBroadcast(SDL_cond *cond);
// 等待条件锁
int SDL_CondWait(SDL_cond *cond, SDL_mutex *mutex);
// 等待条件锁多少秒
int SDL_CondWaitTimeout(SDL_cond *cond, SDL_mutex *mutex, uint32_t ms);

#endif //CAINCAMERA_CAINSDL_MUTEX_H
