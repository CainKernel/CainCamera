//
// Created by Administrator on 2018/3/9.
//

#include "ContrastFilter.h"

ContrastFilter::ContrastFilter(int *pixels, int width, int height)
        : IImageFilter(pixels, width, height) {

}

void ContrastFilter::processImage(int *destPixels) {
    for (int i = 0; i < width * height; i++) {
        Color color(*(pixels + i));
        int r = 128 + (color.red() - 128) * contrast;
        int g = 128 + (color.green() - 128) * contrast;
        int b = 128 + (color.blue() - 128) * contrast;

        r = min(255, max(0, r));
        g = min(255, max(0, g));
        b = min(255, max(0, b));

        *(destPixels + i) = rgb2Color(r, g, b);
    }
}