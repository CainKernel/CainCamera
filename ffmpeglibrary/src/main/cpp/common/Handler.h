//
// Created by cain on 2018/2/11.
//

#ifndef CAINCAMERA_HANDLER_H
#define CAINCAMERA_HANDLER_H

#include "MessageQueue.h"

class Handler {
private:
    MessageQueue *mQueue;

public:
    Handler(MessageQueue *queue);
    virtual ~Handler();

    int postMessage(Message *msg);

    virtual void handleMessage(Message* msg) {};
};

#endif //CAINCAMERA_HANDLER_H
