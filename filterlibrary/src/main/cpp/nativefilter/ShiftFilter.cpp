//
// Created by cain on 2018/9/8.
//

#include <cstdint>
#include <stdlib.h>
#include "ShiftFilter.h"
#include "FilterUtils.h"

ShiftFilter::ShiftFilter() : amount(2) {

}

ShiftFilter::~ShiftFilter() {

}

int
ShiftFilter::process(void *pixels, unsigned int width, unsigned int height) {
    if (width == 0 || height == 0) {
        return -1;
    }

    int32_t *currentPixels = (int32_t *) pixels;

    uint8_t alpha;
    uint8_t red;
    uint8_t green;
    uint8_t blue;
    int current;
    for (int j = 0; j < height; j++) {
        for (int i = 0; i < width; i++) {
            // 计算随机偏移位置
            if (i == 0) {
                current = (rand() % amount) * ((rand() % 2) ? 1 : -1);
            }
            int x = clampValue(i + current, 0, width - 1);
            int32_t color = currentPixels[j * width + x];
            alpha = (uint8_t)((color & 0xFF000000) >> 24);
            red = (uint8_t)(color & 0x000000FF);
            green = (uint8_t)((color & 0x0000FF00) >> 8);
            blue = (uint8_t)((color & 0x00FF0000) >> 16);

            red = (uint8_t)(255 - red);
            green = (uint8_t)(255 - green);
            blue = (uint8_t)(255 - blue);

            currentPixels[j * width + i] = ARGB_COLOR(alpha, red, green, blue);
        }
    }

    return 0;
}

void ShiftFilter::setAmount(int amount) {
    this->amount = ((amount >= 2) ? amount : 2);
}


