//
// Created by CainHuang on 2020-02-24.
//

#include "VideoDecodeThread.h"

VideoDecodeThread::VideoDecodeThread(SafetyQueue<Picture*> *frameQueue) {
    LOGD("VideoDecodeThread::constructor()");
    av_register_all();
    mFrameQueue = frameQueue;
    mVideoDemuxer = std::make_shared<AVMediaDemuxer>();
    mVideoDecoder = std::make_shared<AVVideoDecoder>(mVideoDemuxer);

    // 初始化数据包
    av_init_packet(&mPacket);
    mPacket.data = nullptr;
    mPacket.size = 0;

    mMaxFrame = 2;
    mThread = nullptr;
    mAbortRequest = true;
    mPauseRequest = true;
    mSeekRequest = false;
    mSeekTime = -1;
    mStartPosition = -1;
    mEndPosition = -1;
}

VideoDecodeThread::~VideoDecodeThread() {
    release();
    LOGD("VideoDecodeThread::destructor()");
}

void VideoDecodeThread::release() {
    LOGD("VideoDecodeThread::release()");
    if (mVideoDecoder != nullptr) {
        mVideoDecoder->closeDecoder();
        mVideoDecoder.reset();
        mVideoDecoder = nullptr;
    }
    if (mVideoDemuxer != nullptr) {
        mVideoDemuxer->closeDemuxer();
        mVideoDemuxer.reset();
        mVideoDemuxer = nullptr;
    }
    mAbortRequest = true;
    mFrameQueue = nullptr;
}

void VideoDecodeThread::setDataSource(const char *url) {
    LOGD("VideoDecodeThread::setDataSource(): %s", url);
    mVideoDemuxer->setInputPath(url);
}

/**
 * 指定解封装格式名称，比如pcm、aac、h264、mp4之类的
 * @param format
 */
void VideoDecodeThread::setInputFormat(const char *format) {
    LOGD("VideoDecodeThread::setInputFormat(): %s", format);
    mVideoDemuxer->setInputFormat(format);
}

/**
 * 指定解码器名称
 * @param decoder
 */
void VideoDecodeThread::setDecodeName(const char *decoder) {
    LOGD("VideoDecodeThread::setDecodeName(): %s", decoder);
    mVideoDecoder->setDecoder(decoder);
}

/**
 * 添加解封装参数
 */
void VideoDecodeThread::addFormatOptions(std::string key, std::string value) {
    LOGD("VideoDecodeThread::addFormatOptions(): {%s, %s}", key.c_str(), value.c_str());
    mFormatOptions[key] = value;
}

/**
 * 添加解码参数
 */
void VideoDecodeThread::addDecodeOptions(std::string key, std::string value) {
    LOGD("VideoDecodeThread::addDecodeOptions(): {%s, %s}", key.c_str(), value.c_str());
    mDecodeOptions[key] = value;
}

/**
 * 跳转到某个时间
 * @param timeMs
 */
void VideoDecodeThread::seekTo(float timeMs) {
    LOGD("VideoDecodeThread::seekTo(): %f ms", timeMs);
    mSeekRequest = true;
    mSeekTime = timeMs;
    mCondition.signal();
}

/**
 * 设置是否需要循环解码
 * @param looping
 */
void VideoDecodeThread::setLooping(bool looping) {
    LOGD("VideoDecodeThread::setLooping(): %d", looping);
    mLooping = looping;
    mCondition.signal();
}

/**
 * 设置解码区间
 * @param start
 * @param end
 */
void VideoDecodeThread::setRange(float start, float end) {
    LOGD("VideoDecodeThread::setRange(): {%f, %f}", start, end);
    mStartPosition = start;
    mEndPosition = end;
    mCondition.signal();
}

/**
 * 准备解码
 * @return
 */
