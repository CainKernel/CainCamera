//
// Created by CainHuang on 2020/1/12.
//

#include "NdkMediaCodecMuxer.h"

NdkMediaCodecMuxer::NdkMediaCodecMuxer() {
    mPath = nullptr;
    mMediaMuxer = nullptr;
    mMuxerStarted = false;
    mHasVideo = false;
    mHasAudio = false;
}

NdkMediaCodecMuxer::~NdkMediaCodecMuxer() {

}

void NdkMediaCodecMuxer::setOutputPath(const char *path) {
    mPath = strdup(path);
}

void NdkMediaCodecMuxer::setHasAudio(bool hasAudio) {
    mHasAudio = hasAudio;
}

void NdkMediaCodecMuxer::setHasVideo(bool hasVideo) {
    mHasVideo = hasVideo;
}

int NdkMediaCodecMuxer::openMuxer() {
    int mFd;
    FILE *fp = fopen(mPath, "wb");
    if (fp != nullptr) {
        mFd = fileno(fp);
    } else {
        LOGE("open file error: %s", mPath);
        return -1;
    }
    mMediaMuxer = AMediaMuxer_new(mFd, AMEDIAMUXER_OUTPUT_FORMAT_MPEG_4);
    mMuxerStarted = false;
    fclose(fp);
    startTimes = 0;
    return AMEDIA_OK;
}

void NdkMediaCodecMuxer::start() {
    startTimes++;
    if (!mMuxerStarted) {
        bool needToStart = false;
        if (mHasAudio && mHasVideo) {
            needToStart = (startTimes > 1);
        } else if (mHasAudio || mHasVideo) {
            needToStart = (startTimes > 0);
        }
        if (needToStart) {
            AMediaMuxer_start(mMediaMuxer);
            mMuxerStarted = true;
        }
    }
}

void NdkMediaCodecMuxer::closeMuxer() {
    if (mMediaMuxer != nullptr) {
        if (mMuxerStarted) {
            AMediaMuxer_stop(mMediaMuxer);
            mMuxerStarted = false;
        }
        AMediaMuxer_delete(mMediaMuxer);
        mMediaMuxer = nullptr;
    }
}

int NdkMediaCodecMuxer::addTrack(AMediaFormat *mediaFormat) {
    if (!isStart()) {
        return (int) AMediaMuxer_addTrack(mMediaMuxer, mediaFormat);
    }
    return -1;
}

int NdkMediaCodecMuxer::writeFrame(size_t trackId, uint8_t *encodeData,
                                   const AMediaCodecBufferInfo *info) {
    if (isStart()) {
        return AMediaMuxer_writeSampleData(mMediaMuxer, trackId, encodeData, info);
    }
    return -1;
}

bool NdkMediaCodecMuxer::isStart() {
    return mMuxerStarted;
}