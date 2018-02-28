//
// Created by Administrator on 2018/2/27.
//

#include "Decoder.h"

Decoder::Decoder(AVCodecContext *avctx, PacketQueue *queue, Cond *empty_queue_cond) {
    this->avctx = avctx;
    this->queue = queue;
    this->empty_queue_cond = empty_queue_cond;
    this->start_pts = AV_NOPTS_VALUE;
    this->reorderPts = -1;
}

Decoder::~Decoder() {
    flush_pkt = NULL;
    av_packet_unref(&pkt);
    avcodec_free_context(&avctx);
}

void Decoder::setFlushPacket(AVPacket *pkt) {
    flush_pkt = pkt;
}

int Decoder::decodeFrame(AVFrame *frame, AVSubtitle *sub) {
    int got_frame = 0;

    do {
        int ret = -1;
        // 如果处于舍弃状态，则直接返回
        if (queue->isAbort()) {
            return -1;
        }
        // 如果当前没有包在等待，获取队列的序列不相同，取出下一阵
        if (!packet_pending || queue->serial != pkt_serial) {
            AVPacket pkt;
            do {
                // 队列为空
                if (queue->isEmpty()) {
                    CondSignal(empty_queue_cond);
                }
                // 获取裸数据
                if (queue->get(&pkt, 1, &pkt_serial) < 0) {
                    return -1;
                }
                // 刷新数据
                if (pkt.data == flush_pkt->data) {
                    avcodec_flush_buffers(avctx);
                    finished = 0;
                    next_pts = start_pts;
                    next_pts_tb = start_pts_tb;
                }
            } while (pkt.data == flush_pkt->data || queue->serial != pkt_serial);
            av_packet_unref(&this->pkt);
            this->pkt_temp = this->pkt_temp = pkt;
            packet_pending = 1;
        }

        // 根据解码器类型判断是音频还是视频
        switch (avctx->codec_type) {
            case AVMEDIA_TYPE_VIDEO:
                // 视频解码
                ret = avcodec_decode_video2(avctx, frame, &got_frame, &pkt_temp);
                // 解码成功，更新时间戳
                if (got_frame) {
                    if (reorderPts == -1) {
                        frame->pts = av_frame_get_best_effort_timestamp(frame);
                    } else if (!reorderPts) {
                        frame->pts = frame->pkt_dts;
                    }
                }
                break;

            case AVMEDIA_TYPE_AUDIO:
                // 音频解码
                ret = avcodec_decode_audio4(avctx, frame, &got_frame, &pkt_temp);
                if (got_frame) {
                    AVRational tb = (AVRational) {1, frame->sample_rate};
                    // 更新帧时间戳
                    if (frame->pts != AV_NOPTS_VALUE) {
                        frame->pts = av_rescale_q(frame->pts, av_codec_get_pkt_timebase(avctx), tb);
                    } else if (next_pts != AV_NOPTS_VALUE) {
                        frame->pts = av_rescale_q(next_pts, next_pts_tb, tb);
                    }
                    // 更新下一帧时间戳
                    if (frame->pts != AV_NOPTS_VALUE) {
                        next_pts = frame->pts + frame->nb_samples;
                        next_pts_tb = tb;
                    }
                }
                break;
        }

        // 判断是否解码成功
        if (ret < 0) {
            packet_pending = 0;
        } else {
            pkt_temp.dts = pkt_temp.pts = AV_NOPTS_VALUE;
            if (pkt_temp.data) {
                if (avctx->codec_type != AVMEDIA_TYPE_AUDIO) {
                    ret = pkt_temp.size;
                }
                pkt_temp.data += ret;
                pkt_temp.size -= ret;

                if (pkt_temp.size <= 0) {
                    packet_pending = 0;
                }
            } else {
                if (!got_frame) {
                    packet_pending = 0;
                    finished = pkt_serial;
                }
            }
        }
    } while (!got_frame && !finished);

    return got_frame;
}

void Decoder::abort(FrameQueue *fq) {
    queue->abort();
    fq->signal();
    ThreadWait(decoder_tid, NULL);
    free(decoder_tid);
    decoder_tid = NULL;
    queue->flush();
}