//
// Created by CainHuang on 2019/3/15.
//

#include <cstring>
#include "InputRenderNode.h"

InputRenderNode::InputRenderNode() : RenderNode(NODE_INPUT) {
    resetVertices();
    resetTextureVertices(NULL);
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
        return ((GLInputFilter *) glFilter)->renderTexture(texture, vertices, textureVertices);
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
    ((GLInputFilter *) glFilter)->renderTexture(texture, vertices, textureVertices);
    frameBuffer->unbindBuffer();

    return frameBuffer->getTexture();
}

bool InputRenderNode::drawFrame(GLuint texture, const float *vertices, const float *textureVertices) {
    return RenderNode::drawFrame(texture, vertices, textureVertices);
}

int InputRenderNode::drawFrameBuffer(GLuint texture, const float *vertices, const float *textureVertices) {
    return RenderNode::drawFrameBuffer(texture, vertices, textureVertices);
}

void InputRenderNode::resetVertices() {
    const float *verticesCoord = CoordinateUtils::getVertexCoordinates();
    for (int i = 0; i < 8; ++i) {
        vertices[i] = verticesCoord[i];
    }
}

void InputRenderNode::resetTextureVertices(Texture *texture) {
    const float *vertices = CoordinateUtils::getInputTextureCoordinates(getRotateMode(texture));
    for (int i = 0; i < 8; ++i) {
        textureVertices[i] = vertices[i];
    }
}

RotationMode InputRenderNode::getRotateMode(Texture *texture) {
    if (texture == nullptr) {
        return ROTATE_NONE;
    }
    RotationMode mode = ROTATE_NONE;
    if (texture->rotate == 90) {
        mode = ROTATE_90;
    } else if (texture->rotate == 180) {
        mode = ROTATE_180;
    } else if (texture->rotate == 270) {
        mode = ROTATE_270;
    }
    return mode;
}

void InputRenderNode::cropTexVertices(Texture *texture) {
    // 帧宽度和linesize宽度不一致，需要裁掉多余的地方，否则会出现绿屏的情况
    if (texture && texture->frameWidth != texture->width) {
        GLsizei padding = texture->width - texture->frameWidth;
        GLfloat normalized = ((GLfloat)padding + 0.5f) / (GLfloat)texture->width;
        const float *vertices = CoordinateUtils::getInputTextureCoordinates(getRotateMode(texture));
        textureVertices[0] = vertices[0];
        textureVertices[1] = vertices[1];
        textureVertices[2] = vertices[2] - normalized;
        textureVertices[3] = vertices[3];
        textureVertices[4] = vertices[4];
        textureVertices[5] = vertices[5];
        textureVertices[6] = vertices[6] - normalized;
        textureVertices[7] = vertices[7];
    } else {
        resetTextureVertices(texture);
    }
}
