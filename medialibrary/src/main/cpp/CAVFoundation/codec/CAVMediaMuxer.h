//
// Created by CainHuang on 2020/9/5.
//

#ifndef CAVMEDIAMUXER_H
#define CAVMEDIAMUXER_H

#include <AVMediaHeader.h>
#include <cstdint>
#include "CAVMediaInfo.h"

/**
 * 媒体封装器
 */
class CAVMediaMuxer {
public:
    CAVMediaMuxer();

    virtual ~CAVMediaMuxer();

    void setOutputPath(const char *url);

    int prepare();

    void start();

    int stop();

    void release();

    void setAudioInfo(CAVAudioInfo &audioInfo);

    void setVideoInfo(CAVVideoInfo &videoInfo);

    int writeExtraData(int track, uint8_t *extraData, size_t size);

    int writeFrame(AVPacket *packet);

    bool hasGlobalHeader();

    void printInfo();

    int getAudioTrack();

    int getVideoTrack();

    bool isStarted();

    int rescalePacketTs(AVPacket *packet, int track, long pts, AVRational rational);

protected:
    int prepareTrack(CAVAudioInfo *info);

    int prepareTrack(CAVVideoInfo *info);

    int init();

    int openMuxer();

    AVStream *createStream(AVCodecID id);

    int writeHeader(AVDictionary **options = nullptr);

    int writeTrailer();

    void closeMuxer();

private:
    bool prepared;
    bool started;
    const char *path;
    AVFormatContext *pFormatCtx;
    CAVVideoInfo *videoInfo;
    CAVAudioInfo *audioInfo;
};


#endif //CAVMEDIAMUXER_H
