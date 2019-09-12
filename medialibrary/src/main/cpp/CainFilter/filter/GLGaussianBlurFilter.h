//
// Created by CainHuang on 2019/3/16.
//

#ifndef GLGAUSSIANBLURFILTER_H
#define GLGAUSSIANBLURFILTER_H

#include "GLFilter.h"
#include "GLGroupFilter.h"

/**
 * 高斯模糊滤镜
 */
class GLGaussianBlurFilter : public GLGroupFilter {
public:
    GLGaussianBlurFilter();

    GLGaussianBlurFilter(const char *vertexShader, const char *fragmentShader);

    virtual ~GLGaussianBlurFilter();

    void setTextureSize(int width, int height) override;

    void setBlurSize(float blurSize);
};


#endif //GLGAUSSIANBLURFILTER_H
