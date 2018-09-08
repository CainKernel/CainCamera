//
// Created by cain on 2018/9/7.
//

#include <jni.h>
#include <android/bitmap.h>
#include "common/AndroidLog.h"

#include "nativefilter/ndkfilter.h"

/**
 * 马赛克处理
 */
extern "C"
JNIEXPORT jint JNICALL
Java_com_cgfay_filterlibrary_ndkfilter_ImageFilter_nativeMosaic(JNIEnv *env, jobject instance,
        jobject sourceBitmap, jint radius) {

    AndroidBitmapInfo sourceInfo;
    void *pixels;
    int result;

    if ((result = AndroidBitmap_getInfo(env, sourceBitmap, &sourceInfo)) < 0) {
        ALOGE("Source bitmap call AndroidBitmap_getInfo() failed! error: %d", result);
        return -1;
    }

    if (sourceInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        ALOGE("Source bitmap info format is not RGBA_8888");
        return -1;
    }

    // 处理bitmap之前加锁
    if ((result = AndroidBitmap_lockPixels(env, sourceBitmap, &pixels)) < 0) {
        ALOGE("Source bitmap call AndroidBitmap_lockPixels() failed! error: %d", result);
        return -1;
    }

    // 处理马赛克
    MosaicFilter *mosaicFilter = new MosaicFilter();
    mosaicFilter->setMosaicSize(radius);
    mosaicFilter->process(pixels,  sourceInfo.width, sourceInfo.height);
    delete mosaicFilter;

    AndroidBitmap_unlockPixels(env, sourceBitmap);

    return result;
}

/**
 * 查找表滤镜
 */
