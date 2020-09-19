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
 * 将数据包写入文件中
 * @param packet
 * @return
 */
int NdkMediaEncoder::writePacket(AVPacket *packet) {
    int ret = -1;
    if (packet == nullptr || packet->data == nullptr || packet->stream_index < 0) {
        return ret;
    }
    // 写入文件
    auto mediaMuxer = mWeakMuxer.lock();
    if (mediaMuxer != nullptr) {
        ret = mediaMuxer->writeFrame(packet);
        if (ret < 0) {
            LOGE("Failed to call av_interleaved_write_frame: %s, type: %s", av_err2str(ret), av_get_media_type_string(getMediaType()));
            return ret;
        }
        LOGD("write packet: type:%s, pts: %ld, s: %f", av_get_media_type_string(getMediaType()),
             packet->pts, (float) (packet->pts * av_q2d(pStream->time_base)));
        return 0;
    } else {
        LOGE("Failed to find media muxer");
        return ret;
    }
}
