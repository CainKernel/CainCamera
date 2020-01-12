//
// Created by CainHuang on 2020-01-10.
//

#include "AVVideoDecoder.h"

AVVideoDecoder::AVVideoDecoder(const std::shared_ptr<AVMediaDemuxer> &mediaDemuxer)
        : AVMediaDecoder(mediaDemuxer) {

}

AVVideoDecoder::~AVVideoDecoder() {

}

AVMediaType AVVideoDecoder::getMediaType() {
    return AVMEDIA_TYPE_VIDEO;
}

void AVVideoDecoder::initMetadata() {
    mWidth = pCodecCtx->width;
    mHeight = pCodecCtx->height;
    mPixelFormat = pCodecCtx->pix_fmt;
    auto demuxer = mWeakDemuxer.lock().get();
    if (demuxer) {
        mFrameRate = (int) av_q2d(av_guess_frame_rate(demuxer->getContext(), pStream, nullptr));
        pCodecCtx->time_base = av_inv_q(av_d2q(mFrameRate, 100000));
    } else {
        mFrameRate = 30;
        pCodecCtx->time_base = av_inv_q(av_d2q(mFrameRate, 100000));
    }
}

int AVVideoDecoder::getWidth() {
    return mWidth;
}

int AVVideoDecoder::getHeight() {
    return mHeight;
}

int AVVideoDecoder::getFrameRate() {
    return mFrameRate;
}

AVPixelFormat AVVideoDecoder::getFormat() {
    return mPixelFormat;
}