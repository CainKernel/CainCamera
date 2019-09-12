//
// Created by CainHuang on 2019/3/17.
//

#include <algorithm>
#include <AndroidLog.h>
#include "GLGroupFilter.h"

GLGroupFilter::GLGroupFilter() : fboSizeScale(1.0) {

}

GLGroupFilter::~GLGroupFilter() {
    removeAllFilters();
}

void GLGroupFilter::addFilter(GLFilter *filter) {
    if (containFilter(filter)) {
        return;
    }
    filterList.push_back(filter);
}

bool GLGroupFilter::containFilter(GLFilter *filter) const {
    std::vector<GLFilter *>::const_iterator iterator = std::find(filterList.begin(), filterList.end(), filter);
    if (iterator != filterList.end()) {
        return true;
    }
    return false;
}

void GLGroupFilter::removeAllFilters() {
    for (auto const& filter : filterList) {
        filter->destroyProgram();
        delete filter;
    }
    filterList.clear();
    for (auto const& frameBuffer : frameBufferList) {
        frameBuffer->destroy();
        delete frameBuffer;
    }
    frameBufferList.clear();
}

void GLGroupFilter::initProgram() {
    bool inited = true;
    for (auto item = filterList.cbegin(); item != filterList.cend(); item++) {
        (*item)->initProgram();
        if (!(*item)->isInitialized()) {
            inited = false;
            break;
        }
    }
    setInitialized(inited);
}

void GLGroupFilter::initProgram(const char *vertexShader, const char *fragmentShader) {
    for (auto item = filterList.cbegin(); item != filterList.cend(); item++) {
        (*item)->initProgram(vertexShader, fragmentShader);
    }
}

void GLGroupFilter::destroyProgram() {
    for (auto item = filterList.cbegin(); item != filterList.cend(); item++) {
        (*item)->destroyProgram();
    }
}

void GLGroupFilter::drawTexture(GLuint texture, const float *vertices, const float *textureVertices,
                                bool viewPortUpdate) {
    if (frameBufferList.size() < filterList.size()-1) {
        return;
    }
    GLuint currentTexture = texture;
    for (int i = 0; i < filterList.size(); ++i) {
        // 最后一个纹理直接绘制
        if (i == filterList.size()-1) {
            if (viewPortUpdate) {
                updateViewPort();
            }
            filterList[i]->drawTexture(currentTexture, vertices, textureVertices);
        } else {
            filterList[i]->drawTexture(frameBufferList[i], currentTexture, vertices, textureVertices);
            currentTexture = frameBufferList[i]->getTexture();
        }
    }
}

void GLGroupFilter::drawTexture(FrameBuffer *frameBuffer, GLuint texture, const float *vertices,
                                const float *textureVertices) {
    if (frameBufferList.size() < filterList.size()-1) {
        return;
    }
    GLuint currentTexture = texture;
    for (int i = 0; i < filterList.size(); ++i) {
        // 最后一个纹理绘制到输出的FBO中
        if (i == filterList.size()-1) {
            filterList[i]->drawTexture(frameBuffer, currentTexture, vertices, textureVertices);
        } else {
            filterList[i]->drawTexture(frameBufferList[i], currentTexture, vertices, textureVertices);
            currentTexture = frameBufferList[i]->getTexture();
        }
    }
}

void GLGroupFilter::setFrameBufferScale(float scale) {
    this->fboSizeScale = scale;
}

void GLGroupFilter::setTextureSize(int width, int height) {
    GLFilter::setTextureSize(width, height);
    for (auto item = filterList.cbegin(); item != filterList.cend(); item++) {
        (*item)->setTextureSize((int)(width * fboSizeScale), (int)(height * fboSizeScale));
    }
    if (frameBufferList.size() < filterList.size()-1) {
        int size = filterList.size()-1 - frameBufferList.size();
        for (int i = 0; i < size; ++i) {
            FrameBuffer *frameBuffer = new FrameBuffer((int)(width * fboSizeScale), (int)(height * fboSizeScale));
            frameBuffer->init();
            frameBufferList.push_back(frameBuffer);
        }
    }
}


void GLGroupFilter::setDisplaySize(int width, int height) {
    GLFilter::setDisplaySize(width, height);
    for (auto item = filterList.cbegin(); item != filterList.cend(); item++) {
        (*item)->setDisplaySize(width, height);
    }
}

void GLGroupFilter::setTimeStamp(double timeStamp) {
    for (auto item = filterList.cbegin(); item != filterList.cend(); item++) {
        (*item)->setTimeStamp(timeStamp);
    }
}

void GLGroupFilter::setIntensity(float intensity) {
    for (auto item = filterList.cbegin(); item != filterList.cend(); item++) {
        (*item)->setIntensity(intensity);
    }
}

