//
// Created by CainHuang on 2019/3/22.
//

#ifndef GLIMAGEADJUSTFILTER_H
#define GLIMAGEADJUSTFILTER_H


#include <filter/GLGroupFilter.h>

class GLImageAdjustFilter : public GLGroupFilter {
public:
    GLImageAdjustFilter();

    void setAdjustIntensity(const float *adjust);
};


#endif //GLIMAGEADJUSTFILTER_H
