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
YuvData* convertToYuvData(uint8_t *image, int length, int width, int height, int pixelFormat) {

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
YuvData* convertToYuvData(AVMediaData *mediaData) {
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

/**
 * 将AVFrame的数据转换成YUV420P格式
 * @param frame
 * @return
 */
YuvData* convertToYuvData(AVFrame *frame) {
    if (!frame || frame->format == AV_PIX_FMT_NONE || frame->width <= 0 || frame->height <= 0) {
        return nullptr;
    }
    auto yuv = new YuvData(frame->width, frame->height);
    switch (frame->format) {
        case AV_PIX_FMT_ARGB: {
            libyuv::ARGBToI420(frame->data[0], frame->linesize[0],
                               yuv->dataY, yuv->lineSizeY,
                               yuv->dataU, yuv->lineSizeU,
                               yuv->dataV, yuv->lineSizeV,
                               frame->width, frame->height);
            break;
        }

        case AV_PIX_FMT_RGBA: {
            libyuv::RGBAToI420(frame->data[0], frame->linesize[0],
                               yuv->dataY, yuv->lineSizeY,
                               yuv->dataU, yuv->lineSizeU,
                               yuv->dataV, yuv->lineSizeV,
                               frame->width, frame->height);
            break;
        }

        case AV_PIX_FMT_ABGR: {
            libyuv::ABGRToI420(frame->data[0], frame->linesize[0],
                               yuv->dataY, yuv->lineSizeY,
                               yuv->dataU, yuv->lineSizeU,
                               yuv->dataV, yuv->lineSizeV,
                               frame->width, frame->height);
            break;
        }

        case AV_PIX_FMT_BGRA: {
            libyuv::BGRAToI420(frame->data[0], frame->linesize[0],
                               yuv->dataY, yuv->lineSizeY,
                               yuv->dataU, yuv->lineSizeU,
                               yuv->dataV, yuv->lineSizeV,
                               frame->width, frame->height);
            break;
        }

        case AV_PIX_FMT_RGB24: {
            libyuv::RGB24ToI420(frame->data[0], frame->linesize[0],
                                yuv->dataY, yuv->lineSizeY,
                                yuv->dataU, yuv->lineSizeU,
                                yuv->dataV, yuv->lineSizeV,
                                frame->width, frame->height);
            break;
        }

        case AV_PIX_FMT_YUVJ420P:
        case AV_PIX_FMT_YUV420P: {
            libyuv::I420ToI420(frame->data[0], frame->linesize[0],
                             frame->data[1], frame->linesize[1],
                             frame->data[2], frame->linesize[2],
                             yuv->dataY, yuv->lineSizeY,
                             yuv->dataU, yuv->lineSizeU,
                             yuv->dataV, yuv->lineSizeV,
                             frame->width, frame->height);
            break;
        }

        case AV_PIX_FMT_YUV444P: {
            libyuv::I444ToI420(frame->data[0], frame->linesize[0],
                               frame->data[1], frame->linesize[1],
                               frame->data[2], frame->linesize[2],
                               yuv->dataY, yuv->lineSizeY,
                               yuv->dataU, yuv->lineSizeU,
                               yuv->dataV, yuv->lineSizeV,
                               frame->width, frame->height);
            break;
        }

        case AV_PIX_FMT_NV12: {
            libyuv::NV12ToI420(frame->data[0], frame->linesize[0],
                               frame->data[1], frame->linesize[1],
                               yuv->dataY, yuv->lineSizeY,
                               yuv->dataU, yuv->lineSizeU,
                               yuv->dataV, yuv->lineSizeV,
                               frame->width, frame->height);
            break;
        }

        case AV_PIX_FMT_NV21: {
            libyuv::NV21ToI420(frame->data[0], frame->linesize[0],
                               frame->data[1], frame->linesize[1],
                               yuv->dataY, yuv->lineSizeY,
                               yuv->dataU, yuv->lineSizeU,
                               yuv->dataV, yuv->lineSizeV,
                               frame->width, frame->height);
            break;
        }

        // 其他格式暂不支持，直接返回空对象
        default:
            delete yuv;
            yuv = nullptr;
            break;
    }
    return yuv;
}


/**
 * 将YuvData对象转换为AVMediaData
 * @param mediaData
 * @param yuvData
 * @param width
 * @param height
 * @return
 */
void fillVideoData(AVMediaData *mediaData, YuvData *yuvData, int width, int height) {
    auto image = new uint8_t[width * height * 3 / 2];
    if (mediaData != nullptr) {
        mediaData->free();
    } else {
        mediaData = new AVMediaData();
    }
    mediaData->image = image;
    memcpy(mediaData->image, yuvData->dataY, (size_t) width * height);
    memcpy(mediaData->image + width * height, yuvData->dataU, (size_t) width * height / 4);
    memcpy(mediaData->image + width * height * 5 / 4, yuvData->dataV, (size_t) width * height / 4);
    mediaData->length = width * height * 3 / 2;
    mediaData->width = width;
    mediaData->height = height;
    mediaData->pixelFormat = PIXEL_FORMAT_YUV420P;
    mediaData->type = MediaVideo;
}
/**
 * NV12转YUV420P
 */
void NV12toYUV420Planar(uint8_t* input, int offset, uint8_t* output, int width, int height) {
    int frameSize = width * height;
    int qFrameSize = frameSize / 4;

    memmove(output, input + offset, frameSize); // Y
    int i = 0;
    for (i = 0; i < qFrameSize; i++) {
        output[frameSize + i] = input[offset + frameSize + i * 2]; // U
        output[frameSize + qFrameSize + i] = input[offset + frameSize + i * 2 + 1]; // V
    }
}

/**
 * NV21转YUV420P
 */
void NV21toYUV420Planar(uint8_t* input, int offset, uint8_t* output, int width, int height) {
    int frameSize = width * height;
    int qFrameSize = frameSize / 4;

    memmove(output, input + offset, frameSize); // Y
    int i = 0;
    for (i = 0; i < qFrameSize; i++) {
        output[frameSize + i] = input[offset + frameSize + i * 2 + 1]; // U
        output[frameSize + qFrameSize + i] = input[offset + frameSize + i * 2]; // V
    }
}

/**
 * YUV420P转YUV420SP
 */
void I420toYUV420SemiPlanar(uint8_t* input, int offset, uint8_t* output, int width, int height) {
    int frameSize = width * height;
    int qFrameSize = frameSize / 4;

    memmove(output, input + offset, frameSize); // Y
    int i = 0;
    for (i = 0; i < qFrameSize; i++) {
        output[frameSize + i * 2] = input[offset + frameSize + i]; // Cb (U)
        output[frameSize + i * 2 + 1] = input[offset + frameSize + i + qFrameSize]; // Cr (V)
    }
}

/**
 * YUV420P转NV21
 */
void I420toNV21(uint8_t* input, int offset, uint8_t* output, int width, int height) {
    int frameSize = width * height;
    int qFrameSize = frameSize / 4;

    memmove(output, input + offset, frameSize); // Y
    int i = 0;
    for (i = 0; i < qFrameSize; i++) {
        output[frameSize + i * 2 + 1] = input[offset + frameSize + i]; // Cb (U)
        output[frameSize + i * 2] = input[offset + frameSize + i + qFrameSize]; // Cr (V)
    }
}

/**
 * NV12转NV21
 */
void NV12toNV21(uint8_t* input, int offset, uint8_t* output, int width, int height) {
    int frameSize = width * height;
    int qFrameSize = frameSize / 2;

    memmove(output, input + offset, frameSize); // Y
    int i = 0;
    for (i = 0; i + 1 < qFrameSize; i += 2) {
        output[frameSize + i] = input[offset + frameSize + i + 1]; // U
        output[frameSize + i + 1] = input[offset + frameSize + i]; // V
    }
}