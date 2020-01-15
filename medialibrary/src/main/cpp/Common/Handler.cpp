//
// Created by CainHuang on 2020-01-14.
//

#include "Handler.h"

Handler::Handler(MessageQueue *queue) {
    mQueue = queue;
}

Handler::~Handler() {

}

int Handler::sendMessage(Message *msg) {
    msg->target = this;
    mQueue->enqueueMessage(msg);
}

int Handler::sendEmptyMessage(int what) {
    Message *msg = new Message(what);
    msg->target = this;
    mQueue->enqueueMessage(msg);
}

void Handler::handleMessage(Message *msg) {
    if (msg) {
        delete msg;
    }
}