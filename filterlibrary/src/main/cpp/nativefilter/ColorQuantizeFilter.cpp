//
// Created by cain on 2018/9/8.
//

#include <cstdint>
#include "ColorQuantizeFilter.h"
#include "FilterUtils.h"

ColorQuantizeFilter::ColorQuantizeFilter() : levels(5) {

}

ColorQuantizeFilter::~ColorQuantizeFilter() {

}

int ColorQuantizeFilter::process(void *pixels, unsigned int width,
                                 unsigned int height) {
    if (width == 0 || height == 0) {
        return -1;
    }

    int32_t *currentPixels = (int32_t *) pixels;

    uint8_t alpha;
    uint8_t red;
    uint8_t green;
    uint8_t blue;
    for (int j = 0; j < height; j++) {
        for (int i = 0; i < width; i++) {
            int32_t color = currentPixels[j * width + i];
            alpha = (uint8_t)((color & 0xFF000000) >> 24);
            red = (uint8_t)(color & 0x000000FF);
            green = (uint8_t)((color & 0x0000FF00) >> 8);
            blue = (uint8_t)((color & 0x00FF0000) >> 16);

            float qRed = (((float) ((int) (red * 0.003921569 * levels))) / levels) * 255;
            float qGreen = (((float) ((int) (green * 0.003921569 * levels))) / levels) * 255;
            float qBlue = (((float) ((int) (blue * 0.003921569 * levels))) / levels) * 255;

            red   = (uint8_t)clamp(qRed);
            green = (uint8_t)clamp(qGreen);
            blue  = (uint8_t)clamp(qBlue);

            currentPixels[j * width + i] = ARGB_COLOR(alpha, red, green, blue);
        }
    }

    return 0;
}

void ColorQuantizeFilter::setLevels(float levels) {
    this->levels = levels;
}
