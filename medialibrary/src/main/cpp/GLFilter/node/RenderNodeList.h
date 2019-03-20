//
// Created by CainHuang on 2019/3/14.
//

#ifndef RENDERNODELIST_H
#define RENDERNODELIST_H

#include "RenderNode.h"
#include <list>

/**
 * 渲染结点链表，根据RenderNodeType类型排序
 * nullptr<-RenderNode<=>RenderNode<=>...<=>RenderNode<=>nullptr
 * nullptr<-head<=>...=>tail->nullptr
 */
class RenderNodeList {
public:
    RenderNodeList();

    virtual ~RenderNodeList();

    // 插入结点
    bool addNode(RenderNode *node);

    // 移除某个结点，并返回该结点的对象
    RenderNode *removeNode(RenderNodeType type);

    // 查找渲染结点
    RenderNode *findNode(RenderNodeType type);

    // 初始化
    void init();

    // 设置纹理大小
    void setTextureSize(int width, int height);

    // 设置显示大小
    void setDisplaySize(int width, int height);

    // 设置时间戳
    void setTimeStamp(double timeStamp);

    // 设置所有强度
    void setIntensity(float intensity);

    // 设置某个渲染结点中滤镜的强度
    void setIntensity(RenderNodeType type, float intensity);

    // 切换滤镜，还有切换那种只切换lut滤镜的那种估计需要构建一个对象来存放处理规则，这里先这么处理吧。
    void changeFilter(RenderNodeType type, GLFilter *filter);

    // 根据滤镜名称切换滤镜
    void changeFilter(RenderNodeType type, const char *name);

    // 根据滤镜ID切换滤镜
    void changeFilter(RenderNodeType type, const int id);

    // 绘制纹理并显示
    bool drawFrame(GLuint texture, float *vertices, float *textureVertices);

    // 绘制纹理到FBO
    int drawFrameBuffer(GLuint texture, float *vertices, float *textureVertices);

    // 判断是否为空
    bool isEmpty();

    // 链表长度
    int size();

    // 删除全部结点
    void deleteAll();

private:
    void dump();

    RenderNode *head;
    RenderNode *tail;
    int length;
};

#endif //RENDERNODELIST_H
