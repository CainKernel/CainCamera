//
// Created by Administrator on 2018/3/6.
//

#ifndef CAINCAMERA_IMAGEPROCESS_H
#define CAINCAMERA_IMAGEPROCESS_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jintArray JNICALL
Java_com_cgfay_cainfilter_ImageFilter_NativeFilter_grayFilter(JNIEnv *env, jclass type,
                                                                    jintArray pixels_, jint width,
                                                                    jint height);

#ifdef __cplusplus
};
#endif

#endif //CAINCAMERA_IMAGEPROCESS_H
