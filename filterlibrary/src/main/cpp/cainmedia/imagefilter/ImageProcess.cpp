//
// Created by Administrator on 2018/3/9.
//

#include <android/bitmap.h>

#include "ImageUtils.h"
#include "ImageProcess.h"

#include "BrightnessFilter.h"
#include "ContrastFilter.h"
#include "ExposureFilter.h"
#include "HueFilter.h"
#include "SaturationFilter.h"
#include "SharpnessFilter.h"

#include "GrayFilter.h"
#include "MosaicFilter.h"

ImageProcess::ImageProcess(JNIEnv *env, jobject *srcBitmap, jobject *destBitmap)
        : env(env), srcBitmap(srcBitmap), destBitmap(destBitmap) {
    filterType = NONE;
    adjustType = None;
    filterValue = -1;
    adjustValue = -1;
}

ImageProcess::~ImageProcess() {
    env = NULL;
    srcBitmap = NULL;
    destBitmap = NULL;
}

void ImageProcess::changeFilter(FilterType type, float value) {
    filterType = type;
    filterValue = value;
}

void ImageProcess::setImageAdjustValue(AdjustType type, float value) {
    adjustType = type;
    adjustValue = value;
}

bool ImageProcess::processImage() {
    if (adjustType != None && adjustValue != -1) {
        return processAdjust();
    } else if (filterType != NONE) {
        return processFilter();
    }
    return false;
}

bool ImageProcess::processAdjust() {
    AndroidBitmapInfo srcInfo, dstInfo;
    if (ANDROID_BITMAP_RESULT_SUCCESS != AndroidBitmap_getInfo(env, *srcBitmap, &srcInfo)
        || ANDROID_BITMAP_RESULT_SUCCESS != AndroidBitmap_getInfo(env, *destBitmap, &dstInfo)) {
        ALOGE("get bitmap info failed");
        return false;
    }

    void *srcBuf, *dstBuf;
    if (ANDROID_BITMAP_RESULT_SUCCESS != AndroidBitmap_lockPixels(env, *srcBitmap, &srcBuf)) {
        ALOGE("lock source bitmap failed");
        return false;
    }

    if (ANDROID_BITMAP_RESULT_SUCCESS != AndroidBitmap_lockPixels(env, *destBitmap, &dstBuf)) {
        ALOGE("lock destination bitmap failed");
        return false;
    }

    int width = srcInfo.width;
    int height = srcInfo.height;
    int32_t *srcPixels = (int32_t *) srcBuf;
    int32_t *desPixels = (int32_t *) dstBuf;
    IImageFilter *filter = NULL;

    switch (adjustType) {
        case Brightness:
            filter = new BrightnessFilter(srcPixels, width, height);
            ((BrightnessFilter *)filter)->setBrightness(adjustValue);
            break;

        case Contrast:
            filter = new ContrastFilter(srcPixels, width, height);
            ((ContrastFilter *) filter)->setContrast(adjustValue);
            break;

        case Exposure:
            filter = new ExposureFilter(srcPixels, width, height);
            ((ExposureFilter *) filter)->setExposure(adjustValue);
            break;

        case Hue:
            filter = new HueFilter(srcPixels, width, height);
            ((HueFilter *) filter)->setHue(adjustValue);
            break;

        case Saturation:
            filter = new SaturationFilter(srcPixels, width, height);
            ((SaturationFilter *) filter)->setSaturation(adjustValue);
            break;

        case Sharpness:
            filter = new SharpnessFilter(srcPixels, width, height);
            ((SharpnessFilter *) filter)->setSharpness(adjustValue);
            break;

        default:
            break;
    }

    if (filter != NULL) {
        filter->processImage(desPixels);
        delete filter;
    }

    AndroidBitmap_unlockPixels(env, *srcBitmap);
    AndroidBitmap_unlockPixels(env, *destBitmap);
    return true;
}

bool ImageProcess::processFilter() {

    AndroidBitmapInfo srcInfo, dstInfo;
    if (ANDROID_BITMAP_RESULT_SUCCESS != AndroidBitmap_getInfo(env, *srcBitmap, &srcInfo)
        || ANDROID_BITMAP_RESULT_SUCCESS != AndroidBitmap_getInfo(env, *destBitmap, &dstInfo)) {
        ALOGE("get bitmap info failed");
        return false;
    }

    void *srcBuf, *dstBuf;
    if (ANDROID_BITMAP_RESULT_SUCCESS != AndroidBitmap_lockPixels(env, *srcBitmap, &srcBuf)) {
        ALOGE("lock source bitmap failed");
        return false;
    }

    if (ANDROID_BITMAP_RESULT_SUCCESS != AndroidBitmap_lockPixels(env, *destBitmap, &dstBuf)) {
        ALOGE("lock destination bitmap failed");
        return false;
    }

    int width = srcInfo.width;
    int height = srcInfo.height;
    int32_t *srcPixels = (int32_t *) srcBuf;
    int32_t *desPixels = (int32_t *) dstBuf;
    IImageFilter *filter = NULL;

    switch (filterType) {

        case Gray:
            filter = new GrayFilter(srcPixels, width, height);
            break;

        case Mosaic:
            filter = new MosaicFilter(srcPixels, width, height);
            if (filterValue != -1) {
                ((MosaicFilter *) filter)->setMosaicSize(filterValue);
            }
            break;

        default:
            break;
    }

    if (filter != NULL) {
        filter->processImage(desPixels);
        delete filter;
    }

    AndroidBitmap_unlockPixels(env, *srcBitmap);
    AndroidBitmap_unlockPixels(env, *destBitmap);
    return true;
}