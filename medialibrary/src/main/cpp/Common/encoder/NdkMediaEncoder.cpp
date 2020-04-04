//
// Created by CainHuang on 2020/1/11.
//

#include "NdkMediaEncoder.h"

NdkMediaEncoder::NdkMediaEncoder(std::shared_ptr<AVMediaMuxer> mediaMuxer) {
    mWeakMuxer = mediaMuxer;
    pStream = nullptr;
    mMediaCodec = nullptr;
    mStreamIndex = -1;
}

NdkMediaEncoder::~NdkMediaEncoder() {
    release();
}

int NdkMediaEncoder::openEncoder() {
    return 0;
}

int NdkMediaEncoder::closeEncoder() {
    return 0;
}

void NdkMediaEncoder::release() {

}

int NdkMediaEncoder::encodeMediaData(AVMediaData *mediaData) {
    return encodeMediaData(mediaData, nullptr);
}

int NdkMediaEncoder::encodeFrame(AVFrame *frame) {
    return encodeFrame(frame, nullptr);
}

/**
 * 计算编码后的AVPacket.pts
 * @param presentationTimeUs us
 * @param time_base          time_base
 */
int64_t NdkMediaEncoder::rescalePts(int64_t presentationTimeUs, AVRational time_base) {
    return (int64_t)(presentationTimeUs / 1000000.0 / av_q2d(time_base));
}
