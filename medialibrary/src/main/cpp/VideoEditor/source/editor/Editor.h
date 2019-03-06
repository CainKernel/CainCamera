//
// Created by CainHuang on 2019/2/26.
//

#ifndef EDITOR_H
#define EDITOR_H

#include <editor_log.h>

extern "C" {
#include <libavcodec/avcodec.h>
#include <libavfilter/avfilter.h>
#include <libavfilter/buffersink.h>
#include <libavfilter/buffersrc.h>
#include <libavformat/avformat.h>
#include <libavutil/audio_fifo.h>
#include <libavutil/avassert.h>
#include <libavutil/avstring.h>
#include <libavutil/dict.h>
#include <libavutil/imgutils.h>
#include <libavutil/opt.h>
#include <libavutil/samplefmt.h>
#include <libavutil/timestamp.h>
#include <libswresample/swresample.h>
#include <libswscale/swscale.h>
};

enum editor_event_type {
    EDITOR_PROCESSING = 1,
    EDITOR_ERROR = 100,
};


// 消息句柄结构体
typedef struct MessageHandle {
    void *opaque;
    void (*callback)(void *opaque, int what, int arg1, int arg2, void *obj, int len);
} MessageHandle;

class Editor {
public:
    Editor(MessageHandle *messageHandle)
            : messageHandle(messageHandle) {}

    virtual ~Editor() {

    }

    virtual void reset() {
        av_freep(&messageHandle);
        messageHandle = nullptr;
    }

    // 处理方法
    virtual int process() = 0;

protected:
    MessageHandle *messageHandle;
};

#endif //EDITOR_H
