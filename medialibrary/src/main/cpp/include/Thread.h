//
// Created by cain on 2018/12/28.
//

#ifndef THREAD_H
#define THREAD_H

#include <Mutex.h>
#include <Condition.h>

typedef enum {
    Priority_Default = -1,
    Priority_Low = 0,
    Priority_Normal = 1,
    Priority_High = 2
} ThreadPriority;

class Runnable {
public:
    virtual ~Runnable(){}

    virtual void run() = 0;
};

/**
 * Thread can use a custom Runnable, but must delete Runnable constructor yourself
 */
class Thread : public Runnable {
public:

    Thread();

    Thread(ThreadPriority priority);

    Thread(Runnable *runnable);

    Thread(Runnable *runnable, ThreadPriority priority);

    virtual ~Thread();

    void start();

    void join();

    void detach();

    pthread_t getId() const;

    bool isActive() const;

protected:
    static void *threadEntry(void *arg);

    int schedPriority(ThreadPriority priority);

    virtual void run();

protected:
    Mutex mMutex;
    Condition mCondition;
    Runnable *mRunnable;
    ThreadPriority mPriority; // thread priority
    pthread_t mId;  // thread id
    bool mRunning;  // thread running
    bool mNeedJoin; // if call detach function, then do not call join function
};

inline Thread::Thread() {
    mNeedJoin = true;
    mRunning = false;
    mId = -1;
    mRunnable = NULL;
    mPriority = Priority_Default;
}

inline Thread::Thread(ThreadPriority priority) {
    mNeedJoin = true;
    mRunning = false;
    mId = -1;
    mRunnable = NULL;
    mPriority = priority;
}

inline Thread::Thread(Runnable *runnable) {
    mNeedJoin = false;
    mRunning = false;
    mId = -1;
    mRunnable = runnable;
    mPriority = Priority_Default;
}

inline Thread::Thread(Runnable *runnable, ThreadPriority priority) {
    mNeedJoin = false;
    mRunning = false;
    mId = -1;
    mRunnable = runnable;
    mPriority = priority;
}

inline Thread::~Thread() {
    join();
    mRunnable = NULL;
}

inline void Thread::start() {
    if (!mRunning) {
        pthread_create(&mId, NULL, threadEntry, this);
        mNeedJoin = true;
    }
    // wait thread to run
    mMutex.lock();
    while (!mRunning) {
        mCondition.wait(mMutex);
    }
    mMutex.unlock();
}

inline void Thread::join() {
    if (mId == -1) {
        return;
    }
    Mutex::Autolock lock(mMutex);
    if (mId != -1 && mNeedJoin) {
        pthread_join(mId, NULL);
    }
    mNeedJoin = false;
    mId = -1;
}

inline  void Thread::detach() {
    Mutex::Autolock lock(mMutex);
    if (mId >= 0) {
        pthread_detach(mId);
        mNeedJoin = false;
    }
}

inline pthread_t Thread::getId() const {
    return mId;
}

inline bool Thread::isActive() const {
    return mRunning;
}

inline void* Thread::threadEntry(void *arg) {
    Thread *thread = (Thread *) arg;

    if (thread != NULL) {
        thread->mRunning = true;
        thread->mCondition.signal();

        thread->schedPriority(thread->mPriority);

        // when runnable is exit，run runnable else run()
        if (thread->mRunnable) {
            thread->mRunnable->run();
        } else {
            thread->run();
        }

        thread->mRunning = false;
        thread->mCondition.signal();
    }

    pthread_exit(NULL);

    return NULL;
}

inline int Thread::schedPriority(ThreadPriority priority) {
    if (priority == Priority_Default) {
        return 0;
    }

    struct sched_param sched;
    int policy;
    pthread_t thread = pthread_self();
    if (pthread_getschedparam(thread, &policy, &sched) < 0) {
        return -1;
    }
    if (priority == Priority_Low) {
        sched.sched_priority = sched_get_priority_min(policy);
    } else if (priority == Priority_High) {
        sched.sched_priority = sched_get_priority_max(policy);
    } else {
        int min_priority = sched_get_priority_min(policy);
        int max_priority = sched_get_priority_max(policy);
        sched.sched_priority = (min_priority + (max_priority - min_priority) / 2);
    }

    if (pthread_setschedparam(thread, policy, &sched) < 0) {
        return -1;
    }
    return 0;
}

inline void Thread::run() {
    // do nothing
}


#endif //THREAD_H
