//
// Created by Administrator on 2018/2/12.
//

#ifndef CAINCAMERA_PACKETQUEUE_H
#define CAINCAMERA_PACKETQUEUE_H

#include "PlayerDefintion.h"


class PacketQueue {
private:
    // 队列头尾指针
    MyAVPacketList *first_pkt, *last_pkt;
    // 待解码的数量
    int nb_packets;
    // 待解码的总大小
    int size;
    // 待解码的总时长
    int64_t duration;
    // 取消入队标志
    int abort_request;
    // 序列
    int serial;
    // 互斥锁
    Mutex *mutex;
    // 条件锁
    Cond *cond;

    // 初始化
    int init();

    // 销毁队列
    void destroy();

public:

    PacketQueue();
    virtual ~PacketQueue();
    // 入队
    int put(AVPacket *pkt, AVPacket flush_PKt);
    // 入队空数据包
    int putNullPacket(int stream_index, AVPacket flush_pkt);
    // 刷出剩余帧
    void flush();
    // 请求舍弃待解码包
    void abort();
    // 队列开始
    void start(AVPacket flush_pkt);
    // 获取包
    int get(AVPacket *pkt, int block, int *serial);

    // 是否处于舍弃状态
    int isAbort();
    // 获取序列
    int getSerial();

};


#endif //CAINCAMERA_PACKETQUEUE_H
