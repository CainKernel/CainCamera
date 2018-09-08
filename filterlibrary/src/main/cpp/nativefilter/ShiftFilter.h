//
// Created by cain on 2018/9/8.
//

#ifndef CAINCAMERA_SHIFTFILTER_H
#define CAINCAMERA_SHIFTFILTER_H


#include "ImageFilter.h"

class ShiftFilter : public ImageFilter {
public:
    ShiftFilter();

    virtual ~ShiftFilter();

    int process(void *pixels, unsigned int width, unsigned int height) override;

    void setAmount(int amount);

private:
    int amount;
};


#endif //CAINCAMERA_SHIFTFILTER_H
