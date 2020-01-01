//
// Created by CainHuang on 2019/9/15.
//

#if defined(__ANDROID__)

#include "MediaCodecReader.h"

MediaCodecReader::MediaCodecReader() {

}

MediaCodecReader::~MediaCodecReader() {

}

void MediaCodecReader::setDataSource(const char *url) {

}

void MediaCodecReader::setStart(float timeMs) {

}

void MediaCodecReader::setEnd(float timeMs) {

}

void MediaCodecReader::setReadListener(OnReadListener *listener, bool autoRelease) {

}

void MediaCodecReader::seekTo(float timeMs) {

}

void MediaCodecReader::start() {

}

void MediaCodecReader::pause() {

}

void MediaCodecReader::resume() {

}

void MediaCodecReader::stop() {

}

void MediaCodecReader::run() {

}

int MediaCodecReader::decodePacket(AMediaCodec *codec, AVMediaType type, OnReadListener *listener) {
    ssize_t bufidx = -1;
    bufidx = AMediaCodec_dequeueInputBuffer(codec, 2000);

    if (bufidx >= 0) {
        size_t bufSize;
        auto buf = AMediaCodec_getInputBuffer(codec, bufidx, &bufSize);
        auto sampleSize = AMediaExtractor_readSampleData(mMediaExtractor, buf, bufSize);

        if (sampleSize < 0) {
            sampleSize = 0;
            LOGD("EOS");
        }
        auto presentationTimeUs = AMediaExtractor_getSampleTime(mMediaExtractor);
        AMediaCodec_queueInputBuffer(codec, bufidx, 0, sampleSize, presentationTimeUs, 0);
        AMediaExtractor_advance(mMediaExtractor);
    }

    AMediaCodecBufferInfo info;
    auto status = AMediaCodec_dequeueOutputBuffer(codec, &info, 0);
    if (status >= 0) {
        if (info.flags & AMEDIACODEC_BUFFER_FLAG_END_OF_STREAM) {
            LOGD("output EOS");

        }
        // TODO 计算时间戳
        int64_t presentationNano = info.presentationTimeUs * 1000;

        // 释放输出缓冲区
        AMediaCodec_releaseOutputBuffer(codec, status, info.size != 0);
    } else if (status == AMEDIACODEC_INFO_OUTPUT_BUFFERS_CHANGED) {
        LOGD("output buffers changed");
    } else if (status == AMEDIACODEC_INFO_OUTPUT_FORMAT_CHANGED) {
        auto format = AMediaCodec_getOutputFormat(codec);
        LOGD("format changed to: %s", AMediaFormat_toString(format));
        AMediaFormat_delete(format);
    } else if (status == AMEDIACODEC_INFO_TRY_AGAIN_LATER) {
        LOGD("no output buffer right now");
    } else {
        LOGD("unexpected info codec: %zd", status);
    }

    return 0;
}

#endif /* defined(__ANDROID__) */