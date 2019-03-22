//
// Created by CainHuang on 2019/3/22.
//

#ifndef GLINTENSITYFILTER_H
#define GLINTENSITYFILTER_H

#include "GLFilter.h"

/**
 * 带intensity强度绑定的滤镜
 */
class GLIntensityFilter : public GLFilter {
public:
    GLIntensityFilter();

    void initProgram(const char *vertexShader, const char *fragmentShader) override;

protected:
    void onDrawBegin() override;

protected:
    int intensityHandle;
};


#endif //GLINTENSITYFILTER_H
