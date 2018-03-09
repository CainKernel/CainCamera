//
// Created by Administrator on 2018/3/9.
//

#include "MosaicFilter.h"

MosaicFilter::MosaicFilter(int *pixels, int width, int height)
        : IImageFilter(pixels, width, height), mosaicSize(4) {

}

void MosaicFilter::processImage(int *destPixels) {
    int r = 0, g = 0, b = 0;
    for (int y = 0; y < height; ++y) {
        for (int x = 0; x < width; ++x) {
            int value = *(pixels + y * width + x);
            if ((y % mosaicSize) == 0) {
                if ((x % mosaicSize) == 0) {
                    Color color(value);
                    r = color.red();
                    g = color.green();
                    b = color.blue();
                    *(destPixels +  y * width + x) = rgb2Color(r, g, b);
                }
            } else {
                *(destPixels +  y * width + x) = *(pixels + (y - 1) * width + x);
            }
        }
    }
}