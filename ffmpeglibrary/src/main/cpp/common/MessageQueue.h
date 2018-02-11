//
// Created by cain on 2018/2/11.
//

#ifndef CAINCAMERA_MESSAGEQUEUE_H
#define CAINCAMERA_MESSAGEQUEUE_H

#include <pthread.h>

#define MESSAGE_QUEUE_QUIT_FLAG        20000000

class Handler;

class Message {
private:
    int what;
    int arg1;
    int arg2;
    void *obj;

public:
    Message();
    ~Message();
    Message(int what);
    Message(int what, int arg1, int arg2);
    Message(int what, void *obj);
    Message(int what, int arg1, int arg2, void *obj);

    int execute();

    int getWhat() const {
        return what;
    }

    int getArg1() const {
        return arg1;
    }

    int getArg2() const {
        arg2;
    }

    void *getObj() const {
        return obj;
    }

    Handler *handler;
};


typedef struct MessageNode {
    Message *msg;
    struct MessageNode *next;
    MessageNode() {
        msg = NULL;
        next = NULL;
    }
} MessageNode;


class MessageQueue {
private:
    MessageNode* mFirst;
    MessageNode* mLast;
    int mNbPackets;
    bool mAbortRequest;
    pthread_mutex_t mLock;
    pthread_cond_t mCondition;
    const char* queueName;


public:
    MessageQueue();
    MessageQueue(const char* queueName);
    ~MessageQueue();

    void init();
    void flush();
    int enqueueMessage(Message* msg);
    int dequeueMessage(Message **msg, bool block);
    int size();
    void abort();
};


#endif //CAINCAMERA_MESSAGEQUEUE_H
