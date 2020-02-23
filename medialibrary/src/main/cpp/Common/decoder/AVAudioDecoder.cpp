//
// Created by CainHuang on 2020-01-10.
//

#include "AVAudioDecoder.h"

AVAudioDecoder::AVAudioDecoder(const std::shared_ptr<AVMediaDemuxer> &mediaDemuxer)
        : AVMediaDecoder(mediaDemuxer) {
    mSampleRate = 0;
    mSampleFormat = AV_SAMPLE_FMT_NONE;
    mChannels = 0;
}

AVAudioDecoder::~AVAudioDecoder() {

}

AVMediaType AVAudioDecoder::getMediaType() {
    return AVMEDIA_TYPE_AUDIO;
}

/**
 * 初始化媒体信息
 */
void AVAudioDecoder::initMetadata() {
    mSampleRate = pCodecCtx->sample_rate;
    mSampleFormat = pCodecCtx->sample_fmt;
    mChannels = pCodecCtx->channels;
}

/**
 * 获取采样率
 * @return
 */
int AVAudioDecoder::getSampleRate() {
    return mSampleRate;
}

/**
 * 获取声道格式
 * @return
 */
AVSampleFormat AVAudioDecoder::getSampleFormat() {
    return mSampleFormat;
}

/**
 * 获取声道数量
 * @return
 */
int AVAudioDecoder::getChannels() {
    return mChannels;
}