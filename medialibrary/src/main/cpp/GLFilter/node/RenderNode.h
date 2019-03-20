//
// Created by CainHuang on 2019/3/13.
//

#ifndef RENDERNODE_H
#define RENDERNODE_H

#include <string>

#include "Filter.h"
#include "FrameBuffer.h"

#include "NodeType.h"

/**
 * 渲染结点
 * 数据流如下：
 *   input->render node 1->render node 2->render node 3->...->render node n->output
 * texture->frameBuffer 1->frameBuffer 2->frameBuffer 3->...->frameBuffer n->texture
 *        ->glFilter     ->glFilter     ->glFilter     ->...->glFilter     ->glFilter
 *
 * 每个渲染结点上的glFilter均可以随时改变，而FrameBuffer则不需要跟随glFilter一起销毁重建，节省开销
 */
class RenderNode {

public:
    RenderNode(RenderNodeType type);

    virtual ~RenderNode();

    // 初始化
    void init();

    // 销毁结点
    void destroy();

    // 设置纹理大小
    void setTextureSize(int width, int height);

    // 设置显示大小
    void setDisplaySize(int width, int height);

    // 设置FrameBuffer
    void setFrameBuffer(FrameBuffer *buffer);

    // 切换Filter
    void changeFilter(GLFilter *filter);

    // 设置时间戳
    void setTimeStamp(double timeStamp);

    // 设置强度
    void setIntensity(float intensity);

    // 直接绘制输出
    virtual bool drawFrame(GLuint texture, float *vertices, float *textureVertices);

    // 绘制到FBO
    virtual int drawFrameBuffer(GLuint texture, float *vertices, float *textureVertices);

    RenderNodeType getNodeType() const;

    bool hasFrameBuffer() const;
public:
    // 前继结点
    RenderNode *prevNode;
    // 下一个渲染结点
    RenderNode *nextNode;
    // 渲染结点的类型
    RenderNodeType nodeType;

protected:
    // 纹理宽高
    int textureWidth, textureHeight;
    // 显示宽高
    int displayWidth, displayHeight;
    // 滤镜
    GLFilter *glFilter;
    // FrameBuffer 对象
    FrameBuffer *frameBuffer;
};

#endif //RENDERNODE_H
