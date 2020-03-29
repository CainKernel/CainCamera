//
// Created by CainHuang on 2019/3/18.
//

#include <base/OpenGLUtils.h>
#include "GLBeautyHighPassFilter.h"

const std::string kHighPassFragmentShader = SHADER_TO_STRING(
        precision mediump float;
        varying vec2 textureCoordinate;
        uniform sampler2D inputTexture; // 输入原图
        uniform sampler2D blurTexture;  // 高斯模糊图片
        const float intensity = 24.0;   // 强光程度
        void main() {
            lowp vec4 sourceColor = texture2D(inputTexture, textureCoordinate);
            lowp vec4 blurColor = texture2D(blurTexture, textureCoordinate);
            // 高通滤波之后的颜色值
            highp vec4 highPassColor = sourceColor - blurColor;
            // 对应混合模式中的强光模式(color = 2.0 * color1 * color2)，对于高反差的颜色来说，color1 和color2 是同一个
            highPassColor.r = clamp(2.0 * highPassColor.r * highPassColor.r * intensity, 0.0, 1.0);
            highPassColor.g = clamp(2.0 * highPassColor.g * highPassColor.g * intensity, 0.0, 1.0);
            highPassColor.b = clamp(2.0 * highPassColor.b * highPassColor.b * intensity, 0.0, 1.0);
            // 输出的是把痘印等过滤掉
            gl_FragColor = vec4(highPassColor.rgb, 1.0);
        }
        );

GLBeautyHighPassFilter::GLBeautyHighPassFilter() : blurTextureHandle(-1), blurTexture(-1) {

}

GLBeautyHighPassFilter::~GLBeautyHighPassFilter() {

}

void GLBeautyHighPassFilter::initProgram() {
    if (!isInitialized()) {
        initProgram(kDefaultVertexShader.c_str(), kHighPassFragmentShader.c_str());
    }
}

void GLBeautyHighPassFilter::initProgram(const char *vertexShader, const char *fragmentShader) {
    GLFilter::initProgram(vertexShader, fragmentShader);
    if (isInitialized()) {
        blurTextureHandle = glGetUniformLocation(programHandle, "blurTexture");
    }
}


void GLBeautyHighPassFilter::onDrawBegin() {
    GLFilter::onDrawBegin();
    if (blurTextureHandle >= 0) {
        OpenGLUtils::bindTexture(blurTextureHandle, blurTexture, 1);
    }
}

void GLBeautyHighPassFilter::setBlurTexture(int texture) {
    this->blurTexture = texture;
}

