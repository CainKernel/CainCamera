//
// Created by CainHuang on 2019/3/18.
//

#include <common/OpenGLUtils.h>
#include "GLBeautyAdjustFilter.h"

const std::string kBeautyAdjustFragmentShader = SHADER_TO_STRING(
        precision mediump float;
        varying vec2 textureCoordinate;
        uniform sampler2D inputTexture;         // 输入原图
        uniform sampler2D blurTexture;          // 原图的高斯模糊纹理
        uniform sampler2D highPassBlurTexture;  // 高反差保留的高斯模糊纹理
        uniform lowp float intensity;           // 磨皮程度
        void main() {
            lowp vec4 sourceColor = texture2D(inputTexture, textureCoordinate);
            lowp vec4 blurColor = texture2D(blurTexture, textureCoordinate);
            lowp vec4 highPassBlurColor = texture2D(highPassBlurTexture, textureCoordinate);
            // 调节蓝色通道值
            mediump float value = clamp((min(sourceColor.b, blurColor.b) - 0.2) * 5.0, 0.0, 1.0);
            // 找到模糊之后RGB通道的最大值
            mediump float maxChannelColor = max(max(highPassBlurColor.r, highPassBlurColor.g), highPassBlurColor.b);
            // 计算当前的强度
            mediump float currentIntensity = (1.0 - maxChannelColor / (maxChannelColor + 0.2)) * value * intensity;
            // 混合输出结果
            lowp vec3 resultColor = mix(sourceColor.rgb, blurColor.rgb, currentIntensity);
            // 输出颜色
            gl_FragColor = vec4(resultColor, 1.0);
        }
        );

GLBeautyAdjustFilter::GLBeautyAdjustFilter() {
    intensity = 0.5f;
}

GLBeautyAdjustFilter::~GLBeautyAdjustFilter() {

}

void GLBeautyAdjustFilter::initProgram() {
    if (!isInitialized()) {
        initProgram(kDefaultVertexShader.c_str(), kBeautyAdjustFragmentShader.c_str());
    }
}

void GLBeautyAdjustFilter::initProgram(const char *vertexShader, const char *fragmentShader) {
    GLFilter::initProgram(vertexShader, fragmentShader);
    if (isInitialized()) {
        blurTextureHandle = glGetUniformLocation(programHandle, "blurTexture");
        highPassBlurTextureHandle = glGetUniformLocation(programHandle, "highPassBlurTexture");
        intensityHandle = glGetUniformLocation(programHandle, "intensity");
    }
}

void GLBeautyAdjustFilter::onDrawBegin() {
    GLFilter::onDrawBegin();
    if (isInitialized()) {
        OpenGLUtils::bindTexture(blurTextureHandle, blurTexture, 1);
        OpenGLUtils::bindTexture(highPassBlurTextureHandle, highPassBlurTexture, 2);
        glUniform1f(intensityHandle, intensity);
    }
}

void GLBeautyAdjustFilter::setBlurTexture(int blurTexture, int highPassBlurTexture) {
    this->blurTexture = blurTexture;
    this->highPassBlurTexture = highPassBlurTexture;
}
