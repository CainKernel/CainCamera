//
// Created by CainHuang on 2019/3/22.
//

#ifndef GLCONTRASTFILTER_H
#define GLCONTRASTFILTER_H

#include <filter/GLIntensityFilter.h>

/**
 * 对比度滤镜
 */
class GLContrastFilter : public GLIntensityFilter {

public:
    void initProgram() override;
};


#endif //GLCONTRASTFILTER_H
