//
// Created by Administrator on 2018/3/21.
//

#include "BaseDecoder.h"
#include "native_log.h"

BaseDecoder::BaseDecoder() {
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
}

BaseDecoder::~BaseDecoder() {
    packetFlush();
    ThreadDestroy(mThread);
    MutexDestroy(mMutex);
    CondDestroy(mCondition);
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
    MutexLock(mMutex);
    while (mPacketQueue.size() > 0) {
        AVPacket *pkt = mPacketQueue[0];
        mPacketQueue.erase(mPacketQueue.begin());
        av_packet_unref(pkt);
        pkt = NULL;
    }
    std::vector<AVPacket *>().swap(mPacketQueue);
    MutexUnlock(mMutex);
}

/**
 * 裸数据包入队
 * @param packet
 */
void BaseDecoder::put(AVPacket *packet) {
    MutexLock(mMutex);
    mPacketQueue.push_back(packet);
    MutexUnlock(mMutex);
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
}

/**
 * 获取裸数据包大小
 * @return
 */
int BaseDecoder::packetSize() {
    return mPacketQueue.size();
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
    MutexLock(mMutex);
    CondSignal(mCondition);
    MutexUnlock(mMutex);
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