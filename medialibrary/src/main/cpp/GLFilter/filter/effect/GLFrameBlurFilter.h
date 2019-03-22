//
// Created by CainHuang on 2019/3/22.
//

#ifndef GLFRAMEBLURFILTER_H
#define GLFRAMEBLURFILTER_H

#include <filter/GLGroupFilter.h>

/**
 * 模糊分屏特效
 */
class GLFrameBlurFilter : public GLGroupFilter {

public:
    GLFrameBlurFilter();

    void drawTexture(GLuint texture, const float *vertices, const float *textureVertices,
                     bool viewPortUpdate) override;

    void drawTexture(FrameBuffer *frameBuffer, GLuint texture, const float *vertices,
                     const float *textureVertices) override;
};


#endif //GLFRAMEBLURFILTER_H
