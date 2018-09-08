//
// Created by cain on 2018/9/8.
//

#ifndef CAINCAMERA_MOSAICFILTER_H
#define CAINCAMERA_MOSAICFILTER_H


#include "ImageFilter.h"

/**
 * 马赛克滤镜
 */
class MosaicFilter : public ImageFilter{

public:
    MosaicFilter();

    virtual ~MosaicFilter();

    int process(void *pixels, unsigned int width, unsigned int height) override;

    // 马赛克大小
    void setMosaicSize(int size);

private:
    int mosaicSize;
};


#endif //CAINCAMERA_MOSAICFILTER_H
