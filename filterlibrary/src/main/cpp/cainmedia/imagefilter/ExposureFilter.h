//
// Created by Administrator on 2018/3/9.
//

#ifndef CAINCAMERA_EXPOSUREFILTER_H
#define CAINCAMERA_EXPOSUREFILTER_H


#include "IImageFilter.h"

class ExposureFilter : public IImageFilter {
public:
    ExposureFilter(int *pixels, int width, int height);

    void setExposure(float exposure) {
        this->exposure = exposure;
    }

    void processImage(int *destPixels) override;

private:
    float exposure;
};


#endif //CAINCAMERA_EXPOSUREFILTER_H
