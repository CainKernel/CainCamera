//
// Created by CainHuang on 2019/3/22.
//

#ifndef GLEXPOSUREFILTER_H
#define GLEXPOSUREFILTER_H

#include <filter/GLIntensityFilter.h>

/**
 * 曝光滤镜
 */
class GLExposureFilter : public GLIntensityFilter {

public:
    void initProgram() override;
};


#endif //GLEXPOSUREFILTER_H
