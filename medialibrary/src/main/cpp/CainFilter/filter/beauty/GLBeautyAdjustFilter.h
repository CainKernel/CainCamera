//
// Created by CainHuang on 2019/3/18.
//

#ifndef GLBEAUTYADJUSTFILTER_H
#define GLBEAUTYADJUSTFILTER_H

#include <filter/GLFilter.h>

/**
 * 磨皮调节滤镜，高反差保留法最后一步
 */
class GLBeautyAdjustFilter : public GLFilter {
public:
    GLBeautyAdjustFilter();

    virtual ~GLBeautyAdjustFilter();

    void initProgram() override;

    void initProgram(const char *vertexShader, const char *fragmentShader) override;

    void setBlurTexture(int blurTexture, int highPassBlurTexture);

protected:
    void onDrawBegin() override;

    int blurTextureHandle;          // 第一轮高斯模糊纹理句柄
    int highPassBlurTextureHandle;  // 第二轮高斯模糊纹理句柄
    int intensityHandle;            // 强度句柄

    int blurTexture;
    int highPassBlurTexture;
};


#endif //GLBEAUTYADJUSTFILTER_H
