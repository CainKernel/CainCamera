//
// Created by CainHuang on 2019/3/21.
//

#include "OESRenderNode.h"

OESRenderNode::OESRenderNode() : RenderNode(NODE_INPUT) {
    glFilter = new GLOESInputFilter();
}

OESRenderNode::~OESRenderNode() {

}

void OESRenderNode::updateTransformMatrix(const float *matrix) {
    if (glFilter) {
        ((GLOESInputFilter *) glFilter)->updateTransformMatrix(matrix);
    }
}
