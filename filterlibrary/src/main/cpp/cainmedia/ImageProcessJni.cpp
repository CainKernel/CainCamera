//
// Created by Administrator on 2018/3/6.
//

#include <android/bitmap.h>



#include "ImageProcessJni.h"
#include "ImageProcess.h"

JNIEXPORT jboolean JNICALL
Java_com_cgfay_cainfilter_ImageFilter_NativeFilter_setBrightness(JNIEnv *env, jclass type, jfloat brightness, jobject srcBitmap, jobject destBitmap) {
    ImageProcess *imageProcess = new ImageProcess(env, &srcBitmap, &destBitmap);
    imageProcess->setImageAdjustValue(Brightness, brightness);
    bool result = imageProcess->processImage();
    delete imageProcess;
    return result;
}

JNIEXPORT jboolean JNICALL
Java_com_cgfay_cainfilter_ImageFilter_NativeFilter_setContrast(JNIEnv *env, jclass type, jfloat contrast, jobject srcBitmap, jobject destBitmap) {
    ImageProcess *imageProcess = new ImageProcess(env, &srcBitmap, &destBitmap);
    imageProcess->setImageAdjustValue(Contrast, contrast);
    bool result = imageProcess->processImage();
    delete imageProcess;
    return result;
}

JNIEXPORT jboolean JNICALL
Java_com_cgfay_cainfilter_ImageFilter_NativeFilter_setExposure(JNIEnv *env, jclass type, jfloat exposure, jobject srcBitmap, jobject destBitmap) {
    ImageProcess *imageProcess = new ImageProcess(env, &srcBitmap, &destBitmap);
    imageProcess->setImageAdjustValue(Exposure, exposure);
    bool result = imageProcess->processImage();
    delete imageProcess;
    return result;
}

JNIEXPORT jboolean JNICALL
Java_com_cgfay_cainfilter_ImageFilter_NativeFilter_setHue(JNIEnv *env, jclass type, jfloat hue, jobject srcBitmap, jobject destBitmap) {
    ImageProcess *imageProcess = new ImageProcess(env, &srcBitmap, &destBitmap);
    imageProcess->setImageAdjustValue(Hue, hue);
    bool result = imageProcess->processImage();
    delete imageProcess;
    return result;
}

JNIEXPORT jboolean JNICALL
Java_com_cgfay_cainfilter_ImageFilter_NativeFilter_setSaturation(JNIEnv *env, jclass type, jfloat saturation, jobject srcBitmap, jobject destBitmap) {
    ImageProcess *imageProcess = new ImageProcess(env, &srcBitmap, &destBitmap);
    imageProcess->setImageAdjustValue(Saturation, saturation);
    bool result = imageProcess->processImage();
    delete imageProcess;
    return result;
}

JNIEXPORT jboolean JNICALL
Java_com_cgfay_cainfilter_ImageFilter_NativeFilter_setSharpness(JNIEnv *env, jclass type, jfloat sharpness, jobject srcBitmap, jobject destBitmap) {
    ImageProcess *imageProcess = new ImageProcess(env, &srcBitmap, &destBitmap);
    imageProcess->setImageAdjustValue(Sharpness, sharpness);
    bool result = imageProcess->processImage();
    delete imageProcess;
    return result;
}

JNIEXPORT jboolean JNICALL
Java_com_cgfay_cainfilter_ImageFilter_NativeFilter_grayFilter(JNIEnv *env, jclass type, jobject srcBitmap, jobject destBitmap) {
    ImageProcess *imageProcess = new ImageProcess(env, &srcBitmap, &destBitmap);
    imageProcess->changeFilter(Gray);
    bool result = imageProcess->processImage();
    delete imageProcess;
    return result;
}

JNIEXPORT jboolean JNICALL
Java_com_cgfay_cainfilter_ImageFilter_NativeFilter_mosaicFilter(JNIEnv *env, jclass type, jfloat mosaicSize, jobject srcBitmap, jobject destBitmap) {
    ImageProcess *imageProcess = new ImageProcess(env, &srcBitmap, &destBitmap);
    imageProcess->changeFilter(Mosaic, mosaicSize > 0 ? mosaicSize : -1);
    bool result = imageProcess->processImage();
    delete imageProcess;
    return result;
}