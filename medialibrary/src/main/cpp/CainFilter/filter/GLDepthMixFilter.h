//
// Created by CainHuang on 2019/3/22.
//

#ifndef GLDEPTHMIXFILTER_H
#define GLDEPTHMIXFILTER_H

#include <Filter.h>
#include "GLIntensityFilter.h"

/**
 * 景深混合滤镜
 */
class GLDepthMixFilter : public GLIntensityFilter {
public:
    GLDepthMixFilter();

    void initProgram() override;

    void initProgram(const char *vertexShader, const char *fragmentShader) override;

    void setBlurTexture(int texture);

protected:
    void bindTexture(GLuint texture) override;

    void onDrawBegin() override;

private:
    int blurImageHandle;
    int innerHandle;
    int outerHandle;
    int widthHandle;
    int heightHandle;
    int centerHandle;
    int line1Handle;
    int line2Handle;

    int blurTexture;
    float inner;
    float outer;
    Vector2 center;
    Vector3 line1;
    Vector3 line2;
};


#endif //GLDEPTHMIXFILTER_H
