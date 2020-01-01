//
// Created by CainHuang on 2019/9/15.
//

#include <memory.h>
#include "AVMediaHeader.h"
#include "RecordConfig.h"

RecordConfig::RecordConfig() : dstFile(nullptr), width(0), height(0), frameRate(0), maxBitRate(0),
                               pixelFormat(0), sampleRate(0), sampleFormat(0), channels(0),
                               cropX(0), cropY(0), cropWidth(0), cropHeight(0), rotateDegree(0),
                               scaleWidth(0), scaleHeight(0), mirror(false),
                               enableAudio(false), enableVideo(false) {

}

RecordConfig::~RecordConfig() {
    if (dstFile != nullptr) {
        av_freep(&dstFile);
        dstFile = nullptr;
    }
}

void RecordConfig::setOutput(const char *url) {
    dstFile = strdup(url);
}

void RecordConfig::setVideoParams(int width, int height, int frameRate, int pixelFormat,
                                  int maxBitRate) {
    this->width = width;
    this->height = height;
    this->frameRate = frameRate;
    this->pixelFormat = pixelFormat;
    this->maxBitRate = maxBitRate;
    enableVideo = true;
}

void RecordConfig::setAudioParams(int sampleRate, int sampleFormat, int channels) {
    this->sampleRate = sampleRate;
    this->sampleFormat = sampleFormat;
    this->channels = channels;
    enableAudio = true;
}

void RecordConfig::setMaxBitRate(int64_t maxBitRate) {
    this->maxBitRate = maxBitRate;
}

void RecordConfig::setCrop(int cropX, int cropY, int cropW, int cropH) {
    this->cropX = cropX;
    this->cropY = cropY;
    this->cropWidth = cropW;
    this->cropHeight = cropH;
}

void RecordConfig::setRotate(int rotateDegree) {
    this->rotateDegree = rotateDegree;
}

void RecordConfig::setScale(int scaleW, int scaleH) {
    this->scaleWidth = scaleW;
    this->scaleHeight = scaleH;
}

void RecordConfig::setMirror(bool mirror) {
    this->mirror = mirror;
}