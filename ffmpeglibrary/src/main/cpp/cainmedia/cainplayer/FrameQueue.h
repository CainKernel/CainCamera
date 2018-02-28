//
// Created by cain on 2018/2/25.
//

#ifndef CAINCAMERA_FRAMEQUEUE_H
#define CAINCAMERA_FRAMEQUEUE_H

#ifdef __cplusplus
extern "C" {
#endif

#include "libavutil/frame.h"
#include "libavcodec/avcodec.h"

#ifdef __cplusplus
}
#endif

#include <pthread.h>
#include "PacketQueue.h"

#ifndef MAX_FRAME_QUEUE_SIZE
#define MAX_FRAME_QUEUE_SIZE 16
#endif

// 帧结构体
typedef struct Frame {
    Frame *next;        // 下一帧指针
    AVFrame *frame;		// 帧数据
    AVSubtitle sub;		// 字幕
    int serial;			// 序列
    double pts;			// 帧的显示时间戳
    double duration;	// 帧显示时长
    int64_t pos;		// 文件中的位置
    int width;			// 帧的宽度
    int height;			// 帧的高度
    int format;		    // 格式
    AVRational sar;		// 额外参数
    int uploaded;		// 上载
    int flip_v;			// 反转
} Frame;

class FrameQueue {
public:
    FrameQueue(PacketQueue *pktq, int max_size, int keep_last);
    virtual ~FrameQueue();
    void unref(Frame *vp);
    void signal(void);
    Frame *peek(void);
    Frame *peekNext(void);
    Frame *peekLast(void);
    Frame *peekWritable(void);
    Frame *peekReadable(void);
    void push(void);
    void next(void);
    int nbRemaining(void);
    int64_t lastPos(void);
    void lock(void);
    void unlock(void);

private:
    Frame queue[MAX_FRAME_QUEUE_SIZE];	// 队列数组
    int rindex;                         // 读索引
    int windex;                         // 写索引
    int mSize;                          // 大小
    int maxSize;                        // 最大值
    int keepLast;                       // 保持上一帧
    int rindexShown;                    // 读显示索引
    pthread_mutex_t mLock;
    pthread_cond_t mCondition;
    PacketQueue *pktq;                  // 裸数据队列
};


#endif //CAINCAMERA_FRAMEQUEUE_H
