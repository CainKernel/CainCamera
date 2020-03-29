//
// Created by CainHuang on 2020-02-22.
//

#include "DecodeAudioThread.h"

DecodeAudioThread::DecodeAudioThread() {
    LOGD("DecodeAudioThread::constructor()");
    av_register_all();
    mFrameQueue = nullptr;
    mAudioDemuxer = std::make_shared<AVMediaDemuxer>();
    mAudioDecoder = std::make_shared<AVAudioDecoder>(mAudioDemuxer);

    // 初始化数据包
    av_init_packet(&mPacket);
    mPacket.data = nullptr;
    mPacket.size = 0;

    mMaxFrame = 5;
    mOutSampleRate = 44100;
    mOutChannels = 1;
    mOutFormat = AV_SAMPLE_FMT_S16;

    mFrame = av_frame_alloc();
    pSwrContext = nullptr;
    mMaxBufferSize = 4096;
    mBuffer = (uint8_t *)malloc(mMaxBufferSize);

    mThread = nullptr;
    mAbortRequest = true;
    mPauseRequest = true;
    mSeekRequest = false;
    mSeekTime = -1;
    mStartPosition = -1;
    mEndPosition = -1;

    mNextPts = AV_NOPTS_VALUE;
}

DecodeAudioThread::~DecodeAudioThread() {
    release();
    LOGD("DecodeAudioThread::destructor()");
}

/**
 * 释放资源
 */
void DecodeAudioThread::release() {
    LOGD("DecodeAudioThread::release()");
    if (mAudioDecoder != nullptr) {
        mAudioDecoder->closeDecoder();
        mAudioDecoder.reset();
        mAudioDecoder = nullptr;
    }
    if (mAudioDemuxer != nullptr) {
        mAudioDemuxer->closeDemuxer();
        mAudioDemuxer.reset();
        mAudioDemuxer = nullptr;
    }
    mAbortRequest = true;
    mFrameQueue = nullptr;
    if (pSwrContext != nullptr) {
        swr_free(&pSwrContext);
        pSwrContext = nullptr;
    }
    if (mBuffer) {
        free(mBuffer);
        mBuffer = nullptr;
    }
    if (mFrame) {
        av_frame_unref(mFrame);
        av_frame_free(&mFrame);
        mFrame = nullptr;
    }
    av_packet_unref(&mPacket);
}

/**
 * 设置解码监听器
 * @param listener
 */
void DecodeAudioThread::setOnDecodeListener(const std::shared_ptr<OnDecodeListener> &listener) {
    mDecodeListener = listener;
    mCondition.signal();
}

/**
 * 绑定解码帧队列
 * @param frameQueue
 */
void DecodeAudioThread::setDecodeFrameQueue(SafetyQueue<AVMediaData *> *frameQueue) {
    mFrameQueue = frameQueue;
}

/**
 * 设置数据源
 */
void DecodeAudioThread::setDataSource(const char *url) {
    mAudioDemuxer->setInputPath(url);
}

/**
 * 设置输入格式参数
 */
void DecodeAudioThread::setInputFormat(const char *format) {
    mAudioDemuxer->setInputFormat(format);
}

/**
 * 设置解码器名称
 */
void DecodeAudioThread::setDecodeName(const char *decoder) {
    mAudioDecoder->setDecoder(decoder);
}

/**
 * 添加解封装参数
 */
void DecodeAudioThread::addFormatOptions(std::string key, std::string value) {
    mFormatOptions[key] = value;
}

/**
 * 添加解码参数
 */
void DecodeAudioThread::addDecodeOptions(std::string key, std::string value) {
    mDecodeOptions[key] = value;
}

/**
 * 设置音频输出参数
 * @param sampleRate    采样率
 * @param channel       声道数
 * @param format        采样格式
 */
void DecodeAudioThread::setOutput(int sampleRate, int channel, AVSampleFormat format) {
    mOutSampleRate = sampleRate;
    mOutChannels = channel;
    mOutFormat = format;
}

/**
 * 定位到某个位置
 * @param timeMs
 * @return
 */
void DecodeAudioThread::seekTo(float timeMs) {
    mSeekRequest = true;
    mSeekTime = timeMs;
    mCondition.signal();
}

/**
 * 设置循环解码
 * @param looping
 */
void DecodeAudioThread::setLooping(bool looping) {
    mLooping = looping;
    mCondition.signal();
}

/**
 * 设置播放区间
 * @param start
 * @param end
 */
void DecodeAudioThread::setRange(float start, float end) {
    mStartPosition = start;
    mEndPosition = end;
    mCondition.signal();
}

/**
 * 准备解码
 * @return 准备结果
 */
