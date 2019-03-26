//
// Created by CainHuang on 2019/3/26.
//

#include "GLEffectGlitterWhiteFilter.h"
#include <math.h>

const std::string kGliterWhiteFragmentShader = SHADER_TO_STRING(
        precision mediump float;
        varying vec2 textureCoordinate;
        uniform sampler2D inputTexture;

        uniform float color; // 增强的颜色值

        void main() {
            vec2 uv = textureCoordinate.xy;
            vec4 textureColor = texture2D(inputTexture, uv);

            // 计算出最后的rgb颜色，并限定范围到0.0 ~ 1.0的范围
            float rColor = clamp(textureColor.r + color, 0.0, 1.0);
            float gColor = clamp(textureColor.g + color, 0.0, 1.0);
            float bColor = clamp(textureColor.b + color, 0.0, 1.0);

            // 输出结果
            gl_FragColor = vec4(rColor, gColor, bColor, textureColor.a);
        }
        );

GLEffectGlitterWhiteFilter::GLEffectGlitterWhiteFilter() : color(0.0f), colorHandle(-1) {

}

void GLEffectGlitterWhiteFilter::initProgram() {
    initProgram(kDefaultVertexShader.c_str(), kGliterWhiteFragmentShader.c_str());
}

void GLEffectGlitterWhiteFilter::initProgram(const char *vertexShader, const char *fragmentShader) {
    GLFilter::initProgram(vertexShader, fragmentShader);
    if (isInitialized()) {
        colorHandle = glGetUniformLocation(programHandle, "color");
    }
}

void GLEffectGlitterWhiteFilter::setTimeStamp(double timeStamp) {
    GLFilter::setTimeStamp(timeStamp);
    double interval = fmod(timeStamp, 40);
    color += interval * 0.015f;
    if (color > 1.0f) {
        color = 0.0f;
    }
}

void GLEffectGlitterWhiteFilter::onDrawBegin() {
    GLFilter::onDrawBegin();
    if (isInitialized()) {
        glUniform1f(colorHandle, color);
    }
}

