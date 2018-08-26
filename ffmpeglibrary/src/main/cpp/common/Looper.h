//
// Created by admin on 2018/3/30.
//

#ifndef CAINPLAYER_LOOPER_H
#define CAINPLAYER_LOOPER_H

#include <pthread.h>
#include <sys/types.h>
#include <semaphore.h>

struct LooperMessage {
    int what;
    int arg1;
    int arg2;
    void *obj;
    LooperMessage *next;
    bool quit;
};

class Looper {

public:
    Looper();
    Looper&operator=(const Looper& ) = delete;
    Looper(Looper&) = delete;
    virtual ~Looper();

    // 发送消息
    void postMessage(int what, bool flush = false);
    void postMessage(int what, void *obj, bool flush = false);
    void postMessage(int what, int arg1, int arg2, bool flush = false);
    void postMessage(int what, int arg1, int arg2, void *obj, bool flush = false);

    // 退出Looper循环
    void quit();

    // 处理消息
    virtual void handleMessage(LooperMessage *msg);

private:
    // 添加消息
    void addMessage(LooperMessage *msg, bool flush);

    // 消息线程句柄
    static void *trampoline(void *p);

    // 循环体
    void loop(void);

    LooperMessage *head;
    pthread_t worker;
    sem_t headwriteprotect;
    sem_t headdataavailable;

protected:
    bool running;
};


#endif //CAINPLAYER_LOOPER_H
