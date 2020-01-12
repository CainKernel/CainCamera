//
// Created by CainHuang on 2020/1/4.
//

#include "MediaTranscoder.h"

MediaTranscoder::MediaTranscoder() {
    mParams = new TranscodeParams();
    mAbortRequest = false;
    mStartRequest = false;
    mTranscoding = false;
    mExit = false;
    mFrameProvider = new MediaFrameProvider();
}

MediaTranscoder::~MediaTranscoder() {
    release();
}

void MediaTranscoder::setOnTranscodeListener(OnTranscodeListener *listener) {
    if (mTranscodeListener != nullptr) {
        delete mTranscodeListener;
    }
    mTranscodeListener = listener;
}

void MediaTranscoder::release() {
    if (mFrameProvider != nullptr) {
        mFrameProvider->release();
        mFrameProvider = nullptr;
    }
    if (mTranscodeListener != nullptr) {
        delete mTranscodeListener;
        mTranscodeListener = nullptr;
    }
    if (mParams != nullptr) {
        delete mParams;
        mParams = nullptr;
    }
}


void MediaTranscoder::startTranscode() {

}

void MediaTranscoder::stopTranscode() {

}

bool MediaTranscoder::isTranscoding() {
    return mTranscoding;
}

void MediaTranscoder::run() {

}

TranscodeParams* MediaTranscoder::getParams() {
    return mParams;
}
