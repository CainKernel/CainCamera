//
// Created by CainHuang on 2020/1/4.
//

#ifndef MEDIATRANSCODER_H
#define MEDIATRANSCODER_H

#include <Thread.h>
#include <AVMediaData.h>
#include "OnTranscodeListener.h"
#include "TranscodeParams.h"
#include "MediaFrameProvider.h"

class MediaTranscoder : public Runnable {
public:
    MediaTranscoder();

    virtual ~MediaTranscoder();

    void setOnTranscodeListener(OnTranscodeListener *listener);

    void release();

    void startTranscode();

    void stopTranscode();

    bool isTranscoding();

    void run() override;

    TranscodeParams *getParams();

private:
    Mutex mMutex;
    Condition mCondition;
    Thread *mTranscodeThread;
    OnTranscodeListener *mTranscodeListener;
    bool mAbortRequest;
    bool mStartRequest;
    bool mTranscoding;
    bool mExit;

    TranscodeParams *mParams;

    MediaFrameProvider *mFrameProvider;
};


#endif //MEDIATRANSCODER_H
