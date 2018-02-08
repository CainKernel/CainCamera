//
// Created by Administrator on 2018/2/8.
//

#include "CainPacketQueue.h"

/**
 * 待解码包入队
 * @param q     队列
 * @param pkt   待入队的包AVPacket
 * @param flush_pkt
 * @return      入队成功返回0，否则返回-1
 */
int packet_queue_put_private(PacketQueue *q, AVPacket *pkt, AVPacket flush_pkt)
{
    MyAVPacketList *pkt1;

    // 如果处于舍弃状态，则直接返回-1
    if (q->abort_request)
        return -1;

    // 创建一个包
    pkt1 = (MyAVPacketList *)av_malloc(sizeof(MyAVPacketList));
    if (!pkt1)
        return -1;
    pkt1->pkt = *pkt;
    pkt1->next = NULL;
    // 判断包是否数据flush类型，调整包序列
    if (pkt == &flush_pkt)
        q->serial++;
    pkt1->serial = q->serial;

    // 调整指针
    if (!q->last_pkt)
        q->first_pkt = pkt1;
    else
        q->last_pkt->next = pkt1;
    q->last_pkt = pkt1;
    q->nb_packets++;
    q->size += pkt1->pkt.size + sizeof(*pkt1);
    q->duration += pkt1->pkt.duration;
    /* XXX: should duplicate packet data in DV case */
    // 条件信号
    Cain_CondSignal(q->cond);
    return 0;
}

/**
 * 待解码包入队
 * @param q     队列
 * @param pkt   待入队的包AVPacket
 * @return      入队成功返回0，否则返回-1
 */
int packet_queue_put(PacketQueue *q, AVPacket *pkt, AVPacket flush_pkt)
{
    int ret;

    Cain_LockMutex(q->mutex);
    ret = packet_queue_put_private(q, pkt, flush_pkt);
    Cain_UnlockMutex(q->mutex);

    // 如果不是flush类型的包，并且没有成功入队，则销毁当前的包
    if (pkt != &flush_pkt && ret < 0)
        av_packet_unref(pkt);

    return ret;
}

/**
 * 入队一个空的包
 * @param q             队列
 * @param stream_index  流
 * @return
 */
int packet_queue_put_nullpacket(PacketQueue *q, int stream_index, AVPacket flush_pkt)
{
    // 创建一个空数据的包
    AVPacket pkt1, *pkt = &pkt1;
    av_init_packet(pkt);
    pkt->data = NULL;
    pkt->size = 0;
    pkt->stream_index = stream_index;
    return packet_queue_put(q, pkt, flush_pkt);
}

/* packet queue handling */
/**
 * 待解码包队列初始化
 * @param  q 待解码包队列
 * @return   初始化成功返回0
 */
int packet_queue_init(PacketQueue *q)
{
    // 为一个包队列分配内存
    memset(q, 0, sizeof(PacketQueue));
    // 创建互斥锁
    q->mutex = Cain_CreateMutex();
    if (!q->mutex) {
        av_log(NULL, AV_LOG_FATAL, "Cain_CreateMutex(): %s\n", Cain_GetError());
        return AVERROR(ENOMEM);
    }
    // 创建条件锁
    q->cond = Cain_CreateCond();
    if (!q->cond) {
        av_log(NULL, AV_LOG_FATAL, "Cain_CreateCond(): %s\n", Cain_GetError());
        return AVERROR(ENOMEM);
    }
    // 默认情况舍弃入队的数据
    q->abort_request = 1;
    return 0;
}

/**
 * 刷出待解码包队列中剩余帧
 * @param q 待解码包队列
 */
void packet_queue_flush(PacketQueue *q)
{
    MyAVPacketList *pkt, *pkt1;
    // 刷出包队列的剩余帧并释放掉
    Cain_LockMutex(q->mutex);
    for (pkt = q->first_pkt; pkt; pkt = pkt1) {
        pkt1 = pkt->next;
        av_packet_unref(&pkt->pkt);
        av_freep(&pkt);
    }
    q->last_pkt = NULL;
    q->first_pkt = NULL;
    q->nb_packets = 0;
    q->size = 0;
    q->duration = 0;
    Cain_UnlockMutex(q->mutex);
}

/**
 * 销毁待解码包队列
 * @param q 待解码包队列
 */
void packet_queue_destroy(PacketQueue *q)
{
    // 刷出剩余包
    packet_queue_flush(q);
    // 销毁锁
    Cain_DestroyMutex(q->mutex);
    Cain_DestroyCond(q->cond);
}

/**
 * 请求丢弃待解码包队列
 * @param q 待解码包队列
 */
void packet_queue_abort(PacketQueue *q)
{
    Cain_LockMutex(q->mutex);

    q->abort_request = 1;

    Cain_CondSignal(q->cond);

    Cain_UnlockMutex(q->mutex);
}

/**
 * 待解码包队列开始
 * @param q 待解码包队列
 */
void packet_queue_start(PacketQueue *q, AVPacket flush_pkt)
{
    Cain_LockMutex(q->mutex);
    q->abort_request = 0;
    packet_queue_put_private(q, &flush_pkt, flush_pkt);
    Cain_UnlockMutex(q->mutex);
}

/* return < 0 if aborted, 0 if no packet and > 0 if packet.  */
/**
 * 包数据出列
 * @param  q      待解码包队列
 * @param  pkt    用于存放取出的待解码包
 * @param  block  是否加锁等待，为1时，如果队列为空，则一直等待
 * @param  serial 当前待解码包的序列
 * @return        取出结果，为1表示存在，为0表示空队列，-1表示舍弃状态
 */
int packet_queue_get(PacketQueue *q, AVPacket *pkt, int block, int *serial)
{
    MyAVPacketList *pkt1;
    int ret;

    Cain_LockMutex(q->mutex);

    for (;;) {
        // 如果处于舍弃状态，直接返回
        if (q->abort_request) {
            ret = -1;
            break;
        }
        // 出列包
        pkt1 = q->first_pkt;
        if (pkt1) {
            q->first_pkt = pkt1->next;
            if (!q->first_pkt)
                q->last_pkt = NULL;
            q->nb_packets--;
            q->size -= pkt1->pkt.size + sizeof(*pkt1);
            q->duration -= pkt1->pkt.duration;
            *pkt = pkt1->pkt;
            // 更新序列
            if (serial)
                *serial = pkt1->serial;
            av_free(pkt1);
            ret = 1;
            break;
        } else if (!block) {
            ret = 0;
            break;
        } else { // 等待
            Cain_CondWait(q->cond, q->mutex);
        }
    }
    Cain_UnlockMutex(q->mutex);
    return ret;
}
