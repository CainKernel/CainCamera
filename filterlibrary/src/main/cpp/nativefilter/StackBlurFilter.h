//
// Created by cain on 2018/9/9.
//

#ifndef CAINCAMERA_STACKBLURFILTER_H
#define CAINCAMERA_STACKBLURFILTER_H

#include "ImageFilter.h"

/**
 * 堆栈模糊
 * http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
 */
class StackBlurFilter : public ImageFilter {
public:
    StackBlurFilter();

    virtual ~StackBlurFilter();

    int process(void *pixels, unsigned int width, unsigned int height) override;

    // 设置模糊半径
    void setRadius(int radius);
private:
    // 模糊半径
    int radius;
};


#endif //CAINCAMERA_STACKBLURFILTER_H
