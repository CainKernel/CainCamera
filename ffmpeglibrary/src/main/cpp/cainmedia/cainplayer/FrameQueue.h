//
// Created by Administrator on 2018/3/23.
//

#ifndef CAINCAMERA_FRAMEQUEUE_H
#define CAINCAMERA_FRAMEQUEUE_H

#include <queue>

#include <Mutex.h>

#ifdef __cplusplus
extern "C" {
#endif

#include <libavutil/frame.h>

#ifdef __cplusplus
};
#endif

#define FRAME_QUEUE_SIZE 5

class FrameQueue {
public:
    FrameQueue();

    virtual ~FrameQueue();
    int put(AVFrame *avFrame);
    int get(AVFrame *avframe);
    int flush();
    int size();
    void notify();
    void setAbort(bool abort);
    bool isAbort();

private:
    std::queue<AVFrame *> mQueue;
    Mutex *mMutex;
    Cond *mCondition;
    bool mAbortRequest;
};


#endif //CAINCAMERA_FRAMEQUEUE_H
