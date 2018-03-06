//
// Created by Administrator on 2018/3/6.
//

#ifndef CAINCAMERA_GRAYFILTER_H
#define CAINCAMERA_GRAYFILTER_H


#include "IImageFilter.h"

class GrayFilter : public IImageFilter {
public:
    GrayFilter(int *pixels, int width, int height);

    int *processImage() override;
};


#endif //CAINCAMERA_GRAYFILTER_H
