//
// Created by Administrator on 2018/3/21.
//

#include "BaseDecoder.h"
#include "native_log.h"

BaseDecoder::BaseDecoder() {
    mPacketQueue = new PacketQueue();
    mFrameQueue = new FrameQueue();
    mMutex = MutexCreate();
    mCondition = CondCreate();
    mStreamIndex = -1;
    mStream = NULL;
    mCodecCtx = NULL;
    mCodec = NULL;
    mThread = NULL;
    mPrepared = false;
    mOpenSuccess = false;
    mAbortRequest = true;
    mPaused = false;
    mPacketPending = false;
}

BaseDecoder::~BaseDecoder() {
    if (mPacketQueue != NULL) {
        delete (mPacketQueue);
        mPacketQueue = NULL;
    }
    if (mFrameQueue != NULL) {
        delete (mFrameQueue);
        mFrameQueue = NULL;
    }
    ThreadDestroy(mThread);
    MutexDestroy(mMutex);
    CondDestroy(mCondition);
    av_packet_unref(&packet);
    av_packet_unref(&pkt_temp);
    avcodec_free_context(&mCodecCtx);
    mMutex = NULL;
    mCondition = NULL;
    mThread = NULL;
    mPrepared = false;
    mOpenSuccess = false;
    mAbortRequest = true;
    mPaused = false;
}

/**
 * 设置媒体流
 * @param stream
 * @param streamIndex
 */
void BaseDecoder::setAVStream(AVStream *stream, int streamIndex) {
    mStream = stream;
    mStreamIndex = streamIndex;
}

/**
 * 打开媒体流
 * @return
 */
int BaseDecoder::openStream() {
    int ret = 0;
    // 创建解码上下文
    mCodecCtx = avcodec_alloc_context3(NULL);
    if (!mCodecCtx) {
        ALOGE("Failed to alloc AVCodecContext\n");
        return -1;
    }
    // 复制解码参数
    ret = avcodec_parameters_to_context(mCodecCtx, mStream->codecpar);
    if (ret < 0) {
        ALOGE("Failed to copy parameters to context\n");
        return -1;
    }

    // 创建解码器
    mCodec = avcodec_find_decoder(mCodecCtx->codec_id);
    if (!mCodec) {
        ALOGE("Failed to find decoder\n");
        return -1;
    }

    // 创建解码线程
    mThread = ThreadCreate(decodeThread, this, "DecodeThread");
    if (!mThread) {
        ALOGE("Failed to create decode thread\n");
    }
    mOpenSuccess = true;
    return 0;
}

/**
 * 刷出剩余裸数据
 */
void BaseDecoder::packetFlush() {
    if (mPacketQueue != NULL) {
        mPacketQueue->flush();
    }
}

/**
 * 裸数据包入队
 * @param packet
 */
void BaseDecoder::put(AVPacket *packet) {
    if (mPacketQueue != NULL) {
        mPacketQueue->put(packet);
    }
}

/**
 * 入队一个空的裸数据包
 * @param streamIndex
 */
void BaseDecoder::putNullPacket(int streamIndex) {
    AVPacket pkt1, *pkt = &pkt1;
    av_init_packet(pkt);
    pkt->data = NULL;
    pkt->size = 0;
    pkt->stream_index = streamIndex;
    put(pkt);
}

/**
 * 开始解码
 */
void BaseDecoder::start() {
    if (!mPrepared) {
        mPrepared = true;
    }
    if (mOpenSuccess) {
        mAbortRequest = false;
        mPaused = false;
    }
}

/**
 * 暂停解码
 */
void BaseDecoder::pause() {
    mPaused = true;
}

/**
 * 停止解码
 */
void BaseDecoder::stop() {
    mAbortRequest = true;
    if (mPacketQueue != NULL) {
        mPacketQueue->setAbort(true);
    }
    if (mFrameQueue != NULL) {
        mFrameQueue->setAbort(true);
    }
}

/**
 * 获取裸数据包大小
 * @return
 */
int BaseDecoder::packetSize() {
    if (mPacketQueue == NULL) {
        return 0;
    }
    return mPacketQueue->size();
}

/**
 * 获取媒体流索引
 * @return
 */
int BaseDecoder::getStreamIndex() {
    return mStreamIndex;
}

/**
 * 通知解除条件锁
 */
void BaseDecoder::notify() {
    CondSignal(mCondition);
}

/**
 * 解码线程
 * @param arg
 * @return
 */
int BaseDecoder::decodeThread(void *arg) {
    BaseDecoder *decoder = (BaseDecoder *) arg;
    decoder->decodeFrame();
    return 0;
}