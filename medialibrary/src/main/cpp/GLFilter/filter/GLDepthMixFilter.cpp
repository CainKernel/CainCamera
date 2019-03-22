//
// Created by CainHuang on 2019/3/22.
//

#include "GLDepthMixFilter.h"

const std::string kDepthMixFragmentShader = SHADER_TO_STRING(

        precision highp float;
        varying vec2 textureCoordinate;
        uniform sampler2D inputTexture;         // 输入纹理
        uniform sampler2D blurImageTexture;     // 经过高斯模糊处理的纹理
        uniform float inner;    // 内圆半径
        uniform float outer;    // 外圆半径
        uniform float width;    // 纹理宽度
        uniform float height;   // 纹理高度
        uniform vec2 center;    // 中心点的位置
        uniform vec3 line1;     // 前景深
        uniform vec3 line2;     // 后景深
        uniform float intensity;// 景深程度

        void main() {
            vec4 originalColor = texture2D(inputTexture, textureCoordinate);
            vec4 tempColor;
            float ratio = height / width;
            vec2 ellipse = vec2(1, ratio * ratio);
            float fx = (textureCoordinate.x - center.x);
            float fy = (textureCoordinate.y - center.y);
            // 用椭圆方程求离中心点的距离
            float dist = sqrt(fx * fx * ellipse.x + fy * fy * ellipse.y);
            // 如果小于内圆半径，则直接输出原图，否则拿原始纹理跟高斯模糊的纹理按照不同的半径进行alpha混合
            if (dist < inner) {
                tempColor = originalColor;
            } else {
                vec3 point = vec3(textureCoordinate.x, textureCoordinate.y, 1.0);
                float value1 = dot(line1, point);
                float value2 = dot(line2, point);
                if (value1 >= 0.0 && value2 >= 0.0) {
                    tempColor = originalColor;
                } else {
                    vec4 blurColor = texture2D(blurImageTexture, textureCoordinate);
                    float lineAlpha = max(-value1 / 0.15, -value2 / 0.15);
                    float alpha = (dist - inner)/outer;
                    alpha = min(lineAlpha, alpha);
                    alpha = clamp(alpha, 0.0, 1.0);
                    tempColor = mix(originalColor, blurColor, alpha);
                }
            }
            gl_FragColor = mix(originalColor, tempColor, intensity);
        }

        );


GLDepthMixFilter::GLDepthMixFilter() {
    blurTexture = -1;
    inner  = 0.35f;
    outer = 0.12f;
    center = Vector2(0.5f, 0.5f);
    line1 = Vector3(0.0f, 0.0f, -0.15f);
    line2 = Vector3(0.0f, 0.0f, -0.15f);
}

void GLDepthMixFilter::initProgram() {
    initProgram(kDefaultVertexShader.c_str(), kDepthMixFragmentShader.c_str());
}

void GLDepthMixFilter::initProgram(const char *vertexShader, const char *fragmentShader) {
    GLIntensityFilter::initProgram(vertexShader, fragmentShader);
    if (isInitialized()) {
        blurImageHandle = glGetUniformLocation(programHandle, "blurImageTexture");
        innerHandle = glGetUniformLocation(programHandle, "inner");
        outerHandle = glGetUniformLocation(programHandle, "outer");
        widthHandle = glGetUniformLocation(programHandle, "width");
        heightHandle = glGetUniformLocation(programHandle, "height");
        centerHandle = glGetUniformLocation(programHandle, "center");
        line1Handle = glGetUniformLocation(programHandle, "line1");
        line2Handle = glGetUniformLocation(programHandle, "line2");
    }
}

void GLDepthMixFilter::setBlurTexture(int texture) {
    blurTexture = texture;
}

void GLDepthMixFilter::bindTexture(GLuint texture) {
    GLFilter::bindTexture(texture);
    if (isInitialized()) {
        OpenGLUtils::bindTexture(blurImageHandle, blurTexture, 1);
    }
}

void GLDepthMixFilter::onDrawBegin() {
    GLFilter::onDrawBegin();
    if (isInitialized()) {
        glUniform1f(innerHandle, inner);
        glUniform1f(outerHandle, outer);
        glUniform1f(widthHandle, textureWidth);
        glUniform1f(heightHandle, textureHeight);
        glUniform2fv(centerHandle, 1, center.ptr());
        glUniform3fv(line1Handle, 1, line1.ptr());
        glUniform3fv(line2Handle, 1, line2.ptr());
    }
}


