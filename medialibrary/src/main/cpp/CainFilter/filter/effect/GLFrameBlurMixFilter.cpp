//
// Created by CainHuang on 2019/3/22.
//

#include <base/OpenGLUtils.h>
#include "GLFrameBlurMixFilter.h"

const std::string kFrameBlurMixFragmentShader = SHADER_TO_STRING(

        precision mediump float;
        uniform sampler2D inputTexture; // 原始图像
        uniform sampler2D blurTexture;  // 经过高斯模糊的图像
        varying vec2 textureCoordinate;

        uniform float blurOffsetY;  // y轴边框模糊偏移值
        uniform float scale;        // 模糊部分的缩放倍数

        void main() {
            // uv坐标
            vec2 uv = textureCoordinate.xy;
            vec4 color;
            // 中间为原图部分
            if (uv.y >= blurOffsetY && uv.y <= 1.0 - blurOffsetY) {
                color = texture2D(inputTexture, uv);
            } else { // 边框部分使用高斯模糊的图像
                vec2 center = vec2(0.5, 0.5);
                uv -= center;
                uv = uv / scale;
                uv += center;
                color = texture2D(blurTexture, uv);
            }
            gl_FragColor = color;
        }

        );

GLFrameBlurMixFilter::GLFrameBlurMixFilter() : blurTexture(-1), blurOffsetY(0.33f), scale(1.2f) {

}

void GLFrameBlurMixFilter::initProgram() {
    initProgram(kDefaultVertexShader.c_str(), kFrameBlurMixFragmentShader.c_str());
}

void GLFrameBlurMixFilter::initProgram(const char *vertexShader, const char *fragmentShader) {
    GLFilter::initProgram(vertexShader, fragmentShader);
    if (isInitialized()) {
        blurTextureHandle = glGetUniformLocation(programHandle, "blurTexture");
        blurOffsetYHandle = glGetUniformLocation(programHandle, "blurOffsetY");
        scaleHandle = glGetUniformLocation(programHandle, "scale");
    }
}

void GLFrameBlurMixFilter::setBlurTexture(int blurTexture) {
    this->blurTexture = blurTexture;
}

void GLFrameBlurMixFilter::bindTexture(GLuint texture) {
    GLFilter::bindTexture(texture);
    if (isInitialized()) {
        OpenGLUtils::bindTexture(blurTextureHandle, blurTexture, 1);
    }
}

void GLFrameBlurMixFilter::onDrawBegin() {
    GLFilter::onDrawBegin();
    if (isInitialized()) {
        glUniform1f(blurOffsetYHandle, blurOffsetY);
        glUniform1f(scaleHandle, scale);
    }
}



