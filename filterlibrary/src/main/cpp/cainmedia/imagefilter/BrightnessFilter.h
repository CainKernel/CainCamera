//
// Created by Administrator on 2018/3/9.
//

#ifndef CAINCAMERA_BRIGHTNESSFILTER_H
#define CAINCAMERA_BRIGHTNESSFILTER_H


#include "IImageFilter.h"

class BrightnessFilter : public IImageFilter {
public:
    BrightnessFilter(int *pixels, int width, int height);

    void setBrightness(float brigntness) {
        this->brightness = brigntness;
    }

    void processImage(int *destPixels) override;

private:
    float brightness;
};


#endif //CAINCAMERA_BRIGHTNESSFILTER_H
