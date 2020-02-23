//
// Created by CainHuang on 2020-01-09.
//

#include "AVVideoEncoder.h"

AVVideoEncoder::AVVideoEncoder(const std::shared_ptr<AVMediaMuxer> &mediaMuxer)
        : AVMediaEncoder(mediaMuxer) {

}

AVVideoEncoder::~AVVideoEncoder() {
    LOGD("AVMediaEncoder destructor: %s", av_get_media_type_string(getMediaType()));
}

void AVVideoEncoder::setVideoParams(int width, int height, AVPixelFormat pixelFormat, int frameRate,
        int maxBitRate, bool useTimeStamep, std::map<std::string, std::string> metadata) {
    pCodecCtx->width = width;
    pCodecCtx->height = height;
    pCodecCtx->pix_fmt = pixelFormat;
    pCodecCtx->gop_size = frameRate;

    // 设置是否使用时间戳作为pts，两种的time_base不一样
    if (useTimeStamep) {
        pCodecCtx->time_base = (AVRational) {1, 1000};
    } else {
        pCodecCtx->time_base = (AVRational) {1, frameRate};
    }

    // 设置最大比特率
    if (maxBitRate > 0) {
        pCodecCtx->rc_max_rate = maxBitRate;
        pCodecCtx->rc_buffer_size = maxBitRate;
    }

    // 设置媒体流meta参数
    auto it = metadata.begin();
    for (; it != metadata.end(); it++) {
        av_dict_set(&pStream->metadata, (*it).first.c_str(), (*it).second.c_str(), 0);
    }
}

AVMediaType AVVideoEncoder::getMediaType() {
    return AVMEDIA_TYPE_VIDEO;
}

AVCodecID AVVideoEncoder::getCodecId() {
    return AV_CODEC_ID_H264;
}



