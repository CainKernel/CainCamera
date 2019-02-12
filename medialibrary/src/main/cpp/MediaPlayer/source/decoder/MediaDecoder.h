//
// Created by cain on 2018/12/27.
//

#ifndef MEDIADECODER_H
#define MEDIADECODER_H

#include <AndroidLog.h>
#include <player/PlayerState.h>
#include <queue/PacketQueue.h>
#include <queue/FrameQueue.h>

class MediaDecoder : public Runnable {
public:
    MediaDecoder(AVCodecContext *avctx, AVStream *stream, int streamIndex, PlayerState *playerState);

    virtual ~MediaDecoder();

    virtual void start();

    virtual void stop();

    virtual void flush();

    int pushPacket(AVPacket *pkt);

    int getPacketSize();

    int getStreamIndex();

    AVStream *getStream();

    AVCodecContext *getCodecContext();

    int getMemorySize();

    int hasEnoughPackets();

    virtual void run();

protected:
    Mutex mMutex;
    Condition mCondition;
    bool abortRequest;
    PlayerState *playerState;
    PacketQueue *packetQueue;       // 数据包队列
    AVCodecContext *pCodecCtx;
    AVStream *pStream;
    int streamIndex;
};


#endif //MEDIADECODER_H
