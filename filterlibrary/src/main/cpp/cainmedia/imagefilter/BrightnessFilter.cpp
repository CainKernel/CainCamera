//
// Created by Administrator on 2018/3/9.
//

#include "BrightnessFilter.h"
#include "Color.h"

BrightnessFilter::BrightnessFilter(int *pixels, int width, int height)
        : IImageFilter(pixels, width, height) {

}

void BrightnessFilter::processImage(int *destPixels) {
    for (int i = 0; i < width * height; i++) {
        Color color(*(pixels + i));
        int r = color.red() + brightness;
        int g = color.green() + brightness;
        int b = color.blue() + brightness;

        r = min(255, max(0, r));
        g = min(255, max(0, g));
        b = min(255, max(0, b));

        *(destPixels + i) = rgb2Color(r, g, b);
    }
}