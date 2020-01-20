//
// Created by CainHuang on 2020-01-19.
//

#include "VideoReader.h"

VideoReader::VideoReader() {
    av_register_all();
    reset();
}

VideoReader::~VideoReader() {
    release();
    mAbortRequest = true;
    if (mSrcPath) {
        av_freep(&mSrcPath);
        mSrcPath = nullptr;
    }
    if (mFormat) {
        av_freep(&mFormat);
        mFormat = nullptr;
    }
    if (mVideoCodecName) {
        av_freep(&mVideoCodecName);
        mVideoCodecName = nullptr;
    }
}

void VideoReader::reset() {
    mSrcPath = nullptr;
    mReadListener = nullptr;
    mVideoCodecName = nullptr;
    mFormat = nullptr;
    mSrcPath = nullptr;

    // 初始化数据包
    av_init_packet(&mPacket);
    mPacket.data = nullptr;
    mPacket.size = 0;
    mAbortRequest = false;
}

void VideoReader::release() {
    if (mVideoDecoder != nullptr) {
        mVideoDecoder->closeDecoder();
        mVideoDecoder.reset();
        mVideoDecoder = nullptr;
    }
    if (mMediaDemuxer != nullptr) {
        mMediaDemuxer->closeDemuxer();
        mMediaDemuxer.reset();
        mMediaDemuxer = nullptr;
    }
    if (mAutoRelease) {
        if (mReadListener != nullptr) {
            delete mReadListener;
        }
    }
    mReadListener = nullptr;
}

void VideoReader::setDataSource(const char *url) {
    mSrcPath = av_strdup(url);
}

void VideoReader::setInputFormat(const char *format) {
    mFormat = av_strdup(format);
}

void VideoReader::setVideoDecoder(const char *decoder) {
    mVideoCodecName = av_strdup(decoder);
}

void VideoReader::setReadListener(OnDecodeListener *listener, bool autoRelease) {
    mReadListener = listener;
    mAutoRelease = autoRelease;
}

void VideoReader::addFormatOptions(std::string key, std::string value) {
    mFormatOptions[key] = value;
}

void VideoReader::addDecodeOptions(std::string key, std::string value) {
    mDecodeOptions[key] = value;
}

int VideoReader::seekTo(float timeMs) {
    if (!mMediaDemuxer || !mMediaDemuxer->getContext()) {
        LOGE("Failed to find demuxer or demuxer context");
        return -1;
    }
    if (mMediaDemuxer->getDuration() <= 0) {
        return -1;
    }

    if (!mVideoDecoder || !mVideoDecoder->getStreamIndex() < 0) {
        LOGE("Failed to find audio decoder or audio stream");
        return -1;
    }

    int seekFlags = 0;
    seekFlags &= ~AVSEEK_FLAG_BYTE;
    int64_t start_time = 0;
    int64_t seek_pos = av_rescale(timeMs, AV_TIME_BASE, 1000);
    start_time = mMediaDemuxer->getContext() ? mMediaDemuxer->getContext()->start_time : 0;
    if (start_time > 0 && start_time != AV_NOPTS_VALUE) {
        seek_pos += start_time;
    }
    int ret = avformat_seek_file(mMediaDemuxer->getContext(), mVideoDecoder->getStreamIndex(),
                                 INT64_MIN, seek_pos, INT64_MAX, seekFlags);
    if (ret < 0) {
        LOGE("%s: could not seek to position %0.3f\n", mSrcPath, (double)seek_pos / AV_TIME_BASE);
        return ret;
    }
    return 0;
}

int64_t VideoReader::getDuration() {
    if (mMediaDemuxer) {
        return mMediaDemuxer->getDuration();
    }
    return 0;
}

int VideoReader::openInputFile() {
    int ret;
    mMediaDemuxer = std::make_shared<AVMediaDemuxer>();
    mMediaDemuxer->setInputPath(mSrcPath);
    mMediaDemuxer->setInputFormat(mFormat);
    ret = mMediaDemuxer->openDemuxer(mFormatOptions);
    if (ret < 0) {
        LOGE("Failed to open media demuxer");
        mMediaDemuxer.reset();
        mMediaDemuxer = nullptr;
        return ret;
    }

    // 打开视频解码器
    if (mMediaDemuxer->hasVideoStream()) {
        mVideoDecoder = std::make_shared<AVVideoDecoder>(mMediaDemuxer);
        mVideoDecoder->setDecoder(mVideoCodecName);
        ret = mVideoDecoder->openDecoder(mDecodeOptions);
        if (ret < 0) {
            LOGE("Failed to open video decoder");
            mVideoDecoder.reset();
            mVideoDecoder = nullptr;
        }
    }

    // 打印信息
    mMediaDemuxer->printInfo();

    // 判断是否找不到视频流
    if (!mVideoDecoder) {
        LOGE("Could not find audio or video stream in the input, aborting");
        return -1;
    }

    return 0;
}

int VideoReader::decodePacket() {
    if (!mMediaDemuxer || !mMediaDemuxer->getContext()) {
        LOGE("Failed to find demuxer context");
        return -1;
    }
    if (!mVideoDecoder) {
        LOGE("Failed to find video decoder");
        return -1;
    }
    // 读取一帧音频数据包
    int ret = -1;
    while (ret != 0) {
        ret = av_read_frame(mMediaDemuxer->getContext(), &mPacket);
        if (ret < 0) {
            LOGE("Failed to call av_read_frame: %s", av_err2str(ret));
            break;
        }
        if (mPacket.stream_index < 0 || mPacket.stream_index != mVideoDecoder->getStreamIndex()) {
            av_packet_unref(&mPacket);
        } else {
            ret = 0;
            break;
        }
    }
    if (ret == 0) {
        ret = decodePacket(&mPacket, mReadListener);
    }
    av_packet_unref(&mPacket);
    return ret;
}

int VideoReader::decodePacket(AVPacket *packet, OnDecodeListener *listener) {
    int ret = 0;

    if (!packet || packet->stream_index < 0) {
        return -1;
    }

    // 等待队列消耗足够的帧之后再做处理
    if (listener != nullptr) {
        while (!mAbortRequest && listener->isDecodeWaiting()) {
        }
    }

    // 如果处于终止状态，则直接退出解码过程
    if (mAbortRequest) {
        return 0;
    }
    if (packet->stream_index == mVideoDecoder->getStreamIndex()) {

        AVCodecContext *pCodecContext = mVideoDecoder->getContext();
        // 将数据包送去解码
        ret = avcodec_send_packet(pCodecContext, packet);
        if (ret < 0) {
            LOGE("Failed to call avcodec_send_packet: %s", av_err2str(ret));
            return ret;
        }

        while (ret == 0 && !mAbortRequest) {

            AVFrame *frame = av_frame_alloc();
            if (!frame) {
                LOGE("Failed to allocate audio AVFrame");
                ret = -1;
                break;
            }

            // 取出解码后的AVFrame
            ret = avcodec_receive_frame(pCodecContext, frame);
            if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) {
                av_frame_unref(frame);
                av_frame_free(&frame);
                break;
            } else if (ret < 0) {
                LOGE("Failed to call avcodec_receive_frame: %s", av_err2str(ret));
                av_frame_unref(frame);
                av_frame_free(&frame);
                break;
            }

            // 将解码后的帧送出去
            if (listener != nullptr) {
                listener->onDecodedFrame(frame, AVMEDIA_TYPE_AUDIO);
            } else {
                av_frame_unref(frame);
                av_frame_free(&frame);
            }
        }
    }
    return ret;
}