//
// Created by Administrator on 2018/3/6.
//

#include "ImageProcess.h"
#include "GrayFilter.h"

JNIEXPORT jintArray JNICALL
Java_com_cgfay_cainfilter_ImageFilter_NativeFilter_grayFilter(JNIEnv *env, jclass type,
                                                                    jintArray pixels_, jint width,
                                                                    jint height) {
    jint *pixels = env->GetIntArrayElements(pixels_, NULL);

    GrayFilter filter(pixels, width, height);
    pixels = filter.processImage();
    jintArray result = jintToArray(env, width * height, pixels);

    env->ReleaseIntArrayElements(pixels_, pixels, 0);
    return result;
}