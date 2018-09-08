//
// Created by cain on 2018/9/8.
//

#include <cstdint>
#include "BlackWhiteFilter.h"
#include "FilterUtils.h"

BlackWhiteFilter::BlackWhiteFilter() {

}

BlackWhiteFilter::~BlackWhiteFilter() {

}

int BlackWhiteFilter::process(void *pixels, unsigned int width,
                              unsigned int height) {

    if (width == 0 || height == 0) {
        return -1;
    }

    int32_t *cureentPixels = (int32_t *) pixels;

    uint8_t alpha;
    uint8_t red;
    uint8_t green;
    uint8_t blue;
    int result;
    for (int j = 0; j < height; j++) {
        for (int i = 0; i < width; i++) {
            int32_t color = cureentPixels[j * width + i];
            alpha = (uint8_t)((color & 0xFF000000) >> 24);
            red = (uint8_t)(color & 0x000000FF);
            green = (uint8_t)((color & 0x0000FF00) >> 8);
            blue = (uint8_t)((color & 0x00FF0000) >> 16);

            result = (int)(red * 0.3f + green * 0.11f + blue * 0.59f);

            cureentPixels[j * width + i] = ARGB_COLOR(alpha, result, result, result);
        }
    }

    return 0;
}