int VideoDecodeThread::prepare() {
    int ret;
    LOGD("VideoDecodeThread::prepare()");
    // 打开解封装器
    ret = mVideoDemuxer->openDemuxer(mFormatOptions);
    if (ret < 0) {
        LOGE("Failed to open media demuxer");
        mVideoDemuxer.reset();
        mVideoDemuxer = nullptr;
        return ret;
    }

    // 打开音频解码器
    if (mVideoDemuxer->hasAudioStream()) {
        ret = mVideoDecoder->openDecoder(mDecodeOptions);
        if (ret < 0) {
            LOGE("Failed to open audio decoder");
            return ret;
        }
    }

    // 打印信息
    mVideoDemuxer->printInfo();

    return ret;
}

/**
 * 开始解码
 */
void VideoDecodeThread::start() {
    LOGD("VideoDecodeThread::start()");
    mAbortRequest = false;
    mPauseRequest = false;
    mCondition.signal();
    if (mThread == nullptr) {
        mThread = new Thread(this);
    }
    if (!mThread->isActive()) {
        mThread->start();
    }
}

/**
 * 暂停解码
 */
void VideoDecodeThread::pause() {
    LOGD("VideoDecodeThread::pause()");
    mPauseRequest = true;
    mCondition.signal();
}

/**
 * 停止解码
 */
void VideoDecodeThread::stop() {
    LOGD("VideoDecodeThread::stop()");
    mAbortRequest = true;
    mCondition.signal();
    if (mThread != nullptr && mThread->isActive()) {
        mThread->join();
    }
    if (mThread) {
        delete mThread;
        mThread = nullptr;
    }
}

/**
 * 清空解码缓冲区和视频帧队列
 */
void VideoDecodeThread::flush() {
    LOGD("VideoDecodeThread::flush()");
    if (mVideoDecoder != nullptr) {
        mVideoDecoder->flushBuffer();
    }
    if (mFrameQueue != nullptr) {
        while (mFrameQueue->size() > 0) {
            auto picture = mFrameQueue->pop();
            freeFrame(picture->frame);
            free(picture);
        }
    }
}

/**
 * 获取宽度
 */
int VideoDecodeThread::getWidth() {
    return mVideoDecoder->getWidth();
}

/**
 * 获取高度
 */
int VideoDecodeThread::getHeight() {
    return mVideoDecoder->getHeight();
}

/**
 * 获取平均帧率
 */
int VideoDecodeThread::getAvgFrameRate() {
    return mVideoDecoder->getFrameRate();
}

/**
 * 获取时长(ms)
 */
int64_t VideoDecodeThread::getDuration() {
    return mVideoDemuxer->getDuration();
}

/**
 * 获取旋转角度
 */
double VideoDecodeThread::getRotation() {
    return mVideoDecoder->getRotation();
}

void VideoDecodeThread::run() {
    readPacket();
}

/**
 * 读取数据包并解码
 * @return
 */
int VideoDecodeThread::readPacket() {

    int ret = 0;
    mDecodeEnd = false;
    LOGD("VideoDecodeThread::readePacket");

    if (mStartPosition >= 0) {
        mVideoDemuxer->seekTo(mStartPosition);
    }

    while (true) {

        mMutex.lock();
        // 退出解码
        if (mAbortRequest) {
            flush();
            mMutex.unlock();
            break;
        }

        // 定位处理
        if (mSeekRequest) {
            seekFrame();
            mMutex.unlock();
            continue;
        }

        // 处于暂停状态下，睡眠10毫秒继续下一轮循环
        if (mPauseRequest) {
            mCondition.wait(mMutex);
            mMutex.unlock();
            continue;
        }

        // 锁定10毫秒之后继续下一轮做法
        if (isDecodeWaiting()) {
            mCondition.waitRelativeMs(mMutex, 10);
            mMutex.unlock();
            continue;
        }

        // 如果需要循环播放等待音频帧队列消耗完，然后定位到起始位置
        if (mDecodeEnd) {
            // 非循环解码，直接退出解码线程
            if (!mLooping) {
                break;
            }
            // 等待音频帧消耗完
            if (!mFrameQueue->empty()) {
                mCondition.waitRelativeMs(mMutex, 50);
                mMutex.unlock();
                continue;
            }
            mDecodeEnd = false;
            // 定位到开始位置
            float position = mStartPosition;
            if (position < 0) {
                position = 0;
            }
            seekTo(position);
            mMutex.unlock();
            continue;
        }
        mMutex.unlock();

        // 读取数据包
        ret = mVideoDemuxer->readFrame(&mPacket);
        // 解码到结尾位置，如果需要循环播放，则记录解码完成标记，等待队列消耗完
        if (ret == AVERROR_EOF && mLooping) {
            mDecodeEnd = true;
            LOGD("need to decode looping");
            continue;
        } else if (ret < 0) { // 解码出错直接退出解码线程
            LOGE("Failed to call av_read_frame: %s", av_err2str(ret));
            break;
        }
        if (mPacket.stream_index < 0 || mPacket.stream_index != mVideoDecoder->getStreamIndex()) {
            av_packet_unref(&mPacket);
            continue;
        }
        decodePacket(&mPacket);
        av_packet_unref(&mPacket);
    }
    LOGD("VideoDecodeThread exit!");
    return ret;
}

