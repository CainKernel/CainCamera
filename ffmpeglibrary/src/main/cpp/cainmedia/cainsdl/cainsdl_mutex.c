//
// Created by Administrator on 2018/1/29.
//

#include "cainsdl_mutex.h"
#include <errno.h>
#include <assert.h>
#include <sys/time.h>
#include <stdlib.h>
#include <memory.h>

/**
 * 创建同步锁
 * @return
 */
SDL_mutex *SDL_CreateMutex(void) {
    SDL_mutex *mutex;
    mutex = (SDL_mutex *) malloc(sizeof(SDL_mutex));
    if (!mutex) {
        return NULL;
    }
    memset(mutex, 0, sizeof(SDL_mutex));
    if (pthread_mutex_init(&mutex->id, NULL) != 0) {
        free(mutex);
        return NULL;
    }
    return mutex;
}

/**
 * 销毁同步锁
 * @param mutex
 */
void SDL_DestroyMutex(SDL_mutex *mutex) {
    if (mutex) {
        pthread_mutex_destroy(&mutex->id);
        free(mutex);
    }
}

/**
 * 销毁同步锁指针
 * @param mutex
 */
void SDL_DestroyMutexPointer(SDL_mutex **mutex) {
    if (mutex) {
        SDL_DestroyMutex(*mutex);
        *mutex = NULL;
    }
}

/**
 * 上同步锁
 * @param mutex
 * @return
 */
int SDL_LockMutex(SDL_mutex *mutex) {
    assert(mutex);
    if (!mutex) {
        return -1;
    }
    return pthread_mutex_lock(&mutex->id);
}

/**
 * 解除同步锁
 * @param mutex
 * @return
 */
int SDL_UnlockMutex(SDL_mutex *mutex) {
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
SDL_cond *SDL_CreateCond(void) {
    SDL_cond *cond;
    cond = (SDL_cond *) malloc(sizeof(SDL_cond));
    if (!cond) {
        return NULL;
    }
    memset(cond, 0, sizeof(SDL_cond));
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
void SDL_DestroyCond(SDL_cond *cond) {
    if (cond) {
        pthread_cond_destroy(&cond->id);
        free(cond);
    }
}

/**
 * 销毁条件锁指针
 * @param cond
 */
void SDL_DestroyCondPointer(SDL_cond **cond) {
    if (cond) {
        SDL_DestroyCond(*cond);
        *cond = NULL;
    }
}

/**
 * 条件锁信号
 * @param cond
 * @return
 */
int SDL_CondSignal(SDL_cond *cond) {
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
int SDL_CondBroadcast(SDL_cond *cond) {
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
int SDL_CondWait(SDL_cond *cond, SDL_mutex *mutex) {
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
int SDL_CondWaitTimeout(SDL_cond *cond, SDL_mutex *mutex, uint32_t ms) {
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



