//
// Created by cain on 2018/9/8.
//

#include "MosaicFilter.h"
#include "FilterUtils.h"
#include <stdlib.h>

MosaicFilter::MosaicFilter() : mosaicSize(0) {

}

MosaicFilter::~MosaicFilter() {

}

/**
 * 处理过程
 * @param pixels
 * @param width
 * @param height
 * @return
 */
int MosaicFilter::process(void *pixels, unsigned int width, unsigned int height) {
    if (width == 0 || height == 0 || mosaicSize <= 1) {
        return -1;
    }

    uint32_t x, y;
    uint32_t sumAlpha = 0;
    uint32_t sumRed = 0;
    uint32_t sumGreen = 0;
    uint32_t sumBlue = 0;

    uint32_t offsetX = 0;
    uint32_t offsetY = 0;

    // 像素值
    int32_t *currentPixels = (int32_t *) pixels;

    // 马赛克格子遍历
    for (y = 0; y < height; y += mosaicSize) {
        for (x = 0; x < width; x += mosaicSize) {

            // 偏移的宽高值
            offsetX = x + mosaicSize > width ? width : x + mosaicSize;
            offsetY = y + mosaicSize > height ? height : y + mosaicSize;

            sumAlpha = 0;
            sumRed = 0;
            sumGreen = 0;
            sumBlue = 0;
            uint32_t count = 0;

            // 计算马赛克的总颜色值
            for (int j = y; j < offsetY; j++) {
                for (int i = x; i < offsetX; i++) {
                    int32_t color = currentPixels[j * width + i];

                    uint8_t alpha = ((color & 0xFF000000) >> 24);
                    uint8_t red = (color & 0x000000FF);
                    uint8_t green = ((color & 0x0000FF00) >> 8);
                    uint8_t blue = ((color & 0x00FF0000) >> 16);

                    sumRed += red;
                    sumGreen += green;
                    sumBlue += blue;
                    sumAlpha += alpha;

                    count++;
                }
            }

            // 计算当前马赛克格子的平均颜色值
            uint32_t red = sumRed / count;
            uint32_t green = sumGreen / count;
            uint32_t blue = sumBlue / count;
            uint32_t alpha = sumAlpha / count;

            // 将计算的结果赋值
            for (int j = y; j < offsetY; j++) {
                for (int i = x; i < offsetX; i++) {
                    currentPixels[j * width + i] = ARGB_COLOR(alpha, red, green, blue);
                }
            }
        }
    }
    return 0;
}

/**
 * 设置马赛克大小
 * @param size
 */
void MosaicFilter::setMosaicSize(int size) {
    this->mosaicSize = size;
}
