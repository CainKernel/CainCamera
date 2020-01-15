//
// Created by CainHuang on 2020-01-14.
//

#include "MessageQueue.h"
#include "Handler.h"

MessageQueue::MessageQueue(const char *name) {
    init();
    mName = name;
}

MessageQueue::~MessageQueue() {
    mAbortRequest = true;
}

void MessageQueue::init() {
    mAbortRequest = false;
}

int MessageQueue::size() {
    std::unique_lock<std::mutex> lock(mutex);
    return (int)queue.size();
}

void MessageQueue::flush() {
    std::unique_lock<std::mutex> lock(mutex);
    while (queue.size() > 0) {
        auto message = queue.front();
        queue.pop();
        if (message) {
            delete message;
        }
    }
    condition.notify_all();
}

void MessageQueue::abort() {
    std::unique_lock<std::mutex> lock(mutex);
    mAbortRequest = true;
    condition.notify_all();
}

bool MessageQueue::empty() {
    std::unique_lock<std::mutex> lock(mutex);
    return queue.empty();
}

void MessageQueue::enqueueMessage(Message *msg) {
    std::unique_lock<std::mutex> lock(mutex);
    queue.push(msg);
}

Message *MessageQueue::dequeueMessage(bool block) {
    Message *msg = nullptr;
    std::unique_lock<std::mutex> lock(mutex);
    for (;;) {
        if (mAbortRequest) {
            break;
        }
        if (queue.size() > 0) {
            msg = queue.front();
            queue.pop();
            break;
        } else if (!block) {
            break;
        } else {
            condition.wait(lock);
        }
    }
    return msg;
}

Message::Message() : target(nullptr) {}

Message::Message(int what) : target(nullptr), what(what) {}

Message::Message(int what, int arg1, int arg2) : target(nullptr), what(what), arg1(arg1),
                                                 arg2(arg2) {}

Message::Message(int what, void *obj) : target(nullptr), what(what), obj(obj) {}

Message::Message(int what, int arg1, int arg2, void *obj) : target(nullptr), what(what),
                                                            arg1(arg1), arg2(arg2), obj(obj) {}

Message::~Message() {

}

int Message::execute() {
    if (target) {
        target->handleMessage(this);
        return 0;
    }
    return -1;
}
