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
    int insertNode(RenderNode *node);

    // 移除某个结点，并返回该结点的对象
    RenderNode *removeNode(RenderNodeType type);

    // 查找渲染结点
    RenderNode *findNode(RenderNodeType type);

    // 判断是否为空
    bool isEmpty();

    // 链表长度
    int size();

    // 删除全部结点
    void deleteAll();

private:
    RenderNode *head;
    RenderNode *tail;
    int length;
};

#endif //RENDERNODELIST_H