extern "C"
JNIEXPORT jint JNICALL
Java_com_cgfay_filterlibrary_ndkfilter_ImageFilter_nativeLookupTable(JNIEnv *env, jobject instance,
                                                                     jobject bitmap,
                                                                     jobject lookupTable) {

    AndroidBitmapInfo sourceInfo;
    void *pixels;
    int result;

    AndroidBitmapInfo lookupTableInfo;
    void *lookupTablePixels;

    if ((result = AndroidBitmap_getInfo(env, bitmap, &sourceInfo)) < 0) {
        ALOGE("Source bitmap call AndroidBitmap_getInfo() failed! error: %d", result);
        return -1;
    }

    if ((result = AndroidBitmap_getInfo(env, lookupTable, &lookupTableInfo)) < 0) {
        ALOGE("Result bitmap call AndroidBitmap_getInfo() failed! error: %d", result);
        return -1;
    }

    if (sourceInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        ALOGE("Source bitmap info format is not RGBA_8888");
        return -1;
    }

    if (lookupTableInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        ALOGE("Result bitmap info format is not RGBA_8888");
        return -1;
    }

    // 处理bitmap之前加锁
    if ((result = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
        ALOGE("Source bitmap call AndroidBitmap_lockPixels() failed! error: %d", result);
        return -1;
    }

    if ((result = AndroidBitmap_lockPixels(env, lookupTable, &lookupTablePixels)) < 0) {
        ALOGE("Result bitmap call AndroidBitmap_lockPixels() failed! error: %d", result);
        return -1;
    }

    LookupTableFilter *lookupTableFilter = new LookupTableFilter();
    lookupTableFilter->setStride(sourceInfo.stride);
    lookupTableFilter->setLookupPixels(lookupTablePixels);
    result = lookupTableFilter->process(pixels, sourceInfo.width, sourceInfo.height);
    delete lookupTableFilter;

    AndroidBitmap_unlockPixels(env, bitmap);
    AndroidBitmap_unlockPixels(env, lookupTable);

    return result;
}

/**
 * 反色滤镜
 */
extern "C"
JNIEXPORT jint JNICALL
Java_com_cgfay_filterlibrary_ndkfilter_ImageFilter_nativeInvertFilter(JNIEnv *env, jobject instance,
                                                                     jobject bitmap) {
    AndroidBitmapInfo sourceInfo;
    void *pixels;
    int result;

    if ((result = AndroidBitmap_getInfo(env, bitmap, &sourceInfo)) < 0) {
        ALOGE("Source bitmap call AndroidBitmap_getInfo() failed! error: %d", result);
        return -1;
    }

    if (sourceInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        ALOGE("Source bitmap info format is not RGBA_8888");
        return -1;
    }

    // 处理bitmap之前加锁
    if ((result = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
        ALOGE("Source bitmap call AndroidBitmap_lockPixels() failed! error: %d", result);
        return -1;
    }

    InvertFilter *filter = new InvertFilter();
    result = filter->process(pixels, sourceInfo.width, sourceInfo.height);
    delete filter;

    AndroidBitmap_unlockPixels(env, bitmap);

    return result;

}

extern "C"
JNIEXPORT jint JNICALL
Java_com_cgfay_filterlibrary_ndkfilter_ImageFilter_nativeBlackWhiteFilter(JNIEnv *env,
                                                                          jobject instance,
                                                                          jobject bitmap) {

    AndroidBitmapInfo sourceInfo;
    void *pixels;
    int result;

    if ((result = AndroidBitmap_getInfo(env, bitmap, &sourceInfo)) < 0) {
        ALOGE("Source bitmap call AndroidBitmap_getInfo() failed! error: %d", result);
        return -1;
    }

    if (sourceInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        ALOGE("Source bitmap info format is not RGBA_8888");
        return -1;
    }

    // 处理bitmap之前加锁
    if ((result = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
        ALOGE("Source bitmap call AndroidBitmap_lockPixels() failed! error: %d", result);
        return -1;
    }

    BlackWhiteFilter *filter = new BlackWhiteFilter();
    result = filter->process(pixels, sourceInfo.width, sourceInfo.height);
    delete filter;

    AndroidBitmap_unlockPixels(env, bitmap);

    return result;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_cgfay_filterlibrary_ndkfilter_ImageFilter_nativeBrightContrastFilter(JNIEnv *env,
                                                                          jobject instance,
                                                                          jobject bitmap,
                                                                          jfloat brightness,
                                                                          jfloat contrast) {

    AndroidBitmapInfo sourceInfo;
    void *pixels;
    int result;

    if ((result = AndroidBitmap_getInfo(env, bitmap, &sourceInfo)) < 0) {
        ALOGE("Source bitmap call AndroidBitmap_getInfo() failed! error: %d", result);
        return -1;
    }

    if (sourceInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        ALOGE("Source bitmap info format is not RGBA_8888");
        return -1;
    }

    // 处理bitmap之前加锁
    if ((result = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
        ALOGE("Source bitmap call AndroidBitmap_lockPixels() failed! error: %d", result);
        return -1;
    }

    BrightContrastFilter *filter = new BrightContrastFilter();
    filter->setBrightness(brightness);
    filter->setContrast(contrast);
    result = filter->process(pixels, sourceInfo.width, sourceInfo.height);
    delete filter;

    AndroidBitmap_unlockPixels(env, bitmap);

    return result;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_cgfay_filterlibrary_ndkfilter_ImageFilter_nativeColorQuantizeFilter(JNIEnv *env,
                                                                          jobject instance,
                                                                          jobject bitmap,
                                                                          jfloat levels) {

    AndroidBitmapInfo sourceInfo;
    void *pixels;
    int result;

    if ((result = AndroidBitmap_getInfo(env, bitmap, &sourceInfo)) < 0) {
        ALOGE("Source bitmap call AndroidBitmap_getInfo() failed! error: %d", result);
        return -1;
    }

    if (sourceInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        ALOGE("Source bitmap info format is not RGBA_8888");
        return -1;
    }

    // 处理bitmap之前加锁
    if ((result = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
        ALOGE("Source bitmap call AndroidBitmap_lockPixels() failed! error: %d", result);
        return -1;
    }

    ColorQuantizeFilter *filter = new ColorQuantizeFilter();
    filter->setLevels(levels);
    result = filter->process(pixels, sourceInfo.width, sourceInfo.height);
    delete filter;

    AndroidBitmap_unlockPixels(env, bitmap);

    return result;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_cgfay_filterlibrary_ndkfilter_ImageFilter_nativeHistogramEqualFilter(JNIEnv *env,
                                                                          jobject instance,
                                                                          jobject bitmap) {

    AndroidBitmapInfo sourceInfo;
    void *pixels;
    int result;

    if ((result = AndroidBitmap_getInfo(env, bitmap, &sourceInfo)) < 0) {
        ALOGE("Source bitmap call AndroidBitmap_getInfo() failed! error: %d", result);
        return -1;
    }

    if (sourceInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        ALOGE("Source bitmap info format is not RGBA_8888");
        return -1;
    }

    // 处理bitmap之前加锁
    if ((result = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
        ALOGE("Source bitmap call AndroidBitmap_lockPixels() failed! error: %d", result);
        return -1;
    }

    HistogramEqualFilter *filter = new HistogramEqualFilter();
    result = filter->process(pixels, sourceInfo.width, sourceInfo.height);
    delete filter;

    AndroidBitmap_unlockPixels(env, bitmap);

    return result;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_cgfay_filterlibrary_ndkfilter_ImageFilter_nativeShiftFilter(JNIEnv *env,
                                                                     jobject instance,
                                                                     jobject bitmap,
                                                                     jint amount) {

    AndroidBitmapInfo sourceInfo;
    void *pixels;
    int result;

    if ((result = AndroidBitmap_getInfo(env, bitmap, &sourceInfo)) < 0) {
        ALOGE("Source bitmap call AndroidBitmap_getInfo() failed! error: %d", result);
        return -1;
    }

    if (sourceInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        ALOGE("Source bitmap info format is not RGBA_8888");
        return -1;
    }

    // 处理bitmap之前加锁
    if ((result = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
        ALOGE("Source bitmap call AndroidBitmap_lockPixels() failed! error: %d", result);
        return -1;
    }

    ShiftFilter *filter = new ShiftFilter();
    filter->setAmount(amount);
    result = filter->process(pixels, sourceInfo.width, sourceInfo.height);
    delete filter;

    AndroidBitmap_unlockPixels(env, bitmap);

    return result;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_cgfay_filterlibrary_ndkfilter_ImageFilter_nativeVignetteFilter(JNIEnv *env,
                                                                     jobject instance,
                                                                     jobject bitmap,
                                                                     jfloat size) {

    AndroidBitmapInfo sourceInfo;
    void *pixels;
    int result;

    if ((result = AndroidBitmap_getInfo(env, bitmap, &sourceInfo)) < 0) {
        ALOGE("Source bitmap call AndroidBitmap_getInfo() failed! error: %d", result);
        return -1;
    }

    if (sourceInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        ALOGE("Source bitmap info format is not RGBA_8888");
        return -1;
    }

    // 处理bitmap之前加锁
    if ((result = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
        ALOGE("Source bitmap call AndroidBitmap_lockPixels() failed! error: %d", result);
        return -1;
    }

    VignetteFilter *filter = new VignetteFilter();
    filter->setVignetteSize(size);
    result = filter->process(pixels, sourceInfo.width, sourceInfo.height);
    delete filter;

    AndroidBitmap_unlockPixels(env, bitmap);

    return result;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_cgfay_filterlibrary_ndkfilter_ImageFilter_nativeGaussianBlurFilter(JNIEnv *env,
                                                                        jobject instance,
                                                                        jobject bitmap) {

    AndroidBitmapInfo sourceInfo;
    void *pixels;
    int result;

    if ((result = AndroidBitmap_getInfo(env, bitmap, &sourceInfo)) < 0) {
        ALOGE("Source bitmap call AndroidBitmap_getInfo() failed! error: %d", result);
        return -1;
    }

    if (sourceInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        ALOGE("Source bitmap info format is not RGBA_8888");
        return -1;
    }

    // 处理bitmap之前加锁
    if ((result = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
        ALOGE("Source bitmap call AndroidBitmap_lockPixels() failed! error: %d", result);
        return -1;
    }


    GaussianBlurFilter *filter = new GaussianBlurFilter();
    result = filter->process(pixels, sourceInfo.width, sourceInfo.height);
    delete filter;

    AndroidBitmap_unlockPixels(env, bitmap);

    return result;
}


extern "C"
JNIEXPORT jint JNICALL
Java_com_cgfay_filterlibrary_ndkfilter_ImageFilter_nativeStackBlurFilter(JNIEnv *env,
                                                                         jobject instance,
                                                                         jobject bitmap,
                                                                         jint radius) {

    AndroidBitmapInfo sourceInfo;
    void *pixels;
    int result;

    if ((result = AndroidBitmap_getInfo(env, bitmap, &sourceInfo)) < 0) {
        ALOGE("Source bitmap call AndroidBitmap_getInfo() failed! error: %d", result);
        return -1;
    }

    if (sourceInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        ALOGE("Source bitmap info format is not RGBA_8888");
        return -1;
    }

    // 处理bitmap之前加锁
    if ((result = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
        ALOGE("Source bitmap call AndroidBitmap_lockPixels() failed! error: %d", result);
        return -1;
    }

    StackBlurFilter *filter = new StackBlurFilter();
    filter->setRadius(radius);
    result = filter->process(pixels, sourceInfo.width, sourceInfo.height);
    delete filter;

    AndroidBitmap_unlockPixels(env, bitmap);

    return result;
}