//
// Created by Administrator on 2018/2/8.
//

#include "CainFrameQueue.h"

#ifdef __cplusplus
extern "C" {
#endif

/**
 * 销毁frame对象
 * @param vp
 */
void frame_queue_unref_item(Frame *vp)
{
    // 销毁帧
    av_frame_unref(vp->frame);
}


/**
 * 已解码帧队列初始化
 * @param  f         已解码帧队列
 * @param  pktq      待解码包队列
 * @param  max_size  队列最大缓存数
 * @param  keep_last 是否保持最后一帧
 * @return
 */
int frame_queue_init(FrameQueue *f, PacketQueue *pktq, int max_size, int keep_last)
{
    int i;
    // 为帧队列分配内存
    memset(f, 0, sizeof(FrameQueue));
    // 创建互斥锁
    if (!(f->mutex = Cain_CreateMutex())) {
        av_log(NULL, AV_LOG_FATAL, "Cain_CreateMutex(): %s\n", Cain_GetError());
        return AVERROR(ENOMEM);
    }
    // 创建条件锁
    if (!(f->cond = Cain_CreateCond())) {
        av_log(NULL, AV_LOG_FATAL, "Cain_CreateCond(): %s\n", Cain_GetError());
        return AVERROR(ENOMEM);
    }
    f->pktq = pktq;
    f->max_size = FFMIN(max_size, FRAME_QUEUE_SIZE);
    f->keep_last = !!keep_last;
    // 初始化队列中的AVFrame
    for (i = 0; i < f->max_size; i++) {
        if (!(f->queue[i].frame = av_frame_alloc()))
            return AVERROR(ENOMEM);
    }
    return 0;
}

/**
 * 销毁已解码帧队列
 * @param f 已解码帧队列
 */
void frame_queue_destory(FrameQueue *f)
{
    int i;
    // 销毁队列中的AVFrame
    for (i = 0; i < f->max_size; i++) {
        Frame *vp = &f->queue[i];
        frame_queue_unref_item(vp);
        av_frame_free(&vp->frame);
    }
    Cain_DestroyMutex(f->mutex);
    Cain_DestroyCond(f->cond);
}

/**
 * 已解码帧队列信号
 * @param f  已解码帧队列
 */
void frame_queue_signal(FrameQueue *f)
{
    Cain_LockMutex(f->mutex);
    Cain_CondSignal(f->cond);
    Cain_UnlockMutex(f->mutex);
}


/**
 * 查找/定位可读帧
 * @param  f 已解码帧队列
 * @return   返回定位到的Frame对象
 */
Frame *frame_queue_peek(FrameQueue *f)
{
    return &f->queue[(f->rindex + f->rindex_shown) % f->max_size];
}

/**
 * 查找/定位下一可读帧
 * @param  f 已解码帧队列
 * @return   返回定位到的Frame对象
 */
Frame *frame_queue_peek_next(FrameQueue *f)
{
    return &f->queue[(f->rindex + f->rindex_shown + 1) % f->max_size];
}

/**
 * 查找/定位最后一可读帧
 * @param  f 已解码帧队列
 * @return   返回定位到的Frame对象
 */
Frame *frame_queue_peek_last(FrameQueue *f)
{
    return &f->queue[f->rindex];
}

/**
 * 查找/定位可写帧
 * @param  f 已解码帧队列
 * @return   返回定位到的Frame对象
 */
Frame *frame_queue_peek_writable(FrameQueue *f)
{
    /* wait until we have space to put a new frame */
    Cain_LockMutex(f->mutex);
    while (f->size >= f->max_size &&
           !f->pktq->abort_request) {
        Cain_CondWait(f->cond, f->mutex);
    }
    Cain_UnlockMutex(f->mutex);

    if (f->pktq->abort_request) {
        return NULL;
    }

    return &f->queue[f->windex];
}

/**
 * 查找/定位可读帧
 * @param  f 已解码帧队列
 * @return   返回定位到的Frame对象
 */
Frame *frame_queue_peek_readable(FrameQueue *f)
{
    /* wait until we have a readable a new frame */
    Cain_LockMutex(f->mutex);
    // 如果没有读出数据，则一直等待
    while (f->size - f->rindex_shown <= 0 &&
           !f->pktq->abort_request) {
        Cain_CondWait(f->cond, f->mutex);
    }
    Cain_UnlockMutex(f->mutex);

    if (f->pktq->abort_request) {
        return NULL;
    }

    return &f->queue[(f->rindex + f->rindex_shown) % f->max_size];
}


/**
 * 入队可写帧
 * @param f 已解码帧队列
 */
void frame_queue_push(FrameQueue *f)
{
    if (++f->windex == f->max_size)
        f->windex = 0;
    Cain_LockMutex(f->mutex);
    f->size++;
    Cain_CondSignal(f->cond);
    Cain_UnlockMutex(f->mutex);
}

/**
 * 定位到下一可读帧
 * @param f [description]
 */
void frame_queue_next(FrameQueue *f)
{
    if (f->keep_last && !f->rindex_shown) {
        f->rindex_shown = 1;
        return;
    }
    // 释放帧
    frame_queue_unref_item(&f->queue[f->rindex]);
    if (++f->rindex == f->max_size) {
        f->rindex = 0;
    }

    Cain_LockMutex(f->mutex);
    f->size--;
    Cain_CondSignal(f->cond);
    Cain_UnlockMutex(f->mutex);
}

/* return the number of undisplayed frames in the queue */
/**
 * 队列剩余帧数量
 * @param  f [description]
 * @return   [description]
 */
int frame_queue_nb_remaining(FrameQueue *f)
{
    return f->size - f->rindex_shown;
}

/* return last shown position */
/**
 * 帧队列最后位置
 * @param  f [description]
 * @return   [description]
 */
int64_t frame_queue_last_pos(FrameQueue *f)
{
    Frame *fp = &f->queue[f->rindex];
    if (f->rindex_shown && fp->serial == f->pktq->serial) {
        return fp->pos;
    }
    return -1;
}



#ifdef __cplusplus
}
#endif