/**
 * 解码数据包
 * @param packet
 * @return 0为解码成功，小于为解码失败
 */
int VideoDecodeThread::decodePacket(AVPacket *packet) {
    int ret = 0;

    if (!packet || packet->stream_index < 0) {
        return -1;
    }

    if (mAbortRequest) {
        return -1;
    }

    // 非视频数据包则直接释放内存并返回
    if (packet->stream_index != mVideoDecoder->getStreamIndex()) {
        av_packet_unref(packet);
        return 0;
    }

    // 将数据包送去解码
    auto pCodecContext = mVideoDecoder->getContext();
    ret = avcodec_send_packet(pCodecContext, packet);
    if (ret < 0) {
        LOGE("Failed to call avcodec_send_packet: %s", av_err2str(ret));
        return ret;
    }

    while (ret == 0 && !mAbortRequest) {
        // 取出解码后的AVFrame
        AVFrame *frame = av_frame_alloc();
        ret = avcodec_receive_frame(pCodecContext, frame);
        if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) {
            freeFrame(frame);
            break;
        } else if (ret < 0) {
            LOGE("Failed to call avcodec_receive_frame: %s", av_err2str(ret));
            freeFrame(frame);
            break;
        }

        // 将解码后数据帧转码后放入队列
        if (mFrameQueue != nullptr) {
            Picture *picture = (Picture *) malloc(sizeof(Picture));
            memset(picture, 0, sizeof(Picture));
            picture->frame = frame;
            picture->pts = calculatePts(frame->pts, mVideoDecoder->getStream()->time_base);
            // 将数据放入帧队列中
            mFrameQueue->push(picture);
            // 比较播放结束位置的pts
            if (mEndPosition > 0 && picture->pts >= mEndPosition) {
                mDecodeEnd = true;
            }
        } else {
            freeFrame(frame);
        }
    }

    return ret;
}

/**
 * 是否需要解码等待
 * @return
 */
bool VideoDecodeThread::isDecodeWaiting() {
    return (mFrameQueue && mFrameQueue->size() >= mMaxFrame);
}

/**
 * 计算当前时间戳
 * @param pts
 * @param time_base
 * @return
 */
int64_t VideoDecodeThread::calculatePts(int64_t pts, AVRational time_base) {
    return (int64_t)(av_q2d(time_base) * 1000 * pts);
}

/**
 * 释放帧对象
 * @param frame
 */
void VideoDecodeThread::freeFrame(AVFrame *frame) {
    if (frame) {
        av_frame_unref(frame);
        av_frame_free(&frame);
    }
}

/**
 * 跳转到某个帧
 */
void VideoDecodeThread::seekFrame() {
    mSeekRequest = false;
    mVideoDemuxer->seekTo(mSeekTime);
    mSeekTime = -1;
    flush();
}