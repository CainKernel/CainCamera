//
// Created by CainHuang on 2019/8/18.
//

#include "AVMediaData.h"
#include "AVFormatter.h"

AVMediaData::AVMediaData() : image(nullptr), length(0), sample(nullptr), sample_size(0),
                             width(0), height(0), pixelFormat(PIXEL_FORMAT_NONE), pts(0),
                             type(MediaNone) {

}

AVMediaData::~AVMediaData() {
    free();
}

void AVMediaData::setAudio(uint8_t *data, int size) {
    sample = data;
    sample_size = size;
    type = MediaAudio;
}

void AVMediaData::setVideo(uint8_t *data, int length, int width, int height, int pixelFormat) {
    this->image = data;
    this->length = length;
    this->width = width;
    this->height = height;
    this->pixelFormat = pixelFormat;
    this->type = MediaVideo;
}

void AVMediaData::setPts(int64_t pts) {
    this->pts = pts;
}

int64_t AVMediaData::getPts() {
    return pts;
}

void AVMediaData::free() {
    if (image != nullptr) {
        delete image;
        image = nullptr;
    }
    length = 0;
    if (sample != nullptr) {
        delete sample;
        sample = nullptr;
    }
    sample_size = 0;
}

MediaType AVMediaData::getType() {
    return type;
}

const char *AVMediaData::getName() {
    return get_media_type_string(getType());
}
