//
// Created by cain on 2018/12/26.
//

#ifndef AUDIODECODER_H
#define AUDIODECODER_H


#include <decoder/MediaDecoder.h>
#include <player/PlayerState.h>

class AudioDecoder : public MediaDecoder {
public:
    AudioDecoder(AVCodecContext *avctx, AVStream *stream, int streamIndex, PlayerState *playerState);

    virtual ~AudioDecoder();

    int getAudioFrame(AVFrame *frame);

private:
    bool packetPending; // 一次解码无法全部消耗完AVPacket中的数据的标志
    AVPacket *packet;
    int64_t next_pts;
    AVRational next_pts_tb;
};


#endif //AUDIODECODER_H
