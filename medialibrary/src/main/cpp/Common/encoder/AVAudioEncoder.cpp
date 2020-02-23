//
// Created by CainHuang on 2020-01-09.
//

#include "AVAudioEncoder.h"

AVAudioEncoder::AVAudioEncoder(const std::shared_ptr<AVMediaMuxer> &mediaMuxer)
        : AVMediaEncoder(mediaMuxer) {
}

AVAudioEncoder::~AVAudioEncoder() {
    LOGD("AVMediaEncoder destructor: %s", av_get_media_type_string(getMediaType()));
}

void AVAudioEncoder::setAudioParams(int bitrate, int sampleRate, int channels) {
    pCodecCtx->sample_rate = sampleRate;
    pCodecCtx->channels = channels;
    pCodecCtx->bit_rate = bitrate;
    pCodecCtx->channel_layout = (uint64_t) av_get_default_channel_layout(channels);
    pCodecCtx->sample_fmt = pCodec->sample_fmts[0];
    pCodecCtx->time_base = AVRational{1, pCodecCtx->sample_rate};
}

AVMediaType AVAudioEncoder::getMediaType() {
    return AVMEDIA_TYPE_AUDIO;
}

AVCodecID AVAudioEncoder::getCodecId() {
    return AV_CODEC_ID_AAC;
}


