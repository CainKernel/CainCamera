//
// Created by CainHuang on 2019/8/12.
//

#include "AVMediaReader.h"

AVMediaReader::AVMediaReader() {
    av_register_all();
    reset();
}

AVMediaReader::~AVMediaReader() {
    release();
}

/**
 * 设置数据源
 * @param url
 */
void AVMediaReader::setDataSource(const char *url) {
    mSrcPath = av_strdup(url);
}

/**
 * 设置读取起始位置，默认0
 * @param timeMs
 */
void AVMediaReader::setStart(float timeMs) {
    this->mStart = timeMs;
}

/**
 * 设置结束位置，默认为duration
 * @param timeMs
 */
void AVMediaReader::setEnd(float timeMs) {
    this->mEnd = timeMs;
}

/**
 * 设置输入格式
 * @param format
 */
void AVMediaReader::setInputFormat(const char *format) {
    iformat = av_find_input_format(format);
    if (!iformat) {
        LOGE("Unknown input format: %s", format);
    }
}

/**
 * 设置音频解码器名称
 * @param decoder
 */
void AVMediaReader::setAudioDecoder(const char *decoder) {
    mAudioDecoder = av_strdup(decoder);
}

/**
 * 设置视频解码器名称
 * @param decoder
 */
void AVMediaReader::setVideoDecoder(const char *decoder) {
    mVideoDecoder = av_strdup(decoder);
}

/**
 * 添加格式参数
 * @param key
 * @param value
 */
void AVMediaReader::addFormatOptions(std::string key, std::string value) {
    mFormatOptions[key] = value;
}

/**
 * 添加解码参数
 * @param key
 * @param value
 */
void AVMediaReader::addDecodeOptions(std::string key, std::string value) {
    mDecodeOptions[key] = value;
}

/**
 * 设置读取监听器
 * @param listener
 * @param autoRelease 是否自动释放
 */
void AVMediaReader::setReadListener(OnReadListener *listener, bool autoRelease) {
    mReadListener = listener;
    mAutoRelease = autoRelease;
}

/**
 * seekTo定位
 * @param timeMs
 */
void AVMediaReader::seekTo(float timeMs) {
    if (mDuration < 0) {
        return;
    }

    mMutex.lock();
    while (!seekRequest) {
        mCondition.wait(mMutex);
    }
    mMutex.unlock();

    if (!seekRequest) {
        int64_t start_time = 0;
        int64_t seek_pos = av_rescale(timeMs, AV_TIME_BASE, 1000);
        start_time = pFormatCtx ? pFormatCtx->start_time : 0;
        if (start_time > 0 && start_time != AV_NOPTS_VALUE) {
            seek_pos += start_time;
        }
        seekPos = seek_pos;
        seekRel = 0;
        seekFlags &= ~AVSEEK_FLAG_BYTE;
        seekRequest = 1;
        mCondition.signal();
    }
}

/**
 * 开始读取
 */
void AVMediaReader::start() {
    if (!mSrcPath) {
        return;
    }

    mMutex.lock();
    abortRequest = false;
    pauseRequest = false;
    mCondition.signal();
    mMutex.unlock();

    if (!mThread) {
        mThread = new Thread(this);
        mThread->start();
    }
}

/**
 * 暂停读取
 */
void AVMediaReader::pause() {
    mMutex.lock();
    pauseRequest = true;
    mCondition.signal();
    mMutex.unlock();
}

/**
 * 继续读取
 */
void AVMediaReader::resume() {
    mMutex.lock();
    pauseRequest = false;
    mCondition.signal();
    mMutex.unlock();
}

/**
 * 停止读取
 */
void AVMediaReader::stop() {
    mMutex.lock();
    abortRequest = true;
    mCondition.signal();
    mMutex.unlock();

    if (mThread != nullptr) {
        mThread->join();
        delete mThread;
        mThread = nullptr;
    }
}

/**
 * 重置所有参数
 */
void AVMediaReader::reset() {
    mThread = nullptr;
    mSrcPath = nullptr;
    mReadListener = nullptr;
    mVideoDecoder = nullptr;
    mAudioDecoder = nullptr;
    iformat = nullptr;
    mSrcPath = nullptr;
    pFormatCtx = nullptr;
    pAudioCodecCtx = nullptr;
    pVideoCodecCtx = nullptr;
    mStart = -1;
    mEnd = -1;
}

