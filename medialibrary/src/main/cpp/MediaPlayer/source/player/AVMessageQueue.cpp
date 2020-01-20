//
// Created by cain on 2019/1/28.
//

#include "AVMessageQueue.h"


AVMessageQueue::AVMessageQueue() {
    abortRequest = false;
    mSize = 0;
    mFirstMsg = 0;
    mLastMsg = 0;
}

AVMessageQueue::~AVMessageQueue() {

}

void AVMessageQueue::start() {
    Mutex::Autolock lock(mMutex);
    abortRequest = false;
    AVMessage msg;
    message_init(&msg);
    msg.what = MSG_FLUSH;
}

void AVMessageQueue::stop() {
    Mutex::Autolock lock(mMutex);
    abortRequest = true;
    mCondition.signal();
}

void AVMessageQueue::flush() {
    AVMessage *msg, *msg1;
    Mutex::Autolock lock(mMutex);
    for (msg = mFirstMsg; msg != NULL; msg = msg1) {
        msg1 = msg->next;
        av_freep(&msg);
    }
    mFirstMsg = NULL;
    mLastMsg = NULL;
    mSize = 0;
    mCondition.signal();
}

void AVMessageQueue::release() {
    flush();
}

void AVMessageQueue::postMessage(int what) {
    AVMessage msg;
    message_init(&msg);
    msg.what = what;
    putMessage(&msg);
}

void AVMessageQueue::postMessage(int what, int arg1) {
    AVMessage msg;
    message_init(&msg);
    msg.what = what;
    msg.arg1 = arg1;
    putMessage(&msg);
}

void AVMessageQueue::postMessage(int what, int arg1, int arg2) {
    AVMessage msg;
    message_init(&msg);
    msg.what = what;
    msg.arg1 = arg1;
    msg.arg2 = arg2;
    putMessage(&msg);
}

void AVMessageQueue::postMessage(int what, int arg1, int arg2, void *obj, int len) {
    AVMessage msg;
    message_init(&msg);
    msg.what = what;
    msg.arg1 = arg1;
    msg.arg2 = arg2;
    msg.obj = av_malloc(sizeof(len));
    memcpy(msg.obj, obj, len);
    msg.free = message_free;
    putMessage(&msg);
}

int AVMessageQueue::getMessage(AVMessage *msg) {
    return getMessage(msg, 1);
}

int AVMessageQueue::getMessage(AVMessage *msg, int block) {
    AVMessage *msg1;
    int ret;
    mMutex.lock();
    for (;;) {
        if (abortRequest) {
            ret = -1;
            break;
        }
        msg1 = mFirstMsg;
        if (msg1) {
            mFirstMsg = msg1->next;
            if (!mFirstMsg) {
                mLastMsg = NULL;
            }
            mSize--;
            *msg = *msg1;
            msg1->obj = NULL;
            av_free(msg1);
            ret = 1;
            break;
        } else if (!block) {
            ret = 0;
            break;
        } else {
            mCondition.wait(mMutex);
        }
    }
    mMutex.unlock();

    return ret;
}

void AVMessageQueue::removeMessage(int what) {
    Mutex::Autolock lock(mMutex);
    AVMessage **p_msg, *msg, *last_msg;
    last_msg = mFirstMsg;
    if (!abortRequest && mFirstMsg) {
        p_msg = &mFirstMsg;
        while (*p_msg) {
            msg = *p_msg;

            if (msg->what == what) {
                *p_msg = msg->next;
                av_free(msg);
                mSize--;
            } else {
                last_msg = msg;
                p_msg = &msg->next;
            }
        }

        if (mFirstMsg) {
            mLastMsg = last_msg;
        } else {
            mLastMsg = NULL;
        }
    }
    mCondition.signal();
}

int AVMessageQueue::putMessage(AVMessage *msg) {
    Mutex::Autolock lock(mMutex);
    AVMessage *message;
    if (abortRequest) {
        return -1;
    }
    message = (AVMessage *) av_malloc(sizeof(AVMessage));
    if (!message) {
        return -1;
    }
    *message = *msg;
    message->next = NULL;

    if (!mLastMsg) {
        mFirstMsg = message;
    } else {
        mLastMsg->next = message;
    }
    mLastMsg = message;
    mSize++;
    mCondition.signal();
    return 0;
}
