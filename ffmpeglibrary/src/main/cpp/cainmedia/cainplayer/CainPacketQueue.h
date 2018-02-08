//
// Created by Administrator on 2018/2/8.
//

#ifndef CAINCAMERA_PACKETQUEUE_H
#define CAINCAMERA_PACKETQUEUE_H

#include "CainPlayerDefinition.h"
#ifdef __cplusplus
extern "C" {
#endif

// 待解码包入队
int packet_queue_put_private(PacketQueue *q, AVPacket *pkt, AVPacket flush_pkt);
// 待解码包入队
int packet_queue_put(PacketQueue *q, AVPacket *pkt, AVPacket flush_pkt);
// 入队一个空的包
int packet_queue_put_nullpacket(PacketQueue *q, int stream_index, AVPacket flush_pkt);
// 待解码包队列初始化
int packet_queue_init(PacketQueue *q);
// 刷出待解码包队列中的剩余帧
void packet_queue_flush(PacketQueue *q);
// 销毁待解码包队列
void packet_queue_destroy(PacketQueue *q);
// 请求丢弃待解码包队列
void packet_queue_abort(PacketQueue *q);
// 待解码包队列开始
void packet_queue_start(PacketQueue *q, AVPacket flush_pkt);
// 待解码包队列出列
int packet_queue_get(PacketQueue *q, AVPacket *pkt, int block, int *serial);


#ifdef __cplusplus
}
#endif

#endif //CAINCAMERA_PACKETQUEUE_H
