//
// Created by Administrator on 2018/2/8.
//

#ifndef CAINCAMERA_FRAMEQUEUE_H
#define CAINCAMERA_FRAMEQUEUE_H
#ifdef __cplusplus
extern "C" {
#endif

#include "CainPlayerDefinition.h"


// 销毁已解码帧对象
void frame_queue_unref_item(Frame *vp);
// 已解码帧队列初始化
int frame_queue_init(FrameQueue *f, PacketQueue *pktq, int max_size, int keep_last);
// 销毁已解码帧队列
void frame_queue_destory(FrameQueue *f);
// 已解码帧队列信号
void frame_queue_signal(FrameQueue *f);
// 查找/定位可读帧
Frame *frame_queue_peek(FrameQueue *f);
// 查找/定位下一可读帧
Frame *frame_queue_peek_next(FrameQueue *f);
// 查找/定位最后可读帧
Frame *frame_queue_peek_last(FrameQueue *f);
// 查找/定位可写帧
Frame *frame_queue_peek_writable(FrameQueue *f);
// 查找/定位可读帧
Frame *frame_queue_peek_readable(FrameQueue *f);
// 入队可写帧
void frame_queue_push(FrameQueue *f);
// 查找/定位下一可读帧
void frame_queue_next(FrameQueue *f);
// 队列剩余帧数量
int frame_queue_nb_remaining(FrameQueue *f);
// 队列最后位置
int64_t frame_queue_last_pos(FrameQueue *f);

#ifdef __cplusplus
}
#endif

#endif //CAINCAMERA_FRAMEQUEUE_H
