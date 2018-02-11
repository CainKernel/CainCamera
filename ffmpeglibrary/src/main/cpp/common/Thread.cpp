//
// Created by cain on 2018/2/11.
//


#include "Thread.h"

Thread::Thread() {
    pthread_mutex_init(&mLock, NULL);
    pthread_cond_init(&mCondition, NULL);
}

Thread::~Thread() {

}

void Thread::start() {
    handleRun(NULL);
}

void Thread::startAsync() {
    pthread_create(&mThread, NULL, startThread, this);
}

int Thread::wait() {
    if (!mRunning) {
        return 0;
    }
    void *status;
    int ret = pthread_join(mThread, &status);
    return ret;
}

void Thread::stop() {

}

void* Thread::startThread(void *ptr) {
    Thread *thread = (Thread *)ptr;
    thread->mRunning = true;
    thread->handleRun(ptr);
    thread->mRunning = false;
}

void Thread::notify() {
    pthread_mutex_lock(&mLock);
    pthread_cond_signal(&mCondition);
    pthread_mutex_unlock(&mLock);
}

void Thread::handleRun(void *ptr) {

}

