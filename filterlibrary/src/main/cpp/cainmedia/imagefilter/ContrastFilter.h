//
// Created by Administrator on 2018/3/9.
//

#ifndef CAINCAMERA_CONTRASTFILTER_H
#define CAINCAMERA_CONTRASTFILTER_H


#include "IImageFilter.h"

class ContrastFilter : public IImageFilter {
public:
    ContrastFilter(int *pixels, int width, int height);

    void processImage(int *destPixels) override;

    void setContrast(float contrast) {
        this->contrast = contrast;
    }

private:
    float contrast;
};


#endif //CAINCAMERA_CONTRASTFILTER_H
