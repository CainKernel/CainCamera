//
// Created by CainHuang on 2019/8/17.
//

#ifndef AVFORMATTER_H
#define AVFORMATTER_H

#include <libyuv.h>
#include <string>
extern "C" {
#include <libavutil/pixfmt.h>
#include <libavutil/samplefmt.h>
};

#include "YuvData.h"
#include "AVMediaData.h"

// 图像像素格式
enum PixelFormat {
    PIXEL_FORMAT_NONE    = 0,
    PIXEL_FORMAT_NV21    = 1,
    PIXEL_FORMAT_YV12    = 2,
    PIXEL_FORMAT_NV12    = 3,
    PIXEL_FORMAT_YUV420P = 4,
    PIXEL_FORMAT_YUV420SP = 5,
    PIXEL_FORMAT_ARGB    = 6,
    PIXEL_FORMAT_ABGR    = 7,
    PIXEL_FORMAT_RGBA    = 8,
};

// 音频采样格式
enum SampleFormat {
    SAMPLE_FORMAT_8BIT   = 8,
    SAMPLE_FORMAT_16BIT  = 16,
    SAMPLE_FORMAT_FLOAT  = 32,
};

// 旋转角度枚举
enum Rotation {
    ROTATE_0,
    ROTATE_90,
    ROTATE_180,
    ROTATE_270
};

/**
 * 根据角度转换角度枚举
 * @param degree
 * @return
 */
inline Rotation getRotation(int degree) {
    switch (degree) {
        case 90: {
            return ROTATE_90;
        }

        case 180: {
            return ROTATE_180;
        }

        case 270: {
            return ROTATE_270;
        }

        default: {
            return ROTATE_0;
        }
    }
}

/**
 * 根据旋转角度枚举获取实际的角度
 */
inline int getDegree(Rotation rotation) {
    switch (rotation) {
        case ROTATE_90: {
            return 90;
        }

        case ROTATE_180: {
            return 180;
        }

        case ROTATE_270: {
            return 270;
        }

        default: {
            return 0;
        }
    }
}

/**
 * 获取libyuv的旋转模式
 * @param degree
 * @return
 */
inline libyuv::RotationMode getRotationMode(int degree) {
    switch (degree) {
        case 90: {
            return libyuv::kRotate90;
        }
        case 180: {
            return libyuv::kRotate180;
        }
        case 270: {
            return libyuv::kRotate270;
        }
        default: {
            return libyuv::kRotate0;
        }
    }
}

/**
 * 获取libyuv的旋转模式
 * @param degree
 * @return
 */
inline libyuv::RotationMode getRotationMode(Rotation rotation) {
    switch (rotation) {
        case ROTATE_90: {
            return libyuv::kRotate90;
        }

        case ROTATE_180: {
            return libyuv::kRotate180;
        }

        case ROTATE_270: {
            return libyuv::kRotate270;
        }

        default: {
            return libyuv::kRotate0;
        }
    }
}

/**
 * 根据libyuv的旋转角度模式获取旋转角度
 * @param rotationMode
 * @return
 */
inline int getDegree(libyuv::RotationMode rotationMode) {
    switch (rotationMode) {
        case libyuv::kRotate90: {
            return 90;
        }

        case libyuv::kRotate180: {
            return 180;
        }

        case libyuv::kRotate270: {
            return 270;
        }

        default: {
            return 0;
        }
    }
}

/**
 * 将像素格式转换成yuv的格式
 * @param format
 * @return
 */
inline libyuv::FourCC getFourCC(PixelFormat format) {
    if (format == PIXEL_FORMAT_NV12) {
        return libyuv::FOURCC_NV12;
    } else if (format == PIXEL_FORMAT_NV21) {
        return libyuv::FOURCC_NV21;
    } else if (format == PIXEL_FORMAT_YV12) {
        return libyuv::FOURCC_YV12;
    } else if (format == PIXEL_FORMAT_YUV420P) {
        return libyuv::FOURCC_I420;
    } else if (format == PIXEL_FORMAT_ABGR) {
        return libyuv::FOURCC_ABGR;
    } else if (format == PIXEL_FORMAT_ARGB) {
        return libyuv::FOURCC_ARGB;
    } else if (format == PIXEL_FORMAT_RGBA) {
        return libyuv::FOURCC_RGBA;
    }
    return libyuv::FOURCC_ANY;
}

