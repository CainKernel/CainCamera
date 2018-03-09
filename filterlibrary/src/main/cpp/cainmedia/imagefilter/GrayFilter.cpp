//
// Created by Administrator on 2018/3/6.
//

#include "GrayFilter.h"

GrayFilter::GrayFilter(int *pixels, int width, int height)
        : IImageFilter(pixels, width, height) {

}

void GrayFilter::processImage(int *destPixels) {
    for (int i = 0; i < width * height; i++) {
        int value = *(pixels + i);
        Color color(value);
        int gray = (int)(color.red() * 0.3 + color.green() * 0.59 + color.blue() * 0.11);
        *(destPixels + i) = rgb2Color(gray, gray, gray);
    }
}