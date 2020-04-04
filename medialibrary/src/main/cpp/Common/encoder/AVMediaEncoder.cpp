//
// Created by CainHuang on 2020-01-09.
//

#include "AVMediaEncoder.h"

AVMediaEncoder::AVMediaEncoder(std::shared_ptr<AVMediaMuxer> mediaMuxer) {
    mWeakMuxer = mediaMuxer;
    pCodecName = nullptr;
    pCodecCtx = nullptr;
    pCodec = nullptr;
    pStream = nullptr;
}

AVMediaEncoder::~AVMediaEncoder() {

    if (pCodecName) {
        av_freep(&pCodecName);
        pCodecName = nullptr;
    }
}

/**
 * 设置编码器名称
 * @param name
 */
void AVMediaEncoder::setEncoder(const char *name) {
    pCodecName = av_strdup(name);
}

/**
 * 创建编码器
 * @param name
 * @return
 */
int AVMediaEncoder::createEncoder() {
    AVCodec *encoder = nullptr;
    if (!pCodecName) {
        encoder = avcodec_find_encoder_by_name(pCodecName);
    }
    if (encoder == nullptr) {
        if (pCodecName) {
            LOGE("Failed to find encoder by name: %s", pCodecName);
        }
        if (getCodecId() != AV_CODEC_ID_NONE) {
            encoder = avcodec_find_encoder(getCodecId());
        }
    }
    if (encoder == nullptr) {
        LOGE("Failed to find encoder: type - %s", av_get_media_type_string(getMediaType()));
        return AVERROR_INVALIDDATA;
    }

    pCodec = encoder;
    // 创建编码上下文
    pCodecCtx = avcodec_alloc_context3(encoder);
    if (!pCodecCtx) {
        LOGE("Failed to allocate the encoder context");
        return AVERROR(ENOMEM);
    }

    // 获取媒体复用器
    auto mediaMuxer = mWeakMuxer.lock();
    if (!mediaMuxer) {
        LOGE("Failed to find media muxer: type - %s", av_get_media_type_string(getMediaType()));
        return -1;
    }

    // 设置全局头部信息
    if (mediaMuxer->hasGlobalHeader()) {
        pCodecCtx->flags |= AV_CODEC_FLAG_GLOBAL_HEADER;
    }

    // 创建媒体流
    pStream = mediaMuxer->createStream(encoder);
    if (!pStream) {
        LOGE("Failed to allocate stream.");
        return -1;
    }
    return 0;
}

/**
 * 打开编码器
 * @param mEncodeOptions
 * @return
 */
int AVMediaEncoder::openEncoder(std::map<std::string, std::string> mEncodeOptions) {
    if (pCodecCtx == nullptr || pStream == nullptr) {
        return -1;
    }
    AVDictionary *options = nullptr;
    auto it = mEncodeOptions.begin();
    for (; it != mEncodeOptions.end(); it++) {
        av_dict_set(&options, (*it).first.c_str(), (*it).second.c_str(), 0);
    }

    // 设置时钟基准
    pStream->time_base = pCodecCtx->time_base;

    // 打开编码器
    int ret = avcodec_open2(pCodecCtx, pCodec, &options);
    if (ret < 0) {
        LOGE("Could not open %s codec: %s", av_get_media_type_string(getMediaType()),
             av_err2str(ret));
        av_dict_free(&options);
        return ret;
    }
    av_dict_free(&options);

    // 将编码器参数复制到媒体流中
    ret = avcodec_parameters_from_context(pStream->codecpar, pCodecCtx);
    if (ret < 0) {
        LOGE("Failed to copy encoder parameters to video stream");
        return ret;
    }

    return 0;
}

/**
 * 编码一帧数据
 * @param frame
 * @param gotFrame
 * @return
 */
int AVMediaEncoder::encodeFrame(AVFrame *frame, int *gotFrame) {
    int ret, gotFrameLocal;
    if (!gotFrame) {
        gotFrame = &gotFrameLocal;
    }
    *gotFrame = 0;

    // 初始化数据包
    AVPacket packet;
    packet.data = nullptr;
    packet.size = 0;
    av_init_packet(&packet);

    // 送去编码
    ret = avcodec_send_frame(pCodecCtx, frame);
    if (ret < 0) {
        if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) {
            return 0;
        }
        LOGE("Failed to call avcodec_send_frame: %s", av_err2str(ret));
        return ret;
    }

    while (ret >= 0) {
        // 取出解码后的数据包
        ret = avcodec_receive_packet(pCodecCtx, &packet);
        if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) {
            break;
        } else if (ret < 0) {
            LOGE("Failed to call avcodec_receive_packet: %s, type: %s", av_err2str(ret),
                    av_get_media_type_string(getMediaType()));
            av_packet_unref(&packet);
            return ret;
        }

        // 计算输出的pts
        av_packet_rescale_ts(&packet, pCodecCtx->time_base, pStream->time_base);
        packet.stream_index = pStream->index;

        // 写入文件
        auto mediaMuxer = mWeakMuxer.lock();
        if (mediaMuxer != nullptr) {
            ret = mediaMuxer->writeFrame(&packet);
            if (ret < 0) {
                LOGE("Failed to call av_interleaved_write_frame: %s, type: %s", av_err2str(ret), av_get_media_type_string(getMediaType()));
                av_packet_unref(&packet);
                return ret;
            }
            LOGD("write packet: type:%s, pts: %d, s: %f", av_get_media_type_string(getMediaType()),
                    packet.pts, (float) (packet.pts * av_q2d(pStream->time_base)));
            *gotFrame = 1;
        } else {
            LOGE("Failed to find media muxer");
            av_packet_unref(&packet);
            *gotFrame = 0;
            return ret;
        }
    }

    av_packet_unref(&packet);
    return 0;
}

/**
 * 关闭编码器
 */
void AVMediaEncoder::closeEncoder() {
    if (pCodecCtx != nullptr) {
        avcodec_close(pCodecCtx);
        avcodec_free_context(&pCodecCtx);
        pCodecCtx = nullptr;
        pCodec = nullptr;
    }
    if (pStream != nullptr && pStream->metadata) {
        av_dict_free(&pStream->metadata);
        pStream->metadata = nullptr;
    }
    pStream = nullptr;
}

/**
 * 获取媒体类型
 * @return
 */
AVCodecContext* AVMediaEncoder::getContext() {
    return pCodecCtx;
}