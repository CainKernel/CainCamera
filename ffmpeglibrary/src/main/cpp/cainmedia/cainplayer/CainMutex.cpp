//
// Created by cain on 2018/2/8.
//


#include "CainMutex.h"

#ifdef __cplusplus
extern "C" {
#endif

#include <errno.h>
#include <assert.h>
#include <sys/time.h>
#include <stdlib.h>
#include <memory.h>

/**
 * 创建互斥锁
 * @return
 */
Mutex *Cain_CreateMutex(void) {
    Mutex *mutex;
    mutex = (Mutex *) malloc(sizeof(Mutex));
    if (!mutex) {
        return NULL;
    }
    memset(mutex, 0, sizeof(Mutex));
    if (pthread_mutex_init(&mutex->id, NULL) != 0) {
        free(mutex);
        return NULL;
    }
    return mutex;
}

/**
 * 销毁互斥锁
 * @param mutex
 */
void Cain_DestroyMutex(Mutex *mutex) {
    if (mutex) {
        pthread_mutex_destroy(&mutex->id);
        free(mutex);
    }
}

/**
 * 销毁互斥锁指针
 * @param mutex
 */
void Cain_DestroyMutexPointer(Mutex **mutex) {
    if (mutex) {
        Cain_DestroyMutex(*mutex);
        *mutex = NULL;
    }
}

/**
 * 上互斥锁
 * @param mutex
 * @return
 */
int Cain_LockMutex(Mutex *mutex) {
    assert(mutex);
    if (!mutex) {
        return -1;
    }
    return pthread_mutex_lock(&mutex->id);
}

/**
 * 解除互斥锁
 * @param mutex
 * @return
 */
int Cain_UnlockMutex(Mutex *mutex) {
    assert(mutex);
    if (!mutex) {
        return -1;
    }
    return pthread_mutex_unlock(&mutex->id);
}



/**
 * 创建条件锁
 * @return
 */
Cond *Cain_CreateCond(void) {
    Cond *cond;
    cond = (Cond *) malloc(sizeof(Cond));
    if (!cond) {
        return NULL;
    }
    memset(cond, 0, sizeof(Cond));
    if (pthread_cond_init(&cond->id, NULL) != 0) {
        free(cond);
        return NULL;
    }
    return cond;
}


/**
 * 销毁条件锁
 * @param cond
 */
void Cain_DestroyCond(Cond *cond) {
    if (cond) {
        pthread_cond_destroy(&cond->id);
        free(cond);
    }
}

/**
 * 销毁条件锁指针
 * @param cond
 */
void Cain_DestroyCondPointer(Cond **cond) {
    if (cond) {
        Cain_DestroyCond(*cond);
        *cond = NULL;
    }
}

/**
 * 条件锁信号
 * @param cond
 * @return
 */
int Cain_CondSignal(Cond *cond) {
    assert(cond);
    if (!cond) {
        return -1;
    }
    return pthread_cond_signal(&cond->id);
}

/**
 * 条件锁广播，用于唤醒多个条件变量
 * @param cond
 * @return
 */
int Cain_CondBroadcast(Cond *cond) {
    assert(cond);
    if (!cond) {
        return -1;
    }
    return pthread_cond_broadcast(&cond->id);
}

/**
 * 等待条件锁
 * @param cond
 * @param mutex
 * @return
 */
int Cain_CondWait(Cond *cond, Mutex *mutex) {
    assert(cond);
    assert(mutex);
    if (!cond || !mutex) {
        return -1;
    }

    return pthread_cond_wait(&cond->id, &mutex->id);
}

/**
 * 等待条件锁多少秒
 * @param cond
 * @param mutex
 * @param ms
 * @return
 */
int Cain_CondWaitTimeout(Cond *cond, Mutex *mutex, uint32_t ms) {
    int retval;
    struct timeval delta;
    struct timespec abstime;

    assert(cond);
    assert(mutex);
    if (!cond || !mutex) {
        return -1;
    }

    gettimeofday(&delta, NULL);

    abstime.tv_sec = delta.tv_sec + (time_t)(ms / 1000);
    abstime.tv_nsec = (time_t) (delta.tv_usec + (ms % 1000) * 1000) * 1000;
    if (abstime.tv_nsec > 1000000000) {
        abstime.tv_sec++;
        abstime.tv_nsec -= 1000000000;
    }

    while (1) {
        retval = pthread_cond_timedwait(&cond->id, &mutex->id, &abstime);
        if (retval == 0) {
            return 0;
        } else if (retval == EINTR) {
            continue;
        } else if (retval == ETIMEDOUT) {
            return SDL_MUTEX_TIMEDOUT;
        } else {
            break;
        }
    }
    return -1;
}

/**
 * 出错信息
 * @return
 */
const char *Cain_GetError(void) {
    return NULL;
}

#ifdef __cplusplus
}
#endif