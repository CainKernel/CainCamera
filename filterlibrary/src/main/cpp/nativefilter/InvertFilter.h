//
// Created by cain on 2018/9/8.
//

#ifndef CAINCAMERA_INVERTFILTER_H
#define CAINCAMERA_INVERTFILTER_H


#include "ImageFilter.h"

/**
 * 反色滤镜
 */
class InvertFilter : public ImageFilter {
public:
    InvertFilter();

    virtual ~InvertFilter();

    int process(void *pixels, unsigned int width, unsigned int height) override;
};

#endif //CAINCAMERA_INVERTFILTER_H
