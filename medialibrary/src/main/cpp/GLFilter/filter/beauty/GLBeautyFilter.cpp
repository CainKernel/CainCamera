//
// Created by CainHuang on 2019/3/18.
//

#include "GLBeautyFilter.h"
#include "GLBeautyBlurFilter.h"
#include "GLBeautyHighPassFilter.h"
#include "GLBeautyAdjustFilter.h"

GLBeautyFilter::GLBeautyFilter() {
    addFilter(new GLBeautyBlurFilter());
    addFilter(new GLBeautyHighPassFilter());
    addFilter(new GLGaussianBlurFilter());
    addFilter(new GLBeautyAdjustFilter());
    setFrameBufferScale(0.5);
}

GLBeautyFilter::~GLBeautyFilter() {

}

void GLBeautyFilter::drawTexture(GLuint texture, float *vertices, float *textureVertices, bool viewPortUpdate) {
    if (filterList.size() != 4 || frameBufferList.size() != 3) {
        return;
    }
    // 第一步 对原图进行高斯模糊
    filterList[0]->drawTexture(frameBufferList[0], texture, vertices, textureVertices);

    // 第二步 与原图高反差滤镜
    ((GLBeautyHighPassFilter *)filterList[1])->setBlurTexture(frameBufferList[0]->getTexture());
    filterList[1]->drawTexture(frameBufferList[1], texture, vertices, textureVertices);

    // 第三步 第二轮对高反差纹理做高斯模糊
    filterList[2]->drawTexture(frameBufferList[2], frameBufferList[1]->getTexture(),
            vertices, textureVertices);

    // 第四步 磨皮调节合成滤镜
    ((GLBeautyAdjustFilter *)filterList[3])->setBlurTexture(frameBufferList[0]->getTexture(),
            frameBufferList[2]->getTexture());
    filterList[3]->drawTexture(texture, vertices, textureVertices, viewPortUpdate);
}

void GLBeautyFilter::drawTexture(FrameBuffer *frameBuffer, GLuint texture, float *vertices,
                                 float *textureVertices) {
    if (filterList.size() != 4 || frameBufferList.size() < filterList.size()-1) {
        return;
    }
    // 第一步 对原图进行高斯模糊
    filterList[0]->drawTexture(frameBufferList[0], texture, vertices, textureVertices);

    // 第二步 与原图高反差滤镜
    ((GLBeautyHighPassFilter *)filterList[1])->setBlurTexture(frameBufferList[0]->getTexture());
    filterList[1]->drawTexture(frameBufferList[1], texture, vertices, textureVertices);

    // 第三步 第二轮对高反差纹理做高斯模糊
    filterList[2]->drawTexture(frameBufferList[2], frameBufferList[1]->getTexture(),
                               vertices, textureVertices);

    // 第四步 磨皮调节合成滤镜
    ((GLBeautyAdjustFilter *)filterList[3])->setBlurTexture(frameBufferList[0]->getTexture(),
                                                            frameBufferList[2]->getTexture());
    filterList[3]->drawTexture(frameBuffer, texture, vertices, textureVertices);
}
