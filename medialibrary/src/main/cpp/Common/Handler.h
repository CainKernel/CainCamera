//
// Created by CainHuang on 2020-01-14.
//

#ifndef HANDLER_H
#define HANDLER_H

#include <Thread.h>
#include "MessageQueue.h"

class Handler {
public:
    Handler(MessageQueue *queue);

    virtual ~Handler();

    int sendMessage(Message *msg);

    int sendEmptyMessage(int what);

    virtual void handleMessage(Message *msg);

private:
    MessageQueue* mQueue;

};


#endif //HANDLER_H
