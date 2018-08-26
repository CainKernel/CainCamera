//
// Created by cain on 2018/5/1.
//

#ifndef CAINPLAYER_VIDEOOUTPUTLOOPER_H
#define CAINPLAYER_VIDEOOUTPUTLOOPER_H


#include <Looper.h>
#include "AVVideoOutput.h"

typedef enum {
    kMsgVideoSurfaceCreated,
    kMsgVideoSurfaceChanged,
    kMsgVideoSurfaceDestroyed,
    kMsgVideoDisplayFrame,
} VideoOutputType;

class VideoOutputLooper : public Looper {
public:
    VideoOutputLooper(AVDecoder *decoder);

    virtual ~VideoOutputLooper();

    void handleMessage(LooperMessage *msg) override;

    void onSurfaceCreated(ANativeWindow *window);

    void onSurfaceChanged(int width, int height);

    void onSurfaceDestroyed();

    void onDisplayVideo(AVFrame *frame);

    void stop();

private:
    AVVideoOutput *videoOutput;
};


#endif //CAINPLAYER_VIDEOOUTPUTLOOPER_H
