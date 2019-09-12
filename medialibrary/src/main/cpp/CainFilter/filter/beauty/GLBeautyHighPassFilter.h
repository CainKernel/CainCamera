//
// Created by CainHuang on 2019/3/18.
//

#ifndef GLBEAUTYHIGHPASSFILTER_H
#define GLBEAUTYHIGHPASSFILTER_H

#include "filter/GLFilter.h"

/**
 * 美颜专用高反差滤镜
 */
class GLBeautyHighPassFilter : public GLFilter {
public:
    GLBeautyHighPassFilter();

    virtual ~GLBeautyHighPassFilter();

    void initProgram() override;

    void initProgram(const char *vertexShader, const char *fragmentShader) override;

protected:
    void onDrawBegin() override;

public:

    // 设置经过高斯模糊处理的纹理
    void setBlurTexture(int texture);

private:
    int blurTextureHandle;
    int blurTexture;
};


#endif //GLBEAUTYHIGHPASSFILTER_H
