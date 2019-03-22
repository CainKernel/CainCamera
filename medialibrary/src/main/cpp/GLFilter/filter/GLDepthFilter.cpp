//
// Created by CainHuang on 2019/3/22.
//

#include "GLDepthFilter.h"
#include "GLGaussianBlurFilter.h"
#include "GLDepthMixFilter.h"

GLDepthFilter::GLDepthFilter() {

    GLGaussianBlurFilter *filter = new GLGaussianBlurFilter();
    filter->setFrameBufferScale(0.5f);
    addFilter(filter);
    addFilter(new GLDepthMixFilter());
}

void GLDepthFilter::drawTexture(GLuint texture, const float *vertices, const float *textureVertices,
                                bool viewPortUpdate) {
    if (frameBufferList.size() < filterList.size()-1) {
        return;
    }

    filterList[0]->drawTexture(frameBufferList[0], texture, vertices, textureVertices);
    ((GLDepthMixFilter *)filterList[1])->setBlurTexture(frameBufferList[0]->getTexture());
    filterList[1]->drawTexture(texture, vertices, textureVertices, viewPortUpdate);
}

void GLDepthFilter::drawTexture(FrameBuffer *frameBuffer, GLuint texture, const float *vertices,
                                const float *textureVertices) {
    if (frameBufferList.size() < filterList.size()-1) {
        return;
    }
    filterList[0]->drawTexture(frameBufferList[0], texture, vertices, textureVertices);
    ((GLDepthMixFilter *)filterList[1])->setBlurTexture(frameBufferList[0]->getTexture());
    filterList[1]->drawTexture(frameBuffer, texture, vertices, textureVertices);
}
