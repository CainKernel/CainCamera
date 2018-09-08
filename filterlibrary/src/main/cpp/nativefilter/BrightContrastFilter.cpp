//
// Created by cain on 2018/9/8.
//

#include <cstdint>
#include "BrightContrastFilter.h"
#include "FilterUtils.h"

BrightContrastFilter::BrightContrastFilter() : brightness(0.25f), contrast(0) {

}

BrightContrastFilter::~BrightContrastFilter() {

}

int BrightContrastFilter::process(void *pixels, unsigned int width, unsigned int height) {

    if (width == 0 || height == 0) {
        return -1;
    }

    int32_t *currentPixels = (int32_t *) pixels;

    uint8_t alpha;
    uint8_t red;
    uint8_t green;
    uint8_t blue;

    int bfi = (int)(brightness * 255);
    float cf = 1 + contrast;
    cf *= cf;
    int cfi = (int)(cf * 32768) + 1;

    for (int j = 0; j < height; j++) {
        for (int i = 0; i < width; i++) {
            int32_t color = currentPixels[j * width + i];
            alpha = (uint8_t)((color & 0xFF000000) >> 24);
            red = (uint8_t)(color & 0x000000FF);
            green = (uint8_t)((color & 0x0000FF00) >> 8);
            blue = (uint8_t)((color & 0x00FF0000) >> 16);

            // 修改亮度，加法处理
            if (bfi != 0) {
                // 增加亮度
                int ri = red + bfi;
                int gi = green + bfi;
                int bi = blue + bfi;
                // 限定范围
                red   = (uint8_t) clamp(ri);
                green = (uint8_t) clamp(gi);
                blue  = (uint8_t) clamp(bi);
            }

            // 修改对比度，做乘法处理
            if (cfi != 32769) {
                // 转成 -128 ~ 127
                int ri = red - 128;
                int gi = green - 128;
                int bi = blue - 128;

                // 乘法处理
                ri = (ri * cfi) >> 15;
                gi = (gi * cfi) >> 15;
                bi = (bi * cfi) >> 15;

                // 转成 0 ~ 255
                ri = ri + 128;
                gi = gi + 128;
                bi = bi + 128;

                // 限定范围
                red   = (uint8_t) clamp(ri);
                green = (uint8_t) clamp(gi);
                blue  = (uint8_t) clamp(bi);
            }

            currentPixels[j * width + i] = ARGB_COLOR(alpha, red, green, blue);
        }
    }

    return 0;

}

void BrightContrastFilter::setBrightness(float brightness) {
    this->brightness = brightness;
}

void BrightContrastFilter::setContrast(float contrast) {
    this->contrast = contrast;
}