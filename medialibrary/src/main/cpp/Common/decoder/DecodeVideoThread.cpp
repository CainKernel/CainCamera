//
// Created by CainHuang on 2020-02-24.
//

#include "DecodeVideoThread.h"

DecodeVideoThread::DecodeVideoThread() {
    LOGD("DecodeVideoThread::constructor()");
    av_register_all();
    mFrameQueue = nullptr;
    mVideoDemuxer = std::make_shared<AVMediaDemuxer>();
    mVideoDecoder = std::make_shared<AVVideoDecoder>(mVideoDemuxer);

    // 初始化数据包
    av_init_packet(&mPacket);
    mPacket.data = nullptr;
    mPacket.size = 0;

    mMaxFrame = MAX_FRAME / 3;
    mThread = nullptr;
    mAbortRequest = true;
    mPauseRequest = true;
    mDecodeOnPause = false;
    mSeekRequest = false;
    mSeekTime = -1;
    mSeekPos = -1;
    mStartPosition = -1;
    mEndPosition = -1;
}

DecodeVideoThread::~DecodeVideoThread() {
    release();
    LOGD("DecodeVideoThread::destructor()");
}

void DecodeVideoThread::release() {
    LOGD("DecodeVideoThread::release()");
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

/**
 * 设置解码后的存放队列
 * @param frameQueue
 */
void DecodeVideoThread::setDecodeFrameQueue(SafetyQueue<Picture *> *frameQueue) {
    mFrameQueue = frameQueue;
}

void DecodeVideoThread::setDataSource(const char *url) {
    LOGD("DecodeVideoThread::setDataSource(): %s", url);
    mVideoDemuxer->setInputPath(url);
}

/**
 * 指定解封装格式名称，比如pcm、aac、h264、mp4之类的
 * @param format
 */
void DecodeVideoThread::setInputFormat(const char *format) {
    LOGD("DecodeVideoThread::setInputFormat(): %s", format);
    mVideoDemuxer->setInputFormat(format);
}

/**
 * 指定解码器名称
 * @param decoder
 */
void DecodeVideoThread::setDecodeName(const char *decoder) {
    LOGD("DecodeVideoThread::setDecodeName(): %s", decoder);
    mVideoDecoder->setDecoder(decoder);
}

/**
 * 添加解封装参数
 */
void DecodeVideoThread::addFormatOptions(std::string key, std::string value) {
    LOGD("DecodeVideoThread::addFormatOptions(): {%s, %s}", key.c_str(), value.c_str());
    mFormatOptions[key] = value;
}

/**
 * 添加解码参数
 */
void DecodeVideoThread::addDecodeOptions(std::string key, std::string value) {
    LOGD("DecodeVideoThread::addDecodeOptions(): {%s, %s}", key.c_str(), value.c_str());
    mDecodeOptions[key] = value;
}

/**
 * 设置解码
 * @param decodeOnPause
 */
void DecodeVideoThread::setDecodeOnPause(bool decodeOnPause) {
    LOGD("DecodeVideoThread::setDecodeOnPause(): %d", decodeOnPause);
    mDecodeOnPause = decodeOnPause;
    mCondition.signal();
}

/**
 * 跳转到某个时间
 * @param timeMs
 */
void DecodeVideoThread::seekTo(float timeMs) {
    LOGD("DecodeVideoThread::seekTo(): %f ms", timeMs);
    mSeekRequest = true;
    mSeekTime = timeMs;
    mCondition.signal();
}

/**
 * 设置是否需要循环解码
 * @param looping
 */
void DecodeVideoThread::setLooping(bool looping) {
    LOGD("DecodeVideoThread::setLooping(): %d", looping);
    mLooping = looping;
    mCondition.signal();
}

/**
 * 设置解码区间
 * @param start
 * @param end
 */
void DecodeVideoThread::setRange(float start, float end) {
    LOGD("DecodeVideoThread::setRange(): {%f, %f}", start, end);
    mStartPosition = start;
    mEndPosition = end;
    mCondition.signal();
}

/**
 * 准备解码
 * @return
 */
int DecodeVideoThread::prepare() {
    int ret;
    LOGD("DecodeVideoThread::prepare()");
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
void DecodeVideoThread::start() {
    LOGD("DecodeVideoThread::start()");
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
void DecodeVideoThread::pause() {
    LOGD("DecodeVideoThread::pause()");
    mPauseRequest = true;
    mCondition.signal();
}

/**
 * 停止解码
 */
void DecodeVideoThread::stop() {
    LOGD("DecodeVideoThread::stop()");
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
void DecodeVideoThread::flush() {
    LOGD("DecodeVideoThread::flush()");
    if (mVideoDecoder != nullptr) {
        mVideoDecoder->flushBuffer();
    }
    if (mFrameQueue != nullptr) {
        while (mFrameQueue->size() > 0) {
            auto picture = mFrameQueue->pop();
            if (picture) {
                freeFrame(picture->frame);
                free(picture);
            }
        }
    }
}

/**
 * 获取宽度
 */
int DecodeVideoThread::getWidth() {
    return mVideoDecoder->getWidth();
}

/**
 * 获取高度
 */
int DecodeVideoThread::getHeight() {
    return mVideoDecoder->getHeight();
}

/**
 * 获取平均帧率
 */
int DecodeVideoThread::getFrameRate() {
    return mVideoDecoder->getFrameRate();
}

/**
 * 获取时长(ms)
 */
int64_t DecodeVideoThread::getDuration() {
    return mVideoDemuxer->getDuration();
}

/**
 * 获取旋转角度
 */
double DecodeVideoThread::getRotation() {
    return mVideoDecoder->getRotation();
}

bool DecodeVideoThread::isSeeking() {
    return mSeekRequest;
}

void DecodeVideoThread::run() {
    readPacket();
}

/**
 * 读取数据包并解码
 * @return
 */
int DecodeVideoThread::readPacket() {

    int ret = 0;
    mDecodeEnd = false;
    LOGD("DecodeVideoThread::readePacket");

    if (mStartPosition >= 0) {
        mVideoDemuxer->seekAudio(mStartPosition);
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
            mSeekTime = -1;
            mSeekRequest = false;
            mCondition.signal();
            mMutex.unlock();
            continue;
        }

        // 处于暂停状态下，暂停解码
        if (mPauseRequest && !mDecodeOnPause) {
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

        ret = readAndDecode();
        if (ret < 0) {
            break;
        }
    }
    LOGD("DecodeVideoThread exit!");
    return ret;
}

/**
 * 读取并解码
 * @return
 */
int DecodeVideoThread::readAndDecode() {
    // 读取数据包
    int ret = mVideoDemuxer->readFrame(&mPacket);
    // 解码到结尾位置，如果需要循环播放，则记录解码完成标记，等待队列消耗完
    if (ret == AVERROR_EOF && mLooping) {
        mDecodeEnd = true;
        LOGD("need to decode looping");
        return 0;
    } else if (ret < 0) { // 解码出错直接退出解码线程
        LOGE("Failed to call av_read_frame: %s", av_err2str(ret));
        return ret;
    }
    if (mPacket.stream_index < 0 || mPacket.stream_index != mVideoDecoder->getStreamIndex()
        || (mPacket.flags & AV_PKT_FLAG_CORRUPT)) {
        av_packet_unref(&mPacket);
        return 0;
    }
    decodePacket(&mPacket);
    av_packet_unref(&mPacket);
    return 0;
}

/**
 * 解码数据包
 * @param packet
 * @return 0为解码成功，小于为解码失败
 */
int DecodeVideoThread::decodePacket(AVPacket *packet) {
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

        // 定位到实际的帧中，关键帧立即上屏
        if (mSeekPos > 0 && frame->pts > 0 /*&& !(packet->flags & AV_PKT_FLAG_KEY)*/) {
            if (frame->pts < mSeekPos) {
                LOGD("skip video frame.pos: %ld, seek pos: %ld", time, mSeekTime);
                freeFrame(frame);
                break;
            } else {
                mSeekPos = -1;
            }
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
bool DecodeVideoThread::isDecodeWaiting() {
    return (mFrameQueue && mFrameQueue->size() >= mMaxFrame);
}

/**
 * 计算当前时间戳
 * @param pts
 * @param time_base
 * @return
 */
int64_t DecodeVideoThread::calculatePts(int64_t pts, AVRational time_base) {
    return (int64_t)(av_q2d(time_base) * 1000 * pts);
}

/**
 * 释放帧对象
 * @param frame
 */
void DecodeVideoThread::freeFrame(AVFrame *frame) {
    if (frame) {
        av_frame_unref(frame);
        av_frame_free(&frame);
    }
}

/**
 * 跳转到某个帧
 */
void DecodeVideoThread::seekFrame() {
    int64_t ret;
    int stream_index;
    if (!mVideoDecoder || mSeekTime == -1) {
        return;
    }

    // 定位到某个帧中，如果队列已经存在，则直接返回不做处理
    float frame_duration = 1000.0f / mVideoDecoder->getFrameRate();
    bool hasSeek = false;
    while (mFrameQueue->size() > 0) {
        Picture *picture = nullptr;
        picture = mFrameQueue->pop();
        if (picture != nullptr) {
            if (picture->pts + 2 * frame_duration < mSeekTime && picture->pts + frame_duration >= mSeekTime) {
                hasSeek = true;
            }
            LOGD("seek queue picture time: %f, mSeekTime: %f", picture->pts, mSeekTime);
            freeFrame(picture->frame);
            free(picture);
            if (hasSeek) {
                break;
            }
        }
    }

    // 存在定位帧，则直接退出，seekTime重置
    if (hasSeek) {
        mSeekPos = -1;
        return;
    }

    // 定位到实际位置中
    stream_index = mVideoDecoder->getStreamIndex();
    int64_t time = (int64_t)(mSeekTime / (1000.0f * av_q2d(mVideoDecoder->getStream()->time_base)));
    ret = mVideoDemuxer->seekVideo(time, stream_index, AVSEEK_FLAG_BACKWARD);
    if (ret < 0) {
        return;
    }
    mSeekTime = ret;
    // 清空队列的数据
    flush();

    // 解码得到先要的帧
    while (mFrameQueue->empty()) {
        readAndDecode();
    }
}