//
// Created by cain on 2018/5/1.
//

#include "VideoOutputLooper.h"

VideoOutputLooper::VideoOutputLooper(AVDecoder *decoder) : Looper() {
    videoOutput = new AVVideoOutput(decoder);
}

VideoOutputLooper::~VideoOutputLooper() {
    quit();
    if (videoOutput != NULL) {
        delete videoOutput;
        videoOutput = NULL;
    }
}

void VideoOutputLooper::handleMessage(LooperMessage *msg) {
    if (videoOutput == NULL) {
        return;
    }
    switch (msg->what) {
        case kMsgVideoSurfaceCreated: {
            ANativeWindow *window = (ANativeWindow *) msg->obj;
            videoOutput->surfaceCreated(window);
            break;
        }

        case kMsgVideoSurfaceChanged: {
            videoOutput->surfaceChanged(msg->arg1, msg->arg2);
            break;
        }

        case kMsgVideoSurfaceDestroyed: {
            videoOutput->surfaceDestroyed();
            break;
        }

        case kMsgVideoDisplayFrame: {
            videoOutput->displayVideo((AVFrame *) msg->obj);
            break;
        }

        default: {
            break;
        }
    }

}

void VideoOutputLooper::onSurfaceCreated(ANativeWindow *window) {
    postMessage(kMsgVideoSurfaceCreated, window);
}

void VideoOutputLooper::onSurfaceChanged(int width, int height) {
    postMessage(kMsgVideoSurfaceChanged, width, height);
}

void VideoOutputLooper::onSurfaceDestroyed() {
    postMessage(kMsgVideoSurfaceDestroyed);
}

void VideoOutputLooper::onDisplayVideo(AVFrame *frame) {
    postMessage(kMsgVideoDisplayFrame, frame);
}

void VideoOutputLooper::stop() {
    if (running) {
        quit();
    }
}