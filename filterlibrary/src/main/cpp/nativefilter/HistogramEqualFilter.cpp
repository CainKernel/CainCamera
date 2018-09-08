//
// Created by cain on 2018/9/8.
//

#include <cstdint>
#include <vector>
#include "HistogramEqualFilter.h"
#include "FilterUtils.h"

HistogramEqualFilter::HistogramEqualFilter() : contrastIntensity(1) {

}

HistogramEqualFilter::~HistogramEqualFilter() {

}

int HistogramEqualFilter::process(void *pixels, unsigned int width,
                             unsigned int height) {
    if (width == 0 || height == 0) {
        return -1;
    }

    int32_t *currentPixels = (int32_t *) pixels;

    uint8_t alpha;
    uint8_t red;
    uint8_t green;
    uint8_t blue;
    uint8_t array[256];
    std::vector<int> numArray(width * height);
    int contrast = (int) (contrastIntensity * 255);
    int pos = 0;
    for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
            int32_t color = currentPixels[y * width + x];
            alpha = (uint8_t)((color & 0xFF000000) >> 24);
            red = (uint8_t)(color & 0x000000FF);
            green = (uint8_t)((color & 0x0000FF00) >> 8);
            blue = (uint8_t)((color & 0x00FF0000) >> 16);
            int index = (red * 0x1b36 + green * 0x5b8c + blue * 0x93e) >> 15;
            array[index]++;
            numArray[pos] = index;
            pos++;
        }
    }
    for (int i = 1; i < 0x100; i++){
        array[i] += array[i - 1];
    }
    for (int i = 0; i < 0x100; i++){
        array[i] = (array[i] << 8) / height * width;
        array[i] = ((contrast * array[i]) >> 8) + (((0xff - contrast) * i) >> 8);
    }
    pos = 0;
    for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
            int32_t color = currentPixels[y * width + x];
            alpha = (uint8_t)((color & 0xFF000000) >> 24);
            red = (uint8_t)(color & 0x000000FF);
            green = (uint8_t)((color & 0x0000FF00) >> 8);
            blue = (uint8_t)((color & 0x00FF0000) >> 16);
            if (numArray[pos] != 0){
                int num = array[numArray[pos]];
                red = (red * num) / numArray[pos];
                green = (green * num) / numArray[pos];
                blue = (blue * num) / numArray[pos];
                red = (uint8_t) clamp(red);
                green = (uint8_t) clamp(green);
                blue = (uint8_t) clamp(blue);
            }
            currentPixels[y * width + x] = ARGB_COLOR(alpha, red, green, blue);
            pos++;
        }
    }

    return 0;
}
