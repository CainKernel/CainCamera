//
// Created by Administrator on 2018/3/6.
//

#include "GrayFilter.h"

GrayFilter::GrayFilter(int *pixels, int width, int height)
        : IImageFilter(pixels, width, height) {

}

int *GrayFilter::processImage() {
    for (int i = 0; i < width * height; i++) {
        int value = *(pixels + i);
        Color color(value);
        int gray = (int)(color.red() * 0.3 + color.green() * 0.59 + color.blue() * 0.11);
        *(pixels + i) = rgb2Color(gray, gray, gray);
    }
    return pixels;
}