int DecodeAudioThread::prepare() {
    int ret;
    LOGD("DecodeAudioThread::prepare()");
    // 打开解封装器
    ret = mAudioDemuxer->openDemuxer(mFormatOptions);
    if (ret < 0) {
        LOGE("Failed to open media demuxer");
        mAudioDemuxer.reset();
        mAudioDemuxer = nullptr;
        return ret;
    }

    // 打开音频解码器
    if (mAudioDemuxer->hasAudioStream()) {
        ret = mAudioDecoder->openDecoder(mDecodeOptions);
        if (ret < 0) {
            LOGE("Failed to open audio decoder");
            return ret;
        }
    }

    // 打印信息
    mAudioDemuxer->printInfo();

    return ret;
}

/**
 * 开始
 */
void DecodeAudioThread::start() {
    LOGD("DecodeAudioThread::start()");
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
 * 暂停
 */
void DecodeAudioThread::pause() {
    LOGD("DecodeAudioThread::pause()");
    mPauseRequest = true;
    mCondition.signal();
}

/**
 * 停止
 */
void DecodeAudioThread::stop() {
    LOGD("DecodeAudioThread::stop()");
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
 * 定位某个位置
 */
void DecodeAudioThread::seekFrame() {
    int ret = mAudioDemuxer->seekAudio(mSeekTime);
    if (ret < 0) {
        // seek 出错回调
        if (mDecodeListener.lock() != nullptr) {
            mDecodeListener.lock()->onSeekError(AVMEDIA_TYPE_AUDIO, ret);
        }
        return;
    }
    flush();
}

/**
 * 刷新缓冲区
 */
void DecodeAudioThread::flush() {
    LOGD("DecodeAudioThread::flush()");
    if (mAudioDecoder != nullptr) {
        mAudioDecoder->flushBuffer();
    }
}

int64_t DecodeAudioThread::getDuration() {
    return mAudioDemuxer->getDuration();
}

void DecodeAudioThread::run() {
    readPacket();
}

/**
 * 读取数据包并解码
 */
int DecodeAudioThread::readPacket() {
    int ret = 0;
    mDecodeEnd = false;
    LOGD("DecodeAudioThread::readePacket");
    // 初始化转码上下文
    initResampleContext();

    // 解码开始回调
    if (mDecodeListener.lock() != nullptr) {
        mDecodeListener.lock()->onDecodeStart(AVMEDIA_TYPE_AUDIO);
    }
    if (mStartPosition >= 0) {
        mAudioDemuxer->seekAudio(mStartPosition);
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
            mNextPts = AV_NOPTS_VALUE;
            float seekTime = mSeekTime >= 0 ? mSeekTime : 0;
            seekFrame();
            mSeekRequest = false;
            mSeekTime = -1;
            mCondition.signal();
            mMutex.unlock();
            // seek结束回调
            if (mDecodeListener.lock() != nullptr) {
                mDecodeListener.lock()->onSeekComplete(AVMEDIA_TYPE_AUDIO, seekTime);
            }
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
            mCondition.waitRelative(mMutex, 10 * 1000000);
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
            if (mFrameQueue != nullptr && !mFrameQueue->empty()) {
                mCondition.waitRelative(mMutex, 50 * 1000000);
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
        ret = mAudioDemuxer->readFrame(&mPacket);
        // 解码到结尾位置，如果需要循环播放，则记录解码完成标记，等待队列消耗完
        if (ret == AVERROR_EOF && mLooping) {
            mDecodeEnd = true;
            LOGD("need to decode looping");
            continue;
        } else if (ret < 0) { // 解码出错直接退出解码线程
            LOGE("Failed to call av_read_frame: %s", av_err2str(ret));
            break;
        }
        if (mPacket.stream_index < 0 || mPacket.stream_index != mAudioDecoder->getStreamIndex()) {
            av_packet_unref(&mPacket);
            continue;
        }
        decodePacket(&mPacket);
        av_packet_unref(&mPacket);
    }

    // 解码结束回调
    if (mDecodeListener.lock() != nullptr) {
        mDecodeListener.lock()->onDecodeFinish(AVMEDIA_TYPE_AUDIO);
    }

    LOGD("DecodeAudioThread exit!");
    return ret;
}

/**
 * 解码数据包
 */
int DecodeAudioThread::decodePacket(AVPacket *packet) {

    int ret = 0;

    if (!packet || packet->stream_index < 0) {
        return -1;
    }

    if (mAbortRequest) {
        return -1;
    }

    // 非音频数据包，则直接释放内存并返回
    if (packet->stream_index != mAudioDecoder->getStreamIndex()) {
        av_packet_unref(packet);
        return ret;
    }

    // 将数据包送去解码
    auto pCodecContext = mAudioDecoder->getContext();
    ret = avcodec_send_packet(pCodecContext, packet);
    if (ret < 0) {
        LOGE("Failed to call avcodec_send_packet: %s", av_err2str(ret));
        return ret;
    }

    while (ret == 0 && !mAbortRequest) {
        // 取出解码后的AVFrame
        ret = avcodec_receive_frame(pCodecContext, mFrame);
        if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) {
            av_frame_unref(mFrame);
            break;
        } else if (ret < 0) {
            LOGE("Failed to call avcodec_receive_frame: %s", av_err2str(ret));
            av_frame_unref(mFrame);
            break;
        }
        // 将解码后数据帧转码后放入队列
        if (mFrameQueue != nullptr) {
            // 音频转码
            int resample_nb_samples = (int)av_rescale_rnd(swr_get_delay(pSwrContext, mFrame->sample_rate) + mFrame->nb_samples,
                                                          mOutSampleRate,
                                                          mFrame->sample_rate, AV_ROUND_INF);
            int out_count = reallocBuffer(resample_nb_samples);
            ret = swr_convert(pSwrContext, &mBuffer, out_count,
                              (const uint8_t **) mFrame->data, mFrame->nb_samples);
            if (ret <= 0) {
                LOGE("Failed to call swr_convert : %s", av_err2str(ret));
                av_frame_unref(mFrame);
                break;
            }
            // 计算出实际的pts值
            AVRational tb = (AVRational){1, mFrame->sample_rate};
            if (mFrame->pts != AV_NOPTS_VALUE) {
                mFrame->pts = av_rescale_q(mFrame->pts, av_codec_get_pkt_timebase(mAudioDecoder->getContext()), tb);
            } else if (mNextPts != AV_NOPTS_VALUE) {
                mFrame->pts = av_rescale_q(mNextPts, mNextTimebase, tb);
            } else {
                // 如果都没找到，说明需要重新从FFmpeg中获取猜测当前的时间戳
                mFrame->pts = av_frame_get_best_effort_timestamp(mFrame);
            }
            // 如果pts存在数值，则计算出下一帧的时间戳，防止下一帧音频数据可能不存在PTS值
            if (mFrame->pts != AV_NOPTS_VALUE) {
                mNextPts = mFrame->pts + mFrame->nb_samples;
                mNextTimebase = tb;
            }
            // 复制转码后的数据到AVMediaData中
            auto data = new AVMediaData();
            data->sample_size = out_count;
            data->sample = (uint8_t *) malloc((size_t)data->sample_size);
            memcpy(data->sample, mBuffer, (size_t)data->sample_size);
            data->type = MediaAudio;
            data->pts = calculatePts(mFrame->pts, (AVRational){1, mFrame->sample_rate});
            // 将数据放入帧队列中
            mFrameQueue->push(data);
            // 比较播放结束位置的pts
            if (mEndPosition > 0 && data->pts >= mEndPosition) {
                mDecodeEnd = true;
            }
        }
        av_frame_unref(mFrame);
    }
    return ret;
}

/**
 * 初始化重采样上下文
 */
void DecodeAudioThread::initResampleContext() {
    if (!mAudioDecoder) {
        return;
    }
    pSwrContext = swr_alloc_set_opts(pSwrContext,
                                     av_get_default_channel_layout(mOutChannels), mOutFormat,
                                     mOutSampleRate,
                                     av_get_default_channel_layout(mAudioDecoder->getChannels()),
                                     mAudioDecoder->getSampleFormat(),
                                     mAudioDecoder->getSampleRate(), 0, nullptr);
    if (swr_init(pSwrContext) < 0) {
        LOGE("Failed to call swr_init");
    }
}

/**
 * 是否需要解码等待
 * @return
 */
bool DecodeAudioThread::isDecodeWaiting() {
    return (mFrameQueue != nullptr && mFrameQueue->size() >= mMaxFrame);
}

/**
 * 重新分配内存
 * @param nb_samples 采样点数量
 */
int DecodeAudioThread::reallocBuffer(int nb_samples) {
    int bufferSize = av_samples_get_buffer_size(nullptr, mOutChannels, nb_samples, mOutFormat, 1);
    if (bufferSize > mMaxBufferSize) {
        mBuffer = (uint8_t *) realloc(mBuffer, bufferSize);
        mMaxBufferSize = bufferSize;
    }
    // 清除旧数据
    memset(mBuffer, 0, mMaxBufferSize);
    return bufferSize;
}

/**
 * 计算当前时间戳
 * @param pts
 * @param time_base
 * @return
 */
int64_t DecodeAudioThread::calculatePts(int64_t pts, AVRational time_base) {
    return (int64_t)(av_q2d(time_base) * 1000 * pts);
}
