//
// Created by CainHuang on 2019/3/22.
//

#include <filter/GLGaussianBlurFilter.h>
#include "GLFrameBlurFilter.h"
#include "GLFrameBlurMixFilter.h"

GLFrameBlurFilter::GLFrameBlurFilter() {
    GLGaussianBlurFilter *filter = new GLGaussianBlurFilter();
    filter->setFrameBufferScale(0.5f);
    addFilter(filter);
    addFilter(new GLFrameBlurMixFilter());
}

void
GLFrameBlurFilter::drawTexture(GLuint texture, const float *vertices, const float *textureVertices,
                               bool viewPortUpdate) {
    if (frameBufferList.size() < filterList.size()-1) {
        return;
    }

    filterList[0]->drawTexture(frameBufferList[0], texture, vertices, textureVertices);
    ((GLFrameBlurMixFilter *)filterList[1])->setBlurTexture(frameBufferList[0]->getTexture());
    filterList[1]->drawTexture(texture, vertices, textureVertices, viewPortUpdate);
}

void GLFrameBlurFilter::drawTexture(FrameBuffer *frameBuffer, GLuint texture, const float *vertices,
                                    const float *textureVertices) {
    if (frameBufferList.size() < filterList.size()-1) {
        return;
    }

    filterList[0]->drawTexture(frameBufferList[0], texture, vertices, textureVertices);
    ((GLFrameBlurMixFilter *)filterList[1])->setBlurTexture(frameBufferList[0]->getTexture());
    filterList[1]->drawTexture(frameBuffer, texture, vertices, textureVertices);
}
