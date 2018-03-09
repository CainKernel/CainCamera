//
// Created by Administrator on 2018/3/9.
//

#ifndef CAINCAMERA_HUEFILTER_H
#define CAINCAMERA_HUEFILTER_H


#include "IImageFilter.h"

class HueFilter : public IImageFilter {

public:
    HueFilter(int *pixels, int width, int height);

    void setHue(float hue) {
        this->hue = hue;
    }

    void processImage(int *destPixels) override;

private:
    float hue;
};


#endif //CAINCAMERA_HUEFILTER_H
