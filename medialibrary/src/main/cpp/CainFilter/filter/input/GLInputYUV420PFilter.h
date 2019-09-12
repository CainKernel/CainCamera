//
// Created by CainHuang on 2019/3/13.
//

#ifndef GLINPUTYUV420PFILTER_H
#define GLINPUTYUV420PFILTER_H

#include "GLInputFilter.h"

/**
 * YUV420P输入滤镜
 */
class GLInputYUV420PFilter : public GLInputFilter {
public:
    GLInputYUV420PFilter();

    virtual ~GLInputYUV420PFilter();

    void initProgram() override;

    void initProgram(const char *vertexShader, const char *fragmentShader) override;

    GLboolean renderTexture(Texture *texture, float *vertices, float *textureVertices) override;

    GLboolean uploadTexture(Texture *texture) override;
};


#endif //GLINPUTYUV420PFILTER_H
