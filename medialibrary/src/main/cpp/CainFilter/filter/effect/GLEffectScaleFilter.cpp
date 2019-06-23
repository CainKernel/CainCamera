//
// Created by CainHuang on 2019/3/26.
//

#include "GLEffectScaleFilter.h"
#include <math.h>

const std::string kScaleFragmentShader = SHADER_TO_STRING(

        precision mediump float;
        varying vec2 textureCoordinate;
        uniform sampler2D inputTexture;

        uniform float scale;

        void main() {
            vec2 uv = textureCoordinate.xy;
            // 将纹理坐标中心转成(0.0, 0.0)再做缩放
            vec2 center = vec2(0.5, 0.5);
            uv -= center;
            uv = uv / scale;
            uv += center;

            gl_FragColor = texture2D(inputTexture, uv);
        }

        );

GLEffectScaleFilter::GLEffectScaleFilter() : scale(1.0f), plus(true), offset(0.0f), scaleHandle(-1) {

}

void GLEffectScaleFilter::initProgram() {
    initProgram(kDefaultVertexShader.c_str(), kScaleFragmentShader.c_str());
}

void GLEffectScaleFilter::initProgram(const char *vertexShader, const char *fragmentShader) {
    GLFilter::initProgram(vertexShader, fragmentShader);
    if (isInitialized()) {
        scaleHandle = glGetUniformLocation(programHandle, "scale");
    }
}

void GLEffectScaleFilter::setTimeStamp(double timeStamp) {
    GLFilter::setTimeStamp(timeStamp);
    double interval = fmod(timeStamp, 33);
    offset += (plus ? 1.0f : -1.0f) * interval * 0.0067f;
    if (offset >= 1.0f) {
        plus = false;
    } else if (offset <= 0.0f) {
        plus = true;
    }
    scale = 1.0f + 0.5f * (float)(cos((offset + 1) * PI) / 2.0f + 0.5f);
}

void GLEffectScaleFilter::onDrawBegin() {
    GLFilter::onDrawBegin();
    if (isInitialized()) {
        glUniform1f(scaleHandle, scale);
    }
}
