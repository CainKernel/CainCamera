//
// Created by CainHuang on 2019/3/13.
//

#include "RenderNode.h"

RenderNode::RenderNode(RenderNodeType type)
           : prevNode(nullptr), nextNode(nullptr), glFilter(nullptr), frameBuffer(nullptr),
           nodeType(type), textureWidth(-1), textureHeight(-1), displayWidth(-1), displayHeight(-1) {

}

RenderNode::~RenderNode() {

}

void RenderNode::init() {
    if (glFilter != nullptr && !glFilter->isInitialized()) {
        glFilter->initProgram();
    }
}

void RenderNode::destroy() {
    // 释放filter的shader program
    if (glFilter != nullptr) {
        glFilter->destroyProgram();
    }
    // 将FrameBuffer放入管理器中
    if (frameBuffer != nullptr) {
        frameBuffer->destroy();
        delete frameBuffer;
        frameBuffer = nullptr;
    }
}

void RenderNode::setTextureSize(int width, int height) {
    textureWidth = width;
    textureHeight = height;
}

void RenderNode::setDisplaySize(int width, int height) {
    displayWidth = width;
    displayHeight = height;
}

void RenderNode::setFrameBuffer(FrameBuffer *buffer) {
    // 将旧的FBO放入管理器中
    if (this->frameBuffer != nullptr) {
        frameBuffer->destroy();
        delete frameBuffer;
    }
    this->frameBuffer = buffer;
}

void RenderNode::changeFilter(GLFilter *filter) {
    if (this->glFilter != nullptr) {
        this->glFilter->destroyProgram();
        delete this->glFilter;
    }
    this->glFilter = filter;
}

void RenderNode::setTimeStamp(double timeStamp) {
    if (glFilter != nullptr) {
        glFilter->setTimeStamp(timeStamp);
    }
}

void RenderNode::setIntensity(float intensity) {
    if (glFilter != nullptr) {
        glFilter->setIntensity(intensity);
    }
}

bool RenderNode::drawFrame(GLuint texture, float *vertices, float *textureVertices) {
    if (!glFilter || !glFilter->isInitialized()) {
        return false;
    }
    if (displayWidth != 0 && displayHeight != 0) {
        glViewport(0, 0, displayWidth, displayHeight);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);
    }
    glFilter->drawTexture(texture, vertices, textureVertices);
    return true;
}

int RenderNode::drawFrameBuffer(GLuint texture, float *vertices, float *textureVertices) {

    // FrameBuffer 没有 或者是 滤镜还没初始化，则直接返回输入的纹理
    if (!frameBuffer || !frameBuffer->isInitialized() || !glFilter || !glFilter->isInitialized()) {
        return texture;
    }

    frameBuffer->bindBuffer();
    glFilter->drawTexture(texture, vertices, textureVertices);
    frameBuffer->unbindBuffer();

    return frameBuffer->getTexture();
}

RenderNodeType RenderNode::getNodeType() const {
    return nodeType;
}
