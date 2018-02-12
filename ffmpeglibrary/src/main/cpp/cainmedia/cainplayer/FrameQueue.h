//
// Created by Administrator on 2018/2/12.
//

#ifndef CAINCAMERA_FRAMEQUEUE_H
#define CAINCAMERA_FRAMEQUEUE_H

#include "PlayerDefintion.h"
#include "PacketQueue.h"

class FrameQueue {
    Frame queue[FRAME_QUEUE_SIZE];              // 队列数组
    int rindex;                                 // 读索引
    int windex;                                 // 写索引
    int size;                                   // 大小
    int max_size;                               // 最大大小
    int keep_last;                              // 保持上一个
    int rindex_shown;                           // 读显示
    Mutex *mutex;                               // 互斥变量
    Cond *cond;                                 // 条件变量
    PacketQueue *pktq;                          // 待解码包队列
    // 初始化
    int init(PacketQueue *pktq, int max_size, int keep_last);

public:

    FrameQueue(PacketQueue *pktq, int max_size, int keep_last);

    virtual ~FrameQueue();

    // 销毁已解码帧对象
    void unref_item(Frame *vp);
    // 销毁
    void destroy();

    // 通信信号
    void signal();
    // 定位当前可读帧
    Frame *peek();
    // 定位下一可读帧
    Frame *peekNext();
    // 最后可读帧
    Frame *peekLast();
    // 定位可写帧
    Frame *peekWritable();
    // 定位可读帧
    Frame *peekReadable();
    // 入队
    void push();
    // 下一个可读帧
    void next();
    // 队列剩余帧数量
    int nbRemain();
    // 队列最后位置
    int64_t lasePos();

};


#endif //CAINCAMERA_FRAMEQUEUE_H
