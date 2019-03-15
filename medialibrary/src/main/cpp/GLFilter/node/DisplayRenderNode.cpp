//
// Created by CainHuang on 2019/3/15.
//

#include "DisplayRenderNode.h"

DisplayRenderNode::DisplayRenderNode() : RenderNode(NODE_DISPLAY) {
    glFilter = new GLFilter();
}
