//
// Created by cain on 2018/9/8.
//

#ifndef CAINCAMERA_COLORQUANTIZEFILTER_H
#define CAINCAMERA_COLORQUANTIZEFILTER_H

#include "ImageFilter.h"

/**
 * 色彩量化滤镜
 */
class ColorQuantizeFilter : public ImageFilter {
public:
    ColorQuantizeFilter();

    virtual ~ColorQuantizeFilter();

    int process(void *pixels, unsigned int width, unsigned int height) override;

    void setLevels(float levels);
private:
    float levels;
};


#endif //CAINCAMERA_COLORQUANTIZEFILTER_H
