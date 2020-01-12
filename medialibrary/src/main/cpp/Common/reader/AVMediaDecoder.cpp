//
// Created by CainHuang on 2020-01-10.
//

#include "AVMediaDecoder.h"

AVMediaDecoder::AVMediaDecoder(std::shared_ptr<AVMediaDemuxer> mediaDemuxer) {
    mWeakDemuxer = mediaDemuxer;
    pCodecName = nullptr;
    pCodecCtx = nullptr;
    pCodec = nullptr;
    pStream = nullptr;
    mStreamIndex = -1;
}

AVMediaDecoder::~AVMediaDecoder() {
    if (pCodecName) {
        av_freep(&pCodecName);
        pCodecName = nullptr;
    }
}

/**
 * 设置解码器
 * @param name
 */
void AVMediaDecoder::setDecoder(const char *name) {
    pCodecName = av_strdup(name);
}

/**
 * 打开解码器
 * @return
 */
int AVMediaDecoder::openDecoder(std::map<std::string, std::string> decodeOptions) {
    int ret;
    if (getMediaType() != AVMEDIA_TYPE_AUDIO && getMediaType() != AVMEDIA_TYPE_VIDEO) {
        LOGE("unsupport AVMediaType: %s", av_get_media_type_string(getMediaType()));
        return -1;
    }

    // 获取解复用器
    auto mediaDemuxer = mWeakDemuxer.lock();
    if (!mediaDemuxer) {
        LOGE("Failed to find media demuxer");
        return -1;
    }

    // 查找媒体流
    ret = av_find_best_stream(mediaDemuxer->getContext(), getMediaType(), -1, -1, nullptr, 0);
    if (ret < 0) {
        LOGE("Failed to call av_find_best_stream: %s", av_err2str(ret));
        return -1;
    }

    // 获取媒体流
    mStreamIndex = ret;
    pStream = mediaDemuxer->getContext()->streams[mStreamIndex];

    // 根据指定解码器名称查找解码器
    pCodec = avcodec_find_encoder_by_name(pCodecName);

    // 根据id查找解码器
    if (pCodec == nullptr) {
        pCodec = avcodec_find_decoder(pStream->codecpar->codec_id);
    }
    if (!pCodec) {
        LOGE("Failed to find %s codec", av_get_media_type_string(getMediaType()));
        return AVERROR(ENOMEM);
    }
    pStream->codecpar->codec_id = pCodec->id;

    // 创建解码上下文
    pCodecCtx = avcodec_alloc_context3(pCodec);
    if (!pCodecCtx) {
        LOGE("Failed to alloc the %s codec context", av_get_media_type_string(getMediaType()));
        return AVERROR(ENOMEM);
    }

    // 复制媒体流参数到解码上下文中
    if ((ret = avcodec_parameters_to_context(pCodecCtx, pStream->codecpar)) < 0) {
        LOGE("Failed to copy %s codec parameters to decoder context, result: %d",
             av_get_media_type_string(getMediaType()), ret);
        return ret;
    }

    // 打开解码器
    AVDictionary *options = nullptr;
    auto it = decodeOptions.begin();
    for (; it != decodeOptions.end(); it++) {
        av_dict_set(&options, (*it).first.c_str(), (*it).second.c_str(), 0);
    }
    if ((ret = avcodec_open2(pCodecCtx, pCodec, &options)) < 0) {
        LOGE("Failed to open %s codec, result: %d", av_get_media_type_string(getMediaType()), ret);
        av_dict_free(&options);
        return ret;
    }
    av_dict_free(&options);

    // 初始化媒体数据
    initMetadata();

    return 0;
}

/**
 * 解码数据包
 * @param packet
 * @param gotFrame
 * @return
 */
int AVMediaDecoder::decodePacket(AVPacket *packet, OnDecodeListener *listener, int *gotFrame) {
    int ret = 0;

    if (!packet || packet->stream_index < 0) {
        return -1;
    }

    if (packet->stream_index == mStreamIndex) {
        // 将数据包送去解码
        ret = avcodec_send_packet(pCodecCtx, packet);
        if (ret < 0) {
            LOGE("Failed to call avcodec_send_packet: %s", av_err2str(ret));
            return ret;
        }

        while (ret == 0) {

            AVFrame *frame = av_frame_alloc();
            if (!frame) {
                LOGE("Failed to allocate audio AVFrame");
                ret = -1;
                break;
            }

            // 取出解码后的AVFrame
            ret = avcodec_receive_frame(pCodecCtx, frame);
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
                listener->onDecodedFrame(frame, getMediaType());
            } else {
                av_frame_unref(frame);
                av_frame_free(&frame);
            }
        }
    }
    return ret;
}

/**
 * 关闭解码器
 */
void AVMediaDecoder::closeDecoder() {
    pCodec = nullptr;
    pStream = nullptr;
    if (pCodecCtx) {
        avcodec_close(pCodecCtx);
        avcodec_free_context(&pCodecCtx);
        pCodecCtx = nullptr;
    }
    mStreamIndex = -1;
}

/**
 * 获取媒体流索引
 * @return
 */
int AVMediaDecoder::getStreamIndex() {
    return mStreamIndex;
}

/**
 * 获取解码上下文
 * @return
 */
AVCodecContext* AVMediaDecoder::getContext() {
    return pCodecCtx;
}