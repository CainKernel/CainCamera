//
// Created by CainHuang on 2019/8/17.
//

#include <libyuv/rotate.h>
#include "AVFormatter.h"

/**
 * 将图像数据转成YUV420P格式的图像数据
 * @param image
 * @param length
 * @param width
 * @param height
 * @param pixelFormat
 * @return
 */
YuvData *convertToYuv420P(uint8_t *image, int length, int width, int height, int pixelFormat) {

    if (length <= 0 || width <= 0 || height <= 0 || pixelFormat <= 0 || !image) {
        return nullptr;
    }

    YuvData *yuvData = new YuvData(width, height);
    libyuv::ConvertToI420(image, (size_t) length,
                          yuvData->dataY, yuvData->lineSizeY,
                          yuvData->dataU, yuvData->lineSizeU,
                          yuvData->dataV, yuvData->lineSizeV,
                          0, 0,
                          width, height,
                          width, height,
                          libyuv::kRotate0,
                          getFourCC((PixelFormat)pixelFormat));
    return yuvData;
}

/**
 * 将图像数据转换成YUV420P格式
 * @param mediaData
 * @return
 */
YuvData* convertToYuv420P(AVMediaData *mediaData) {
    if (!mediaData || mediaData->length <= 0 || mediaData->type != MediaVideo || mediaData->width <= 0
        || mediaData->height <= 0 || mediaData->pixelFormat <= 0 || !mediaData->image) {
        return nullptr;
    }
    auto yuv = new YuvData(mediaData->width, mediaData->height);
    libyuv::ConvertToI420(mediaData->image, (size_t) mediaData->length,
                          yuv->dataY, yuv->lineSizeY,
                          yuv->dataU, yuv->lineSizeU,
                          yuv->dataV, yuv->lineSizeV,
                          0, 0,
                          mediaData->width, mediaData->height,
                          mediaData->width, mediaData->height,
                          libyuv::kRotate0,
                          getFourCC((PixelFormat)mediaData->pixelFormat));
    return yuv;
}