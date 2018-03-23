//
// Created by Administrator on 2018/3/23.
//

#include "Synchronizer.h"

Synchronizer::Synchronizer(AudioDecoder *audioDecoder, VideoDecoder *videoDecoder) {
    mAudioDecoder = audioDecoder;
    mVideoDecoder = videoDecoder;

    mAudioClock = new Clock();
    mVideoClock = new Clock();
    mExternalClock = new Clock();
}

Synchronizer::~Synchronizer() {
    mAudioDecoder = NULL;
    mVideoDecoder = NULL;
    delete(mAudioClock);
    delete(mVideoClock);
    delete(mExternalClock);
    mAudioClock = NULL;
    mVideoClock = NULL;
    mExternalClock = NULL;
}

int Synchronizer::getMasterSyncType() {
    if (mSyncType == AV_SYNC_VIDEO_MASTER) {
        if (mVideoDecoder != NULL) {
            return AV_SYNC_VIDEO_MASTER;
        } else {
            return AV_SYNC_EXTERNAL_CLOCK;
        }
    } else if (mSyncType == AV_SYNC_AUDIO_MASTER) {
        if (mAudioDecoder != NULL) {
            return AV_SYNC_AUDIO_MASTER;
        } else {
            return AV_SYNC_EXTERNAL_CLOCK;
        }
    } else {
        return AV_SYNC_EXTERNAL_CLOCK;
    }
}

double Synchronizer::getMasterClock() {
    double val;

    switch (getMasterSyncType()) {
        case AV_SYNC_VIDEO_MASTER:
            val = mVideoClock->getClock();
            break;

        case AV_SYNC_AUDIO_MASTER:
            val = mAudioClock->getClock();
            break;

        default:
            val = mExternalClock->getClock();
            break;
    }

    return val;
}

void Synchronizer::checkExternalClockSpeed() {

}