/**
 * 释放所有资源
 */
void AVMediaReader::release() {
    if (pVideoCodecCtx != nullptr) {
        avcodec_free_context(&pVideoCodecCtx);
        pVideoCodecCtx = nullptr;
    }
    if (pAudioCodecCtx != nullptr) {
        avcodec_free_context(&pAudioCodecCtx);
        pAudioCodecCtx = nullptr;
    }
    if (pFormatCtx != nullptr) {
        avformat_close_input(&pFormatCtx);
        pFormatCtx = nullptr;
    }
    if (mAutoRelease) {
        if (mReadListener != nullptr) {
            delete mReadListener;
        }
    }
    mReadListener = nullptr;
}

/**
 * 读取线程实体
 */
void AVMediaReader::run() {
    int ret = 0;
    // 打开文件
    ret = openInputFile();
    if (ret < 0) {
        return;
    }
    // 读取数据包
    ret = readPackets();
}


/**
 * 打开文件
 * @return
 */
int AVMediaReader::openInputFile() {
    int ret;
    AVDictionary *options = nullptr;
    auto it = mFormatOptions.begin();
    for (; it != mFormatOptions.end(); it++) {
        av_dict_set(&options, (*it).first.c_str(), (*it).second.c_str(), 0);
    }
    if ((ret = avformat_open_input(&pFormatCtx, mSrcPath, iformat, &options)) < 0) {
        LOGE("Failed to call avformat_open_input: %s", mSrcPath);
        av_dict_free(&options);
        return ret;
    }
    av_dict_free(&options);


    if ((ret = avformat_find_stream_info(pFormatCtx, nullptr)) < 0) {
        LOGE("Failed to call avformat_find_stream_info - %s", av_err2str(ret));
        return ret;
    }

    // 重置媒体流索引
    mVideoIndex = -1;
    mAudioIndex = -1;

    // 打开视频解码器
    ret = openDecoder(pFormatCtx, &pVideoCodecCtx, AVMEDIA_TYPE_VIDEO);
    if (ret >= 0) {
        mVideoIndex = ret;
        AVStream *videoStream = pFormatCtx->streams[mVideoIndex];
        mWidth = pVideoCodecCtx->width;
        mHeight = pVideoCodecCtx->height;
        mPixelFormat = pVideoCodecCtx->pix_fmt;
        mFrameRate = (int) av_q2d(av_guess_frame_rate(pFormatCtx, videoStream, nullptr));
        pVideoCodecCtx->time_base = av_inv_q(av_d2q(mFrameRate, 100000));
    }

    // 打开音频解码器
    ret = openDecoder(pFormatCtx, &pAudioCodecCtx, AVMEDIA_TYPE_AUDIO);
    if (ret >= 0) {
        mAudioIndex = ret;
        mSampleRate = pAudioCodecCtx->sample_rate;
    }

    av_dump_format(pFormatCtx, 0, mSrcPath, 0);

    // 判断是否音频流和视频流都找不到
    if (mVideoIndex < 0 && mAudioIndex < 0) {
        LOGE("Could not find audio or video stream in the input, aborting");
        return -1;
    }

    return 0;
}

/**
 * 打开解码器
 * @param formatCtx
 * @param codecCtx
 * @param type
 * @return
 */
