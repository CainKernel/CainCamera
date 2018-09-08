//
// Created by cain on 2018/9/8.
//

#ifndef CAINCAMERA_BRIGHTCONTRASTFILTER_H
#define CAINCAMERA_BRIGHTCONTRASTFILTER_H

#include "ImageFilter.h"

/**
 * 亮度对比度滤镜
 */
class BrightContrastFilter : public ImageFilter {
public:
    BrightContrastFilter();

    virtual ~BrightContrastFilter();

    int process(void *pixels, unsigned int width, unsigned int height) override;

    // 设置亮度
    void setBrightness(float brightness);

    // 设置对比度
    void setContrast(float contrast);

private:
    float brightness;
    float contrast;
};


#endif //CAINCAMERA_BRIGHTCONTRASTFILTER_H