/**
 * 将像素格式转换成FFmpeg的像素格式
 * @param format
 * @return
 */
inline AVPixelFormat getPixelFormat(PixelFormat format) {
    AVPixelFormat pixelFormat = AV_PIX_FMT_NONE;
    if (format == PIXEL_FORMAT_NV21) {
        pixelFormat = AV_PIX_FMT_NV21;
    } else if (format == PIXEL_FORMAT_YV12 || format == PIXEL_FORMAT_YUV420P) {
        pixelFormat = AV_PIX_FMT_YUV420P;
    } else if (format == PIXEL_FORMAT_ABGR) {
        pixelFormat = AV_PIX_FMT_ABGR;
    }
    return pixelFormat;
}

/**
 * 将FFmpeg的像素格式转换成录制的像素格式
 * @param format
 * @return
 */
inline PixelFormat pixelFormatConvert(AVPixelFormat format) {
    PixelFormat mode = PIXEL_FORMAT_NONE;
    if (format == AV_PIX_FMT_NV12) {
        mode = PIXEL_FORMAT_NV12;
    } else if (format == AV_PIX_FMT_NV21) {
        mode = PIXEL_FORMAT_NV21;
    } else if (format == AV_PIX_FMT_YUV420P) {
        mode = PIXEL_FORMAT_YUV420P;
    }
    return mode;
}

/**
 * 将录制器采样格式转换成FFmpeg的音频采样格式
 * @param format
 * @return
 */
inline AVSampleFormat getSampleFormat(SampleFormat format) {
    AVSampleFormat sampleFormat = AV_SAMPLE_FMT_NONE;
    if (format == SAMPLE_FORMAT_8BIT) {
        sampleFormat = AV_SAMPLE_FMT_U8;
    } else if (format == SAMPLE_FORMAT_16BIT) {
        sampleFormat = AV_SAMPLE_FMT_S16;
    } else if (format == SAMPLE_FORMAT_FLOAT) {
        sampleFormat = AV_SAMPLE_FMT_FLT;
    }
    return sampleFormat;
}

/**
 * 将图像数据转成YuvData对象
 * @param image
 * @param length
 * @param width
 * @param height
 * @param pixelFormat
 * @return
 */
YuvData* convertToYuvData(uint8_t *image, int length, int width, int height, int pixelFormat);

/**
 * 将图像数据转成YuvData对象
 * @param mediaData
 * @return
 */
YuvData* convertToYuvData(AVMediaData *mediaData);

/**
 * 将AVFrame的数据转换成YuvData对象
 * @param frame
 * @return
 */
YuvData* convertToYuvData(AVFrame *frame);

/**
 * 将YuvData对象转换为AVMediaData
 * @param mediaData
 * @param yuvData
 * @param width
 * @param height
 */
void fillVideoData(AVMediaData *mediaData, YuvData *yuvData, int width, int height);

/**
 * NV12转YUV420P
 */
void NV12toYUV420Planar(uint8_t* input, int offset, uint8_t* output, int width, int height);

/**
 * NV21转YUV420P
 */
void NV21toYUV420Planar(uint8_t* input, int offset, uint8_t* output, int width, int height);

/**
 * YUV420P转YUV420SP
 */
void I420toYUV420SemiPlanar(uint8_t* input, int offset, uint8_t* output, int width, int height);

/**
 * YUV420P转NV21
 */
void I420toNV21(uint8_t* input, int offset, uint8_t* output, int width, int height);

/**
 * NV12转NV21
 */
void NV12toNV21(uint8_t* input, int offset, uint8_t* output, int width, int height);

#endif //AVFORMATTER_H
