//
// Created by CainHuang on 2019/3/13.
//

#ifndef GLINPUTBGRAFILTER_H
#define GLINPUTBGRAFILTER_H

#include "GLInputFilter.h"

/**
 * BGRA输入滤镜
 */
class GLInputABGRFilter : public GLInputFilter {
public:
    GLInputABGRFilter();

    virtual ~GLInputABGRFilter();

    void initProgram() override;

    void initProgram(const char *vertexShader, const char *fragmentShader) override;

    GLboolean renderTexture(Texture *texture, float *vertices, float *textureVertices) override;

    GLboolean uploadTexture(Texture *texture) override;
};


#endif //GLINPUTBGRAFILTER_H
