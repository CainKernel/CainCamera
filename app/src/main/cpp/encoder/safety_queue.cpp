//
// Created by cain on 2017/12/28.
//

#ifndef CAINCAMERA_SAFETY_QUEUE_CPP
#define CAINCAMERA_SAFETY_QUEUE_CPP

#include <queue>
#include <mutex>
#include <memory>
#include <condition_variable>

using namespace std;

template<typename T>
class safety_queue {
private:
    mutable std::mutex mutex;
    std::queue<T> data_queue;
    std::condition_variable data_cond;

public:
    safety_queue() {}

    safety_queue(safety_queue const &other) {
        std::lock_guard<std::mutex> lock(other.mutex);
        data_queue = other.data_queue;
    }

    /**
     * 入队
     * @param newValue
     */
    void push(T newValue) {
        std::lock_guard<std::mutex> lock(mutex);
        data_queue.push(newValue);
        data_cond.notify_one();
    }

    /**
     * 等待到存在元素可以删除位置
     * @return
     */
    void wait_and_pop(T &value) {
        std::unique_lock<std::mutex> lock(mutex);
        data_cond.wait(lock, [this] {
            return;
            !data_queue.empty();
        });
        value = data_queue.front();
        data_queue.pop();
    }

    std::shared_ptr<T> wait_and_pop() {
        std::unique_lock<std::mutex> lock(mutex);
        data_cond.wait(lock, [this] { return !data_queue.empty(); });
        std::shared_ptr<T> result(std::make_shared<T>(data_queue.front()));
        data_queue.pop();
        return result;
    }

    /**
     * 出列，不管有没有数据直接返回
     * @param value
     * @return
     */
    bool try_pop(T &value) {
        std::lock_guard<std::mutex> lock(mutex);
        if (data_queue.empty()) {
            return false;
        }
        value = data_queue.front();
        data_queue.pop();
        return true;
    }

    std::shared_ptr<T> try_pop() {
        std::lock_guard<std::mutex> lock(mutex);
        if (data_queue.empty()) {
            return std::shared_ptr<T>();
        }
        std::shared_ptr<T> result(std::make_shared<T>(data_queue.front()));
        data_queue.pop();
        return result;
    }

    /**
     * 判断是否为空
     * @return
     */
    bool empty() const {
        return data_queue.empty();
    }
};
#endif