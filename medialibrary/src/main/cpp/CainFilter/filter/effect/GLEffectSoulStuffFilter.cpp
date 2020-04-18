//
// Created by CainHuang on 2019/3/26.
//

#include "GLEffectSoulStuffFilter.h"
#include <math.h>
#include <AndroidLog.h>

const std::string kSoulStuffFragmentShader = SHADER_TO_STRING(

        precision highp float;
        varying vec2 textureCoordinate;
        uniform sampler2D inputTexture;

        uniform float scale;

        void main() {
            vec2 uv = textureCoordinate.xy;
            // 输入纹理
            vec4 sourceColor = texture2D(inputTexture, fract(uv));
            // 将纹理坐标中心转成(0.0, 0.0)再做缩放
            vec2 center = vec2(0.5, 0.5);
            uv -= center;
            uv = uv / scale;
            uv += center;
            // 缩放纹理
            vec4 scaleColor = texture2D(inputTexture, fract(uv));
            // 线性混合
            gl_FragColor = mix(sourceColor, scaleColor, 0.5 * (0.6 - fract(scale)));
        }

        );

GLEffectSoulStuffFilter::GLEffectSoulStuffFilter() : scale(1.0f), offset(0.0f), scaleHandle(-1) {

}

void GLEffectSoulStuffFilter::initProgram() {
    initProgram(kDefaultVertexShader.c_str(), kSoulStuffFragmentShader.c_str());
}

void GLEffectSoulStuffFilter::initProgram(const char *vertexShader, const char *fragmentShader) {
    GLFilter::initProgram(vertexShader, fragmentShader);
    if (isInitialized()) {
        scaleHandle = glGetUniformLocation(programHandle, "scale");
    }
}

void GLEffectSoulStuffFilter::setTimeStamp(double timeStamp) {
    GLFilter::setTimeStamp(timeStamp);
    double interval = fmod(timeStamp, 33);
    offset += interval * 0.0025f;
    if (offset >= 1.0f) {
        offset = 0.0f;
    }
    scale = 1.0f + 0.3f * (float)(cos((offset + 1) * PI) / 2.0f + 0.5f);
}

void GLEffectSoulStuffFilter::onDrawBegin() {
    GLFilter::onDrawBegin();
    if (isInitialized()) {
        glUniform1f(scaleHandle, scale);
    }
}
