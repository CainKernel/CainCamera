//
// Created by cain on 2018/12/26.
//

#include "AudioDecoder.h"

AudioDecoder::AudioDecoder(AVCodecContext *avctx, AVStream *stream, int streamIndex, PlayerState *playerState)
        : MediaDecoder(avctx, stream, streamIndex, playerState) {
    packet = av_packet_alloc();
    packetPending = 0;
}

AudioDecoder::~AudioDecoder() {
    mMutex.lock();
    packetPending = 0;
    if (packet) {
        av_packet_free(&packet);
        av_freep(&packet);
        packet = NULL;
    }
    mMutex.unlock();
}

int AudioDecoder::getAudioFrame(AVFrame *frame) {
    int got_frame = 0;
    int ret = 0;

    if (!frame) {
        return AVERROR(ENOMEM);
    }
    av_frame_unref(frame);

    do {

        if (abortRequest) {
            ret = -1;
            break;
        }

        if (playerState->seekRequest) {
            continue;
        }

        AVPacket pkt;
        if (packetPending) {
            av_packet_move_ref(&pkt, packet);
            packetPending = 0;
        } else {
            if (packetQueue->getPacket(&pkt) < 0) {
                ret = -1;
                break;
            }
        }

        playerState->mMutex.lock();
        // 将数据包解码
        ret = avcodec_send_packet(pCodecCtx, &pkt);
        if (ret < 0) {
            // 一次解码无法消耗完AVPacket中的所有数据，需要重新解码
            if (ret == AVERROR(EAGAIN)) {
                av_packet_move_ref(packet, &pkt);
                packetPending = 1;
            } else {
                av_packet_unref(&pkt);
                packetPending = 0;
            }
            playerState->mMutex.unlock();
            continue;
        }

        // 获取解码得到的音频帧AVFrame
        ret = avcodec_receive_frame(pCodecCtx, frame);
        playerState->mMutex.unlock();
        // 释放数据包的引用，防止内存泄漏
        av_packet_unref(packet);
        if (ret < 0) {
            av_frame_unref(frame);
            got_frame = 0;
            continue;
        } else {
            got_frame = 1;
            // 这里要重新计算frame的pts 否则会导致网络视频出现pts 对不上的情况
            AVRational tb = (AVRational){1, frame->sample_rate};
            if (frame->pts != AV_NOPTS_VALUE) {
                frame->pts = av_rescale_q(frame->pts, av_codec_get_pkt_timebase(pCodecCtx), tb);
            } else if (next_pts != AV_NOPTS_VALUE) {
                frame->pts = av_rescale_q(next_pts, next_pts_tb, tb);
            }
            if (frame->pts != AV_NOPTS_VALUE) {
                next_pts = frame->pts + frame->nb_samples;
                next_pts_tb = tb;
            }
        }
    } while (!got_frame);

    if (ret < 0) {
        return -1;
    }

    return got_frame;
}







