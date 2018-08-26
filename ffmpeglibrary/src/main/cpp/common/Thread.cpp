//
// Created by Administrator on 2018/2/24.
//

#include "AndroidLog.h"
#include "Thread.h"

#ifdef __cplusplus
extern "C" {
#endif

#include <unistd.h>
#include <malloc.h>
#include <memory.h>
#include <errno.h>
#include <assert.h>

/**
 * 线程函数
 * @param data
 * @return
 */
static void *ThreadRun(void *data) {
    Thread *thread = (Thread *) data;
    ALOGI("ThreadRun: [%d] %s\n", (int) gettid(), thread->name);
    pthread_setname_np(pthread_self(), thread->name);
    thread->retval = thread->func(thread->data);
    ThreadDetach(thread);
    return NULL;
}

/**
 * 创建线程
 * @param fn
 * @param data
 * @param name
 * @return
 */
Thread *ThreadCreate(int (*fun)(void *), void *data, const char *name) {

    Thread *thread = (Thread *) malloc(sizeof(Thread));
    thread->func = fun;
    thread->data = data;
    strlcpy(thread->name, name, sizeof(thread->name) - 1);
    // 创建线程
    int retval = pthread_create(&thread->id, NULL, ThreadRun, thread);
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
int ThreadSetPriority(ThreadPriority priority) {
    struct sched_param sched;
    int policy;
    // 获取当前线程
    pthread_t thread = pthread_self();
    // 获取线程优先级参数
    if (pthread_getschedparam(thread, &policy, &sched) < 0) {
        ALOGE("call pthread_getschedparam() failed!\n");
        return -1;
    }
    if (priority == THREAD_PRIORITY_LOW) {
        sched.sched_priority = sched_get_priority_min(policy);
    } else if (priority == THREAD_PRIORITY_HIGH) {
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
void ThreadWait(Thread *thread, int *status) {
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
void ThreadDetach(Thread *thread) {
    assert(thread);
    if (!thread) {
        return;
    }
    // 解绑线程
    pthread_detach(thread->id);
}

/**
 * 销毁线程
 * @param thread
 */
void ThreadDestroy(Thread * thread) {
    if (thread) {
        ThreadWait(thread, NULL);
        free(thread);
        thread = NULL;
    }
}

#ifdef __cplusplus
}
#endif