//
// Created by Administrator on 2018/2/27.
//

#ifndef CAINCAMERA_DECODER_H
#define CAINCAMERA_DECODER_H

#include "CainPlayerDefinition.h"
#include "PacketQueue.h"
#include "Mutex.h"
#include "Thread.h"
#include "FrameQueue.h"

class Decoder {
public:
    AVPacket *flush_pkt;        // 用于flush的裸数据包
    AVPacket pkt;               // 裸数据包
    AVPacket pkt_temp;	        // 中间包
    PacketQueue *queue;         // 包队列
    AVCodecContext *avctx;      // 解码上下文
    int pkt_serial;             // 包序列
    int finished;               // 是否已经结束
    int packet_pending;         // 是否裸数据包挂起，如果为1则表示挂起，为0则表示可用
    Cond *empty_queue_cond;		// 空队列条件变量
    int64_t start_pts;          // 开始的时间戳
    AVRational start_pts_tb;    // 开始的额外参数
    int64_t next_pts;           // 下一帧时间戳
    AVRational next_pts_tb;     // 下一帧的额外参数
    Thread *decoder_tid;        // 解码线程
    int reorderPts;             // 是否需要重新排列PTS

    Decoder(AVCodecContext *avctx, PacketQueue *queue, Cond *empty_queue_cond);
    virtual ~Decoder();
    void setFlushPacket(AVPacket *pkt);
    int decodeFrame(AVFrame *frame, AVSubtitle *sub);
    void abort(FrameQueue *fq);
};


#endif //CAINCAMERA_DECODER_H
