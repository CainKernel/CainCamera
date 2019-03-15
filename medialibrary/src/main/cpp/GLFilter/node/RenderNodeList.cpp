//
// Created by CainHuang on 2019/3/14.
//

#include "RenderNodeList.h"

RenderNodeList::RenderNodeList() {
    head = nullptr;
    tail = nullptr;
    length = 0;
}

RenderNodeList::~RenderNodeList() {
    while (head != nullptr) {
        RenderNode *node = head->nextNode;
        head->destroy();
        delete head;
        head = node;
    }
}

int RenderNodeList::insertNode(RenderNode *node) {
    if (node->getNodeType() == NODE_NONE) {
        return -1;
    }
    // 如果结点头为空值，则表示此时为空链表，直接插入
    if (!head) {
        node->prevNode = nullptr;
        node->nextNode = head;
        head = node;
        tail = node;
    } else {
        // 查找后继结点
        RenderNode *tmp = head;
        for (int i = 0; i < length; ++i) {
            // 判断如果结点为空，或者是后继结点，直接终止查找
            if ((!tmp) || (tmp->getNodeType() > node->getNodeType())) {
                break;
            }
            tmp = tmp->nextNode;
        }

        // 判断找到后继结点，则将结点插入后继结点前面
        if (!tmp) {
            node->prevNode = tmp->prevNode;
            // 判断前继结点是否存在，绑定前继结点的后继结点
            if (tmp->prevNode) {
                tmp->prevNode->nextNode = node;
            }
            node->nextNode = tmp;
            tmp->prevNode = node;
        } else { // 如果后继结点不存在，则直接插到尾结点的后面
            node->prevNode = tail;
            node->nextNode = nullptr;
            tail->nextNode = node;
            tail = node;
        }
    }
    length++;
}

RenderNode *RenderNodeList::removeNode(RenderNodeType type) {
    RenderNode *node = findNode(type);
    // 查找到需要删除的渲染结点，从渲染链表中移除
    if (node) {
        // 将前驱结点指向结点的后继结点
        if (node->prevNode) {
            node->prevNode->nextNode = node->nextNode;
        }
        // 将后继结点指向结点的前驱结点
        if (node->nextNode) {
            node->nextNode->prevNode = node->prevNode;
        } else { // 如果不存在后继结点，则表示需要移除的结点是尾结点。需要更新尾结点的位置
            tail = node->prevNode;
        }
        // 断开需要删除的渲染结点的前继和后继结点链接
        node->prevNode = nullptr;
        node->nextNode = nullptr;
        length--;
    }
    return node;
}

RenderNode *RenderNodeList::findNode(RenderNodeType type) {
    RenderNode *node = head;
    while (node != nullptr && node->getNodeType() != type) {
        node = node->nextNode;
    }
    return node;
}

bool RenderNodeList::isEmpty() {
    return (length == 0);
}

int RenderNodeList::size() {
    return length;
}

void RenderNodeList::deleteAll() {
    RenderNode *node = head;
    while (node != nullptr) {
        head = node->nextNode;
        node->destroy();
        delete node;
        node = head;
    }
    length = 0;
    head = nullptr;
    tail = nullptr;
}
