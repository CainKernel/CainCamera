//
// Created by Administrator on 2018/3/9.
//

#ifndef CAINCAMERA_SHARPNESSFILTER_H
#define CAINCAMERA_SHARPNESSFILTER_H


#include "IImageFilter.h"

class SharpnessFilter : public IImageFilter {

public:
    SharpnessFilter(int *pixels, int width, int height);

    void setSharpness(float sharpness) {
        this->sharpness = sharpness;
    }

    void processImage(int *destPixels) override;

private:
    float sharpness;
};


#endif //CAINCAMERA_SHARPNESSFILTER_H
