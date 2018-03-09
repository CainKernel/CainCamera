//
// Created by Administrator on 2018/3/9.
//

#ifndef CAINCAMERA_SATURATIONFILTER_H
#define CAINCAMERA_SATURATIONFILTER_H


#include "IImageFilter.h"

class SaturationFilter : public IImageFilter {

public:
    SaturationFilter(int *pixels, int width, int height);

    void setSaturation(float saturation) {
        this->saturation = saturation;
    }

    void processImage(int *destPixels) override;

private:
    float saturation;
};


#endif //CAINCAMERA_SATURATIONFILTER_H
