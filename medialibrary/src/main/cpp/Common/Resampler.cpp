//
// Created by CainHuang on 2020/1/11.
//

#include "Resampler.h"

Resampler::Resampler() {
    pSampleConvertCtx = nullptr;
    mSampleFrame = nullptr;

    mOutSampleSize = 0;
    mOutSampleRate = 0;
    mOutChannelLayout = 0;
    mOutSampleFormat = AV_SAMPLE_FMT_NONE;
    mOutFrameSize = 0;
    mOutChannels = 0;

    mInSampleRate = 0;
    mInChannels = 0;
    mInChannelLayout = 0;
    mInSampleFormat = AV_SAMPLE_FMT_NONE;
}

Resampler::~Resampler() {
    release();
}

void Resampler::release() {
    if (mSampleFrame != nullptr) {
        av_frame_free(&mSampleFrame);
        mSampleFrame = nullptr;
    }
    if (mSampleBuffer != nullptr) {
        for (int i = 0; i < mOutChannels; i++) {
            if (mSampleBuffer[i] != nullptr) {
                av_free(mSampleBuffer[i]);
                mSampleBuffer[i] = nullptr;
            }
        }
        delete[] mSampleBuffer;
        mSampleBuffer = nullptr;
    }
}

/**
 * 设置输入参数
 * @param sample_rate
 * @param channel_layout
 * @param sample_fmt
 */
void Resampler::setInput(int sample_rate, int channels, AVSampleFormat sample_fmt) {
    mInSampleRate = sample_rate;
    mInChannels = av_sample_fmt_is_planar(sample_fmt) ? channels : 1;
    mInSampleFormat = sample_fmt;
    mInChannelLayout = av_get_default_channel_layout(mInChannels);
}

/**
 * 设置输出音频参数
 * @param sample_rate
 * @param channel_layout
 * @param sample_fmt
 * @param channels
 * @param frame_size
 */
void Resampler::setOutput(int sample_rate, uint64_t channel_layout, AVSampleFormat sample_fmt,
                          int channels, int frame_size) {

    mOutSampleRate = sample_rate;
    mOutChannelLayout = channel_layout;
    mOutSampleFormat = sample_fmt;
    mOutFrameSize = frame_size;

    mSampleFrame = av_frame_alloc();
    mSampleFrame->format = sample_fmt;
    mSampleFrame->nb_samples = frame_size;
    mSampleFrame->channel_layout = (uint64_t)channel_layout;
    mSampleFrame->pts = 0;

    // 获取声道数
    mOutChannels = av_sample_fmt_is_planar(sample_fmt) ? channels : 1;
    // 计算出缓冲区大小
    mOutSampleSize = av_samples_get_buffer_size(nullptr, channels,
                                             frame_size,
                                             sample_fmt, 1) / mOutChannels;
    // 初始化采样缓冲区大小
    mSampleBuffer = new uint8_t *[mOutChannels];
    for (int i = 0; i < mOutChannels; i++) {
        mSampleBuffer[i] = (uint8_t *) av_malloc((size_t) mOutSampleSize);
        if (mSampleBuffer[i] == nullptr) {
            LOGE("Failed to allocate sample buffer");
        }
    }
}

/**
 * 初始化重采样器
 * @return
 */
int Resampler::init() {
    pSampleConvertCtx = swr_alloc_set_opts(pSampleConvertCtx,
                                           mOutChannelLayout,mOutSampleFormat, mOutSampleRate,
                                           mInChannelLayout, mInSampleFormat, mInSampleRate,
                                           0, nullptr);
    int ret = 0;
    if (!pSampleConvertCtx) {
        LOGE("Failed to allocate SwrContext");
        return -1;
    } else if ((ret = swr_init(pSampleConvertCtx)) < 0) {
        LOGE("Failed to call swr_init: %s", av_err2str(ret));
        return ret;
    }
    return 0;
}

/**
 * resample pcm data
 * @param data          pcm data
 * @param nb_samples    pcm data length
 * @return number of samples output per channel, negative value on error
 */
int Resampler::resample(const uint8_t *data, int nb_samples) {
    int ret = 0;
    // 如果输入输出不相等，则进行转码再做处理
    if (mInChannels != mOutChannels || mOutSampleFormat != mInSampleFormat || mOutSampleRate != mInSampleRate) {
        ret = swr_convert(pSampleConvertCtx, mSampleBuffer, mOutFrameSize,
                              &data, nb_samples);
        if (ret < 0) {
            LOGE("swr_convert error: %s", av_err2str(ret));
            return -1;
        }
        // 将数据复制到采样帧中
        avcodec_fill_audio_frame(mSampleFrame, mOutChannels, mInSampleFormat, mSampleBuffer[0],
                                 mOutSampleSize, 0);
        for (int i = 0; i < mOutChannels; i++) {
            mSampleFrame->data[i] = mSampleBuffer[i];
            mSampleFrame->linesize[i] = mOutSampleSize;
        }
    } else {
        // 直接将数据复制到采样帧中
        ret = av_samples_fill_arrays(mSampleFrame->data, mSampleFrame->linesize, data,
                                     mOutChannels, mSampleFrame->nb_samples,
                                     mOutSampleFormat, 1);
        if (ret < 0) {
            LOGE("Failed to call av_samples_fill_arrays: %s", av_err2str(ret));
            return -1;
        }
    }
    mSampleFrame->pts = mNbSamples;
    mNbSamples += mSampleFrame->nb_samples;
    return 0;
}

/**
 * 对音频帧进行重采样处理
 * @param frame
 * @return
 */
int Resampler::resample(AVFrame *frame) {
    if (!frame) {
        return -1;
    }
    int ret = 0;
    ret = swr_convert(pSampleConvertCtx, mSampleBuffer, mOutFrameSize,
                      (const uint8_t **)frame->extended_data, frame->nb_samples);
    if (ret < 0) {
        LOGE("swr_convert error: %s", av_err2str(ret));
        return -1;
    }
    // 将数据复制到采样帧中
    avcodec_fill_audio_frame(mSampleFrame, mOutChannels, mInSampleFormat, mSampleBuffer[0],
                             mOutSampleSize, 0);
    for (int i = 0; i < mOutChannels; i++) {
        mSampleFrame->data[i] = mSampleBuffer[i];
        mSampleFrame->linesize[i] = mOutSampleSize;
    }
    mSampleFrame->pts = mNbSamples;
    mNbSamples += mSampleFrame->nb_samples;
    return 0;
}
/**
 * get resampled frame
 * @return
 */
AVFrame* Resampler::getConvertedFrame() {
    return mSampleFrame;
}

int Resampler::getInputSampleRate() {
    return mInSampleRate;
}

int Resampler::getInputChannels() {
    return mInChannels;
}

AVSampleFormat Resampler::getInputSampleFormat() {
    return mInSampleFormat;
}