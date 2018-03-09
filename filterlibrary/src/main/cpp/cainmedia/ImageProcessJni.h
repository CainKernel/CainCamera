//
// Created by Administrator on 2018/3/6.
//

#ifndef CAINCAMERA_IMAGEPROCESSJNI_H
#define CAINCAMERA_IMAGEPROCESSJNI_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

// ------------------------------------------ 图片调节 ---------------------------------------------
JNIEXPORT jboolean JNICALL
Java_com_cgfay_cainfilter_ImageFilter_NativeFilter_setBrightness(JNIEnv *env, jclass type, jfloat brightness, jobject srcBitmap, jobject destBitmap);

JNIEXPORT jboolean JNICALL
Java_com_cgfay_cainfilter_ImageFilter_NativeFilter_setContrast(JNIEnv *env, jclass type, jfloat contrast, jobject srcBitmap, jobject destBitmap);

JNIEXPORT jboolean JNICALL
Java_com_cgfay_cainfilter_ImageFilter_NativeFilter_setExposure(JNIEnv *env, jclass type, jfloat exposure, jobject srcBitmap, jobject destBitmap);

JNIEXPORT jboolean JNICALL
Java_com_cgfay_cainfilter_ImageFilter_NativeFilter_setHue(JNIEnv *env, jclass type, jfloat hue, jobject srcBitmap, jobject destBitmap);

JNIEXPORT jboolean JNICALL
Java_com_cgfay_cainfilter_ImageFilter_NativeFilter_setSaturation(JNIEnv *env, jclass type, jfloat saturation, jobject srcBitmap, jobject destBitmap);

JNIEXPORT jboolean JNICALL
Java_com_cgfay_cainfilter_ImageFilter_NativeFilter_setSharpness(JNIEnv *env, jclass type, jfloat sharpness, jobject srcBitmap, jobject destBitmap);


// -------------------------------------------- 滤镜 ----------------------------------------------
JNIEXPORT jboolean JNICALL
Java_com_cgfay_cainfilter_ImageFilter_NativeFilter_grayFilter(JNIEnv *env, jclass type, jobject srcBitmap, jobject destBitmap);

JNIEXPORT jboolean JNICALL
Java_com_cgfay_cainfilter_ImageFilter_NativeFilter_mosaicFilter(JNIEnv *env, jclass type, jfloat mosaicSize, jobject srcBitmap, jobject destBitmap);

#ifdef __cplusplus
};
#endif

#endif //CAINCAMERA_IMAGEPROCESSJNI_H
