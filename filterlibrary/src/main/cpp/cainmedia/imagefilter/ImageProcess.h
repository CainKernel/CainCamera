//
// Created by Administrator on 2018/3/9.
//

#ifndef CAINCAMERA_IMAGEPROCESS_H
#define CAINCAMERA_IMAGEPROCESS_H

#include <jni.h>
#include "FilterType.h"

class ImageProcess {

public:
    ImageProcess(JNIEnv *env, jobject *srcBitmap, jobject *destBitmap);
    virtual ~ImageProcess();

    void changeFilter(FilterType type, float value = -1);

    void setImageAdjustValue(AdjustType type, float value);

    bool processImage();

private:
    JNIEnv *env;
    jobject *srcBitmap, *destBitmap;
    FilterType filterType;
    AdjustType adjustType;
    float filterValue;
    float adjustValue;

private:
    bool processAdjust();
    bool processFilter();
};


#endif //CAINCAMERA_IMAGEPROCESS_H
