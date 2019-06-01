//
// Created by CainHuang on 2019/3/15.
//

#ifndef INPUTRENDERNODE_H
#define INPUTRENDERNODE_H

#include "RenderNode.h"

/**
 * 输入渲染结点
 */
class InputRenderNode : public RenderNode {
public:
    InputRenderNode();

    virtual ~InputRenderNode();

    // 初始化滤镜
    void initFilter(Texture *texture);

    // 上载纹理
    bool uploadTexture(Texture *texture);

    // 直接绘制纹理
    bool drawFrame(Texture *texture);

    // 将纹理绘制到FBO
    int drawFrameBuffer(Texture *texture);

private:
    bool drawFrame(GLuint texture, const float *vertices, const float *textureVertices) override;

    int drawFrameBuffer(GLuint texture, const float *vertices, const float *textureVertices) override;

    void resetVertices();

    void resetTextureVertices(Texture *texture);

    RotationMode getRotateMode(Texture *texture);

    void cropTexVertices(Texture *texture);

private:
    GLfloat vertices[8];                    // 顶点坐标
    GLfloat textureVertices[8];                 // 纹理坐标
};


#endif //INPUTRENDERNODE_H
