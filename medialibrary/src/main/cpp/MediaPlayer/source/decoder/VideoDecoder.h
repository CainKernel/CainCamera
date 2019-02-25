//
// Created by cain on 2018/12/26.
//

#ifndef VIDEODECODER_H
#define VIDEODECODER_H


#include <decoder/MediaDecoder.h>
#include <player/PlayerState.h>
#include <sync/MediaClock.h>

class VideoDecoder : public MediaDecoder {
public:
    VideoDecoder(AVFormatContext *pFormatCtx, AVCodecContext *avctx,
                 AVStream *stream, int streamIndex, PlayerState *playerState);

    virtual ~VideoDecoder();

    void setMasterClock(MediaClock *masterClock);

    void start() override;

    void stop() override;

    void flush() override;

    int getFrameSize();

    int getRotate();

    FrameQueue *getFrameQueue();

    void run() override;

private:
    // 解码视频帧
    int decodeVideo();

private:
    AVFormatContext *pFormatCtx;    // 解复用上下文
    FrameQueue *frameQueue;         // 帧队列
    int mRotate;                    // 旋转角度

    bool mExit;                     // 退出标志
    Thread *decodeThread;           // 解码线程
    MediaClock *masterClock;        // 主时钟
};


#endif //VIDEODECODER_H
