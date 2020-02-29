//
// Created by CainHuang on 2019/7/28.
//

#ifndef SAFETYQUEUE_H
#define SAFETYQUEUE_H

#include <queue>
#include <mutex>

template <typename T>
class SafetyQueue {

public:
    SafetyQueue() {

    }

    void push(T element) {
        std::lock_guard<std::mutex> lock(mutex);
        queue.push(element);
    }

    T front() {
        std::lock_guard<std::mutex> lock(mutex);
        return queue.front();
    }

    T back() {
        std::lock_guard<std::mutex> lock(mutex);
        return queue.back();
    }

    T pop() {
        std::lock_guard<std::mutex> lock(mutex);
        T ret = queue.front();
        queue.pop();
        return ret;
    }

    bool empty() {
        std::lock_guard<std::mutex> lock(mutex);
        return queue.empty();
    }

    int size() {
        std::lock_guard<std::mutex> lock(mutex);
        return static_cast<int>(queue.size());
    }
private:
    mutable std::mutex mutex;
    std::queue<T> queue;
};

#endif //SAFETYQUEUE_H
