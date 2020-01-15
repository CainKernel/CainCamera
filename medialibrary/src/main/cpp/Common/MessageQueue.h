//
// Created by CainHuang on 2020-01-14.
//

#ifndef MESSAGEQUEUE_H
#define MESSAGEQUEUE_H

#include <SafetyQueue.h>
#include <Thread.h>

class Handler;

class Message {
public:
    Message();

    Message(int what);

    Message(int what, int arg1, int arg2);

    Message(int what, void *obj);

    Message(int what, int arg1, int arg2, void *obj);

    virtual ~Message();

    int execute();

    inline int getWhat() {
        return what;
    }

    inline int getArg1() {
        return arg1;
    }

    inline int getArg2() {
        return arg2;
    }

    void* getObj() {
        return obj;
    }

    Handler *target;
private:
    int what;
    int arg1;
    int arg2;
    void* obj;
};

class MessageQueue {
public:
    MessageQueue(const char *name = nullptr);

    virtual ~MessageQueue();

    void flush();

    void enqueueMessage(Message *msg);

    Message *dequeueMessage(bool block = false);

    int size();

    void abort();

    bool empty();

private:
    void init();

private:
    bool mAbortRequest;
    const char *mName;
    mutable std::mutex mutex;
    std::condition_variable condition;
    std::queue<Message *> queue;
};


#endif //MESSAGEQUEUE_H
