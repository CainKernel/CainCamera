//
// Created by cain on 2018/9/8.
//

#ifndef CAINCAMERA_HISTOGRAMFILTER_H
#define CAINCAMERA_HISTOGRAMFILTER_H


#include "ImageFilter.h"

/**
 * 直方图滤镜
 */
class HistogramEqualFilter : public ImageFilter {

public:
    HistogramEqualFilter();

    virtual ~HistogramEqualFilter();

    int process(void *pixels, unsigned int width, unsigned int height) override;

private:
    float contrastIntensity;
};


#endif //CAINCAMERA_HISTOGRAMFILTER_H
