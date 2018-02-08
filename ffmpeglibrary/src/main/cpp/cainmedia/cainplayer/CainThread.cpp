//
// Created by Administrator on 2018/2/8.
//

#include <unistd.h>
#include <malloc.h>
#include "native_log.h"
#include "CainThread.h"

#ifdef __cplusplus
extern "C" {
#endif

#include <errno.h>
#include <assert.h>
#include <unistd.h>
#include <native_log.h>

/**
 * 线程函数
 * @param data
 * @return
 */
static void *CainRunThread(void *data) {
    CainThread *thread = (CainThread *) data;
    ALOGI("CainRunThread: [%d] %s\n", (int) gettid(), thread->name);
    pthread_setname_np(pthread_self(), thread->name);
    thread->retval = thread->func(thread->data);

    // TODO android jni detachthread

    return NULL;
}

/**
 * 创建线程
 * @param thread
 * @param fn
 * @param data
 * @param name
 * @return
 */
CainThread *Cain_CreateThread(CainThread *thread, int (*fun)(void *), void *data, const char *name) {
    thread->func = fun;
    thread->data = data;
    strlcpy(thread->name, name, sizeof(thread->name) - 1);
    // 创建线程
    int retval = pthread_create(&thread->id, NULL, CainRunThread, thread);
    if (retval) {
        return NULL;
    }
    return thread;
}


/**
 * 设置线程优先级
 * @param priority
 * @return
 */
int Cain_SetThreadPriority(CainThreadPriority priority) {
    struct sched_param sched;
    int policy;
    // 获取当前线程
    pthread_t thread = pthread_self();
    // 获取线程优先级参数
    if (pthread_getschedparam(thread, &policy, &sched) < 0) {
        ALOGE("call pthread_getschedparam() failed!\n");
        return -1;
    }
    if (priority == SDL_THREAD_PRIORITY_LOW) {
        sched.sched_priority = sched_get_priority_min(policy);
    } else if (priority == SDL_THREAD_PRIORITY_HIGH) {
        sched.sched_priority = sched_get_priority_max(policy);
    } else {
        int min_priority = sched_get_priority_min(policy);
        int max_priority = sched_get_priority_max(policy);
        sched.sched_priority = (min_priority + (max_priority - min_priority) / 2);
    }

    // 设置线程优先级
    if (pthread_setschedparam(thread, policy, &sched) < 0) {
        ALOGE("call pthread_setschedparam() failed");
        return -1;
    }
    return 0;
}

/**
 * 等待线程
 * @param thread
 * @param status
 */
void Cain_WaitThread(CainThread *thread, int *status) {
    assert(thread);
    if (!thread) {
        return;
    }
    // 等待线程结束
    pthread_join(thread->id, NULL);
    if (status) {
        *status = thread->retval;
    }
}

/**
 * 解绑线程
 * @param thread
 */
void Cain_DetachThread(CainThread *thread) {
    assert(thread);
    if (!thread) {
        return;
    }
    // 解绑线程
    pthread_detach(thread->id);
}

#ifdef __cplusplus
}
#endif