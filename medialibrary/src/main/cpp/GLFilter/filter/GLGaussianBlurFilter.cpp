//
// Created by CainHuang on 2019/3/16.
//

#include "GLGaussianBlurFilter.h"
#include "GLGaussianPassBlurFilter.h"

GLGaussianBlurFilter::GLGaussianBlurFilter() {
    addFilter(new GLGaussianPassBlurFilter());
    addFilter(new GLGaussianPassBlurFilter());
}

GLGaussianBlurFilter::GLGaussianBlurFilter(const char *vertexShader, const char *fragmentShader) {
    addFilter(new GLGaussianPassBlurFilter(vertexShader, fragmentShader));
    addFilter(new GLGaussianPassBlurFilter(vertexShader, fragmentShader));
}

GLGaussianBlurFilter::~GLGaussianBlurFilter() {

}

void GLGaussianBlurFilter::setTextureSize(int width, int height) {
    GLGroupFilter::setTextureSize(width, height);
    filterList[0]->setTextureSize(width, 0);
    filterList[1]->setTextureSize(0, height);
}

void GLGaussianBlurFilter::setBlurSize(float blurSize) {
    ((GLGaussianPassBlurFilter *)filterList[0])->setBlurSize(blurSize);
    ((GLGaussianPassBlurFilter *)filterList[1])->setBlurSize(blurSize);
}


