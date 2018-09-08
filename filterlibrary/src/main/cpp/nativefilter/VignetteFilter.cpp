//
// Created by cain on 2018/9/8.
//

#include <cstdint>
#include "VignetteFilter.h"
#include "FilterUtils.h"

VignetteFilter::VignetteFilter() : size(0.5f) {

}

VignetteFilter::~VignetteFilter() {

}

int
VignetteFilter::process(void *pixels, unsigned int width, unsigned int height) {
    if (width == 0 || height == 0) {
        return -1;
    }

    int32_t *currentPixels = (int32_t *) pixels;

    uint8_t alpha;
    uint8_t red;
    uint8_t green;
    uint8_t blue;

    // 长宽比
    int ratio = width >  height ?  height * 32768 / width : width * 32768 /  height;
    
    // 计算中心点
    int centerX = width >> 1;
    int centerY = height >> 1;
    // 计算最大最小值
    int max = centerX * centerX + centerY * centerY;
    int min = (int) (max * (1 - size));
    int diff = max - min;

    for (int j = 0; j < height; j++) {
        for (int i = 0; i < width; i++) {
            int32_t color = currentPixels[j * width + i];
            alpha = (uint8_t)((color & 0xFF000000) >> 24);
            red = (uint8_t)(color & 0x000000FF);
            green = (uint8_t)((color & 0x0000FF00) >> 8);
            blue = (uint8_t)((color & 0x00FF0000) >> 16);

            int dx = centerX - i;
            int dy = centerY - j;
            if (width > height) {
                dx = (dx * ratio) >> 15;
            } else {
                dy = (dy * ratio) >> 15;
            }
            int distanceSqrt = dx * dx + dy * dy;

            if (distanceSqrt > min) {
                // 计算暗角
                int vignette = ((max - distanceSqrt) << 8) / diff;
                vignette *= vignette;
                // 计算暗角的颜色
                int ri = (red * vignette) >> 16;
                int gi = (green * vignette) >> 16;
                int bi = (blue * vignette) >> 16;

                red = (uint8_t) clamp(ri);
                green = (uint8_t) clamp(gi);
                blue = (uint8_t) clamp(bi);
            }
            currentPixels[j * width + i] = ARGB_COLOR(alpha, red, green, blue);
        }
    }

    return 0;
}

void VignetteFilter::setVignetteSize(float size) {
    this->size = size;
}
