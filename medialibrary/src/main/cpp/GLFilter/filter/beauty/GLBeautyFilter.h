//
// Created by CainHuang on 2019/3/18.
//

#ifndef GLBEAUTYFILTER_H
#define GLBEAUTYFILTER_H


#include <filter/GLGroupFilter.h>

class GLBeautyFilter : public GLGroupFilter {
public:
    GLBeautyFilter();

    virtual ~GLBeautyFilter();

    void drawTexture(GLuint texture, float *vertices, float *textureVertices, bool viewPortUpdate = false) override;

    void drawTexture(FrameBuffer *frameBuffer, GLuint texture, float *vertices,
                     float *textureVertices) override;
};


#endif //GLBEAUTYFILTER_H
