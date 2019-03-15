//
// Created by CainHuang on 2019/3/15.
//

#include <cstring>
#include "InputRenderNode.h"

InputRenderNode::InputRenderNode() : RenderNode(NODE_INPUT) {
    resetVertices();
    resetTextureVertices();
}

InputRenderNode::~InputRenderNode() {

}

void InputRenderNode::initFilter(Texture *texture) {
    if (!glFilter) {
        if (texture) {
            if (texture->format == FMT_YUV420P) {
                glFilter = new GLInputYUV420PFilter();
            } else if (texture->format == FMT_ARGB) {
                glFilter = new GLInputABGRFilter();
            } else {
                glFilter = new GLInputFilter();
            }
            init();
        }
    }
    if (texture) {
        setTextureSize(texture->width, texture->height);
    }
}

bool InputRenderNode::uploadTexture(Texture *texture) {
    if (glFilter && glFilter->isInitialized()) {
        return ((GLInputFilter *) glFilter)->uploadTexture(texture);
    }
    return true;
}

bool InputRenderNode::drawFrame(Texture *texture) {
    cropTexVertices(texture);
    if (glFilter != nullptr) {
        return ((GLInputFilter *) glFilter)->renderTexture(texture, vertices, textureVetrices);
    }
    return false;
}

int InputRenderNode::drawFrameBuffer(Texture *texture) {
    // FrameBuffer 没有 或者是 滤镜还没初始化，则直接返回输入的纹理
    if (!frameBuffer || !frameBuffer->isInitialized() || !glFilter || !glFilter->isInitialized()) {
        return -1;
    }

    frameBuffer->bindBuffer();
    cropTexVertices(texture);
    ((GLInputFilter *) glFilter)->renderTexture(texture, vertices, textureVetrices);
    frameBuffer->unbindBuffer();

    return frameBuffer->getTexture();
}

bool InputRenderNode::drawFrame(GLuint texture, float *vertices, float *textureVertices) {
    return RenderNode::drawFrame(texture, vertices, textureVertices);
}

int InputRenderNode::drawFrameBuffer(GLuint texture, float *vertices, float *textureVertices) {
    return RenderNode::drawFrameBuffer(texture, vertices, textureVertices);
}

void InputRenderNode::resetVertices() {
    vertices[0] = -1.0f;
    vertices[1] = -1.0f;
    vertices[2] =  1.0f;
    vertices[3] = -1.0f;
    vertices[4] = -1.0f;
    vertices[5] =  1.0f;
    vertices[6] =  1.0f;
    vertices[7] =  1.0f;
}

void InputRenderNode::resetTextureVertices() {
    textureVetrices[0] = 0.0f;
    textureVetrices[1] = 1.0f;
    textureVetrices[2] = 1.0f;
    textureVetrices[3] = 1.0f;
    textureVetrices[4] = 0.0f;
    textureVetrices[5] = 0.0f;
    textureVetrices[6] = 1.0f;
    textureVetrices[7] = 0.0f;
}

void InputRenderNode::cropTexVertices(Texture *texture) {
    // 帧宽度和linesize宽度不一致，需要裁掉多余的地方，否则会出现绿屏的情况
    if (texture && texture->frameWidth != texture->width) {
        GLsizei padding = texture->width - texture->frameWidth;
        GLfloat normalized = ((GLfloat)padding + 0.5f) / (GLfloat)texture->width;
        textureVetrices[0] = 0.0f;
        textureVetrices[1] = 1.0f;
        textureVetrices[2] = 1.0f - normalized;
        textureVetrices[3] = 1.0f;
        textureVetrices[4] = 0.0f;
        textureVetrices[5] = 0.0f;
        textureVetrices[6] = 1.0f - normalized;
        textureVetrices[7] = 0.0f;
    } else {
        resetTextureVertices();
    }
}
