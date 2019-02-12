//
// Created by cain on 2018/12/21.
//

#ifndef MEDIAPLAYER_FRAMEQUEUE_H
#define MEDIAPLAYER_FRAMEQUEUE_H

#include <Mutex.h>
#include <Condition.h>

extern "C" {
#include <libavcodec/avcodec.h>
};

#define FRAME_QUEUE_SIZE 10

typedef struct Frame {
    AVFrame *frame;
    AVSubtitle sub;
    double pts;           /* presentation timestamp for the frame */
    double duration;      /* estimated duration of the frame */
    int width;
    int height;
    int format;
    int uploaded;
} Frame;

class FrameQueue {

public:
    FrameQueue(int max_size, int keep_last);

    virtual ~FrameQueue();

    void start();

    void abort();

    Frame *currentFrame();

    Frame *nextFrame();

    Frame *lastFrame();

    Frame *peekWritable();

    void pushFrame();

    void popFrame();

    void flush();

    int getFrameSize();

    int getShowIndex() const;

private:
    void unrefFrame(Frame *vp);

private:
    Mutex mMutex;
    Condition mCondition;
    int abort_request;
    Frame queue[FRAME_QUEUE_SIZE];
    int rindex;
    int windex;
    int size;
    int max_size;
    int keep_last;
    int show_index;
};


#endif //MEDIAPLAYER_FRAMEQUEUE_H
