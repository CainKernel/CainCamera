//
// Created by cain on 2018/9/8.
//

#ifndef CAINCAMERA_VIGNETTEFILTER_H
#define CAINCAMERA_VIGNETTEFILTER_H


#include "ImageFilter.h"

/**
 * 暗角滤镜
 */
class VignetteFilter : public ImageFilter {
public:
    VignetteFilter();

    virtual ~VignetteFilter();

    int process(void *pixels, unsigned int width, unsigned int height) override;

    void setVignetteSize(float size);
private:
    float size;
};


#endif //CAINCAMERA_VIGNETTEFILTER_H
