//
// Created by CainHuang on 2019/3/18.
//

#ifndef GLGAUSSIANPASSBLURFILTER_H
#define GLGAUSSIANPASSBLURFILTER_H


#include "GLFilter.h"

/**
 * 单通道(横向/径向)高斯模糊滤镜
 */
class GLGaussianPassBlurFilter : public GLFilter {
public:
    GLGaussianPassBlurFilter();

    GLGaussianPassBlurFilter(const char *vertexShader, const char *fragmentShader);

    virtual ~GLGaussianPassBlurFilter();

    void initProgram() override;

    void initProgram(const char *vertexShader, const char *fragmentShader) override;

    // 设置模糊半径
    void setBlurSize(float blurSize);

protected:
    void onDrawBegin() override;

protected:
    int texelWidthOffsetHandle;
    int texelHeightOffsetHandle;
    float blurSize;
    const char *vertexShader;
    const char *fragmentShader;
};


#endif //GLGAUSSIANPASSBLURFILTER_H
