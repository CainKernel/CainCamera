//
// Created by cain on 2018/12/28.
//

#include "VideoDevice.h"

VideoDevice::VideoDevice() {

}

VideoDevice::~VideoDevice() {

}

void VideoDevice::terminate() {

}

void VideoDevice::onInitTexture(int width, int height, TextureFormat format, BlendMode blendMode,
                                int rotate) {

}

int VideoDevice::onUpdateYUV(uint8_t *yData, int yPitch, uint8_t *uData, int uPitch,
                             uint8_t *vData, int vPitch) {
    return 0;
}

int VideoDevice::onUpdateARGB(uint8_t *rgba, int pitch) {
    return 0;
}

int VideoDevice::onRequestRender(FlipDirection direction) {
    return 0;
}
