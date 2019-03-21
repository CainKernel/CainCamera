//
// Created by CainHuang on 2019/3/21.
//

#ifndef GLOESINPUTFILTER_H
#define GLOESINPUTFILTER_H


#include <filter/GLFilter.h>
#include <Filter.h>

/**
 * OES纹理输入滤镜
 */
class GLOESInputFilter : public GLFilter {
public:
    GLOESInputFilter();

    void initProgram() override;

    void initProgram(const char *vertexShader, const char *fragmentShader) override;

    void updateTransformMatrix(const float *matrix);

protected:
    void bindAttributes(const float *vertices, const float *textureVertices) override;

    GLenum getTextureType() override;

    int transformMatrixHandle;
    Matrix4 transformMatrix;
};


#endif //GLOESINPUTFILTER_H
