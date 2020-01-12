//
// Created by CainHuang on 2020/1/4.
//

#include <memory.h>
#include "AVMediaHeader.h"
#include "TranscodeParams.h"

TranscodeParams::TranscodeParams()
        : dstFile(nullptr), width(0), height(0), frameRate(0), maxBitRate(0), quality(0),
          pixelFormat(0), videoEncoder(nullptr), useHardCodec(true),
          sampleRate(0), sampleFormat(0), channels(0), audioEncoder(nullptr),
          cropX(0), cropY(0), cropWidth(0), cropHeight(0), rotateDegree(0),
          scaleWidth(0), scaleHeight(0), mirror(false), videoFilter(nullptr), audioFilter(nullptr),
          enableAudio(false), enableVideo(false) {
}

TranscodeParams::~TranscodeParams() {
    if (videoEncoder != nullptr) {
        av_freep(&videoEncoder);
        videoEncoder = nullptr;
    }
    if (audioEncoder != nullptr) {
        av_freep(&audioEncoder);
        audioEncoder = nullptr;
    }
    if (audioFilter != nullptr) {
        av_freep(&audioFilter);
        audioFilter = nullptr;
    }
    if (videoFilter != nullptr) {
        av_freep(&videoFilter);
        videoFilter = nullptr;
    }
    if (dstFile != nullptr) {
        av_freep(&dstFile);
        dstFile = nullptr;
    }
}

void TranscodeParams::setOutput(const char *url) {
    dstFile = strdup(url);
}

void TranscodeParams::setVideoParams(int width, int height, int frameRate, int pixelFormat, int maxBitRate, int quality) {
    this->width = width;
    this->height = height;
    this->frameRate = frameRate;
    this->pixelFormat = pixelFormat;
    this->maxBitRate = maxBitRate;
    this->quality = quality;
    enableVideo = true;
}

void TranscodeParams::setAudioParams(int sampleRate, int sampleFormat, int channels) {
    this->sampleRate = sampleRate;
    this->sampleFormat = sampleFormat;
    this->channels = channels;
    enableAudio = true;
}

void TranscodeParams::setVideoEncoder(const char *encoder) {
    this->videoEncoder = av_strdup(encoder);
}

void TranscodeParams::setAudioEncoder(const char *encoder) {
    this->audioEncoder = av_strdup(encoder);
}

void TranscodeParams::setMaxBitRate(int64_t maxBitRate) {
    this->maxBitRate = maxBitRate;
}

void TranscodeParams::setQuality(int quality) {
    this->quality = quality;
}

void TranscodeParams::setCrop(int cropX, int cropY, int cropW, int cropH) {
    this->cropX = cropX;
    this->cropY = cropY;
    this->cropWidth = cropW;
    this->cropHeight = cropH;
}

void TranscodeParams::setRotate(int rotateDegree) {
    this->rotateDegree = rotateDegree;
}

void TranscodeParams::setScale(int scaleW, int scaleH) {
    this->scaleWidth = scaleW;
    this->scaleHeight = scaleH;
}

void TranscodeParams::setMirror(bool mirror) {
    this->mirror = mirror;
}

void TranscodeParams::setVideoFilter(const char *videoFilter) {
    this->videoFilter = av_strdup(videoFilter);
}

void TranscodeParams::setAudioFilter(const char *audioFilter) {
    this->audioFilter = av_strdup(audioFilter);
}

void TranscodeParams::setUseHardCodec(bool hardCodec) {
    this->useHardCodec = hardCodec;
}
