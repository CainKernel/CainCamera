//
// Created by CainHuang on 2020/1/11.
//

#include "NdkMediaEncoder.h"

NdkMediaEncoder::NdkMediaEncoder(std::shared_ptr<NdkMediaCodecMuxer> mediaMuxer) {
    mWeakMuxer = mediaMuxer;
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

int NdkMediaEncoder::encodeMediaData(AVMediaData *mediaData, int *gotFrame) {
    return -1;
}