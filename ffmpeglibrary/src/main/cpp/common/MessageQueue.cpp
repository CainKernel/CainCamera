//
// Created by cain on 2018/2/11.
//

#include "MessageQueue.h"
#include "Handler.h"
#include "AndroidLog.h"

Message::Message() {
    handler = NULL;
}

Message::~Message() {
}

Message::Message(int what) {
    handler = NULL;
    this->what = what;
}

Message::Message(int what, int arg1, int arg2) {
    handler = NULL;
    this->what = what;
    this->arg1 = arg1;
    this->arg2 = arg2;
}

Message::Message(int what, void* obj) {
    handler = NULL;
    this->what = what;
    this->obj = obj;
}

Message::Message(int what, int arg1, int arg2, void* obj) {
    handler = NULL;
    this->what = what;
    this->arg1 = arg1;
    this->arg2 = arg2;
    this->obj = obj;
}

int Message::execute() {
    if (MESSAGE_QUEUE_QUIT_FLAG == what) {
        return MESSAGE_QUEUE_QUIT_FLAG;
    } else if (handler != NULL) {
        handler->handleMessage(this);
        return 1;
    }
    return 0;
};

MessageQueue::MessageQueue() {
    init();
}

MessageQueue::MessageQueue(const char* queueName) {
    init();
    this->queueName = queueName;
}

void MessageQueue::init() {
    int initLockCode = pthread_mutex_init(&mLock, NULL);
    int initConditionCode = pthread_cond_init(&mCondition, NULL);
    mNbPackets = 0;
    mFirst = NULL;
    mLast = NULL;
    mAbortRequest = false;
}

MessageQueue::~MessageQueue() {
    ALOGI("%s ~PacketQueue ....", queueName);
    flush();
    pthread_mutex_destroy(&mLock);
    pthread_cond_destroy(&mCondition);
}

int MessageQueue::size() {
    pthread_mutex_lock(&mLock);
    int size = mNbPackets;
    pthread_mutex_unlock(&mLock);
    return size;
}

/**
 * 刷出剩余视频帧
 */
void MessageQueue::flush() {
    ALOGI("\n %s flush .... and this time the queue size is %d \n", queueName, size());
    MessageNode *curNode, *nextNode;
    Message *msg;
    pthread_mutex_lock(&mLock);
    for (curNode = mFirst; curNode != NULL; curNode = nextNode) {
        nextNode = curNode->next;
        msg = curNode->msg;
        if (NULL != msg) {
            delete msg;
        }
        delete curNode;
        curNode = NULL;
    }
    mLast = NULL;
    mFirst = NULL;
    mNbPackets = 0;
    pthread_mutex_unlock(&mLock);
}

/**
 * 入队
 * @param msg
 * @return
 */
int MessageQueue::enqueueMessage(Message* msg) {
    if (mAbortRequest) {
        delete msg;
        return -1;
    }
    MessageNode *node = new MessageNode();
    if (!node) {
        return -1;
    }
    node->msg = msg;
    node->next = NULL;
    int getLockCode = pthread_mutex_lock(&mLock);
    if (mLast == NULL) {
        mFirst = node;
    } else {
        mLast->next = node;
    }
    mLast = node;
    mNbPackets++;
    pthread_cond_signal(&mCondition);
    pthread_mutex_unlock(&mLock);
    return 0;
}

/**
 * 查询消息队列
 * @param msg
 * @param block
 * @return
 */
int MessageQueue::dequeueMessage(Message **msg, bool block) {
    MessageNode *node;
    int ret;
    int getLockCode = pthread_mutex_lock(&mLock);
    for (;;) {
        if (mAbortRequest) {
            ret = -1;
            break;
        }
        node = mFirst;
        if (node) {
            mFirst = node->next;
            if (!mFirst)
                mLast = NULL;
            mNbPackets--;
            *msg = node->msg;
            delete node;
            node = NULL;
            ret = 1;
            break;
        } else if (!block) {
            ret = 0;
            break;
        } else {
            pthread_cond_wait(&mCondition, &mLock);
        }
    }
    pthread_mutex_unlock(&mLock);
    return ret;
}

/**
 * 舍弃所有消息
 */
void MessageQueue::abort() {
    pthread_mutex_lock(&mLock);
    mAbortRequest = true;
    pthread_cond_signal(&mCondition);
    pthread_mutex_unlock(&mLock);
}



