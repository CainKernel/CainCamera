//
// Created by CainHuang on 2019/3/22.
//

#ifndef GLDEPTHFILTER_H
#define GLDEPTHFILTER_H

#include "GLGroupFilter.h"

/**
 * 景深特效
 */
class GLDepthFilter : public GLGroupFilter {
public:
    GLDepthFilter();

    void drawTexture(GLuint texture, const float *vertices, const float *textureVertices,
                     bool viewPortUpdate) override;

    void drawTexture(FrameBuffer *frameBuffer, GLuint texture, const float *vertices,
                     const float *textureVertices) override;
};


#endif //GLDEPTHFILTER_H
