//
// Created by cain on 2018/9/8.
//

#ifndef CAINCAMERA_BLACKWHITEFILTER_H
#define CAINCAMERA_BLACKWHITEFILTER_H


#include "ImageFilter.h"

/**
 * 黑白滤镜
 */
class BlackWhiteFilter : public ImageFilter {

public:
    BlackWhiteFilter();

    virtual ~BlackWhiteFilter();

    int process(void *pixels, unsigned int width, unsigned int height) override;
};


#endif //CAINCAMERA_BLACKWHITEFILTER_H
