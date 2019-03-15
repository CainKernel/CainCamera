//
// Created by CainHuang on 2019/3/14.
//

#ifndef NODETYPE_H
#define NODETYPE_H

/**
 * 渲染结点类型
 */
typedef enum RenderNodeType {
    NODE_NONE = -1,     // 未知结点
    NODE_INPUT = 0,     // 输入结点
    NODE_BEAUTY = 1,    // 美颜结点
    NODE_FACE = 2,      // 美型结点
    NODE_MAKEUP = 3,    // 彩妆结点
    NODE_FILTER = 4,    // 滤镜结点
    NODE_EFFECT = 5,    // 特效结点
    NODE_STICKERS = 6,  // 贴纸结点
    NODE_DISPLAY = 7,   // 显示结点
} RenderNodeType;

#endif //NODETYPE_H