int AVMediaReader::openDecoder(AVFormatContext *formatCtx, AVCodecContext **codecCtx,
                               AVMediaType type) {
    int ret, index;
    AVStream *stream;
    AVCodec *codec = nullptr;

    if (type != AVMEDIA_TYPE_AUDIO && type != AVMEDIA_TYPE_VIDEO) {
        LOGE("unsupport AVMediaType: %s", av_get_media_type_string(type));
        return -1;
    }

    // 查找媒体流信息
    ret = av_find_best_stream(formatCtx, type, -1, -1, nullptr, 0);
    if (ret < 0) {
        LOGE("Failed to call av_find_best_stream: %s", av_err2str(ret));
        return ret;
    }

    index = ret;
    stream = formatCtx->streams[index];

    // 根据指定解码器名称查找解码器
    if (type == AVMEDIA_TYPE_AUDIO && mAudioDecoder) {
        codec = avcodec_find_encoder_by_name(mAudioDecoder);
    } else if (type == AVMEDIA_TYPE_VIDEO && mVideoDecoder) {
        codec = avcodec_find_encoder_by_name(mVideoDecoder);
    }

    // 根据id查找解码器
    if (codec == nullptr) {
        codec = avcodec_find_decoder(stream->codecpar->codec_id);
    }
    if (!codec) {
        LOGE("Failed to find %s codec", av_get_media_type_string(type));
        return AVERROR(ENOMEM);
    }
    stream->codecpar->codec_id = codec->id;

    // 创建解码上下文
    *codecCtx = avcodec_alloc_context3(codec);
    if (!*codecCtx) {
        LOGE("Failed to alloc the %s codec context", av_get_media_type_string(type));
        return AVERROR(ENOMEM);
    }

    // 复制媒体流参数到解码上下文中
    if ((ret = avcodec_parameters_to_context(*codecCtx, stream->codecpar)) < 0) {
        LOGE("Failed to copy %s codec parameters to decoder context, result: %d",
             av_get_media_type_string(type), ret);
        return ret;
    }

    // 打开解码器
    AVDictionary *options = nullptr;
    auto it = mDecodeOptions.begin();
    for (; it != mDecodeOptions.end(); it++) {
        av_dict_set(&options, (*it).first.c_str(), (*it).second.c_str(), 0);
    }
    if ((ret = avcodec_open2(*codecCtx, codec, &options)) < 0) {
        LOGE("Failed to open %s codec, result: %d", av_get_media_type_string(type), ret);
        av_dict_free(&options);
        return ret;
    }
    av_dict_free(&options);

    // 返回媒体流索引
    return index;
}

/**
 * 解码数据包
 * @param packet
 * @param listener
 */
void AVMediaReader::decodePacket(AVPacket *packet, OnReadListener *listener) {
    int ret = 0;

    if (!packet || packet->stream_index < 0) {
        av_packet_unref(packet);
        av_packet_free(&packet);
        return;
    }

    // 创建帧对象
    AVFrame *frame = av_frame_alloc();
    if (!frame) {
        av_packet_unref(packet);
        av_packet_free(&packet);
        return;
    }

    // 视频帧解码
    if (packet->stream_index == mVideoIndex) {

        // 将数据包送去解码
        ret = avcodec_send_packet(pVideoCodecCtx, packet);
        if (ret < 0) {
            LOGE("Failed to call avcodec_send_packet: %s", av_err2str(ret));
            av_frame_free(&frame);
            av_packet_unref(packet);
            av_packet_free(&packet);
            return;
        }

        while (ret >= 0) {
            // 取出解码后的AVFrame
            ret = avcodec_receive_frame(pVideoCodecCtx, frame);
            if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) {
                break;
            } else if (ret < 0) {
                LOGE("Failed to call avcodec_receive_frame: %s", av_err2str(ret));
                break;
            }

            // 将解码后的帧送出去
            if (listener != nullptr) {
                listener->onReadFrame(frame, AVMEDIA_TYPE_VIDEO);
            } else {
                av_frame_unref(frame);
            }
        }
        av_frame_free(&frame);
        av_packet_unref(packet);
        av_packet_free(&packet);
    } else if (packet->stream_index == mAudioIndex) {

        // 送去解码
        ret = avcodec_send_packet(pAudioCodecCtx, packet);
        if (ret < 0) {
            LOGE("Failed to call avcodec_send_packet: %s", av_err2str(ret));
            av_frame_free(&frame);
            av_packet_unref(packet);
            av_packet_free(&packet);
            return;
        }

        while (ret >= 0) {

            // 取出解码后的AVFrame
            ret = avcodec_receive_frame(pAudioCodecCtx, frame);
            if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) {
                break;
            } else if (ret < 0) {
                LOGE("Failed to call avcodec_receive_frame: %s", av_err2str(ret));
                break;
            }

            // 将读取到的数据通过接口送取出，没有接口则直接释放掉
            if (listener != nullptr) {
                listener->onReadFrame(frame, AVMEDIA_TYPE_AUDIO);
            } else {
                av_frame_unref(frame);
            }
        }

        av_frame_free(&frame);
        av_packet_unref(packet);
        av_packet_free(&packet);

    } else { // 其他类型数据包直接释放
        av_frame_free(&frame);
        av_packet_unref(packet);
        av_packet_free(&packet);
    }
}

/**
 * 读取数据包
 * @return
 */
int AVMediaReader::readPackets() {

    int ret = 0;



    return 0;
}