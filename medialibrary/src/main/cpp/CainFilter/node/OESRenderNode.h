//
// Created by CainHuang on 2019/3/21.
//

#ifndef OESRENDERNODE_H
#define OESRENDERNODE_H


#include "RenderNode.h"

/**
 * OES纹理输入
 */
class OESRenderNode : public RenderNode {
public:
    OESRenderNode();

    virtual ~OESRenderNode();

    void updateTransformMatrix(const float *matrix);
};


#endif //OESRENDERNODE_H
