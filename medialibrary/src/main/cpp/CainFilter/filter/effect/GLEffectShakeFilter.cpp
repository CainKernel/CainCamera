//
// Created by CainHuang on 2019/3/26.
//

#include "GLEffectShakeFilter.h"
#include <math.h>

const std::string kShakeFragmentShader = SHADER_TO_STRING(
        precision highp float;
        varying vec2 textureCoordinate;
        uniform sampler2D inputTexture;

        uniform float scale;

        void main()
        {
            vec2 uv = textureCoordinate.xy;
            vec2 scaleCoordinate = vec2((scale - 1.0) * 0.5 + uv.x / scale ,
                                        (scale - 1.0) * 0.5 + uv.y / scale);
            vec4 smoothColor = texture2D(inputTexture, scaleCoordinate);

            // 计算红色通道偏移值
            vec4 shiftRedColor = texture2D(inputTexture,
                                           scaleCoordinate + vec2(-0.1 * (scale - 1.0), - 0.1 *(scale - 1.0)));

            // 计算绿色通道偏移值
            vec4 shiftGreenColor = texture2D(inputTexture,
                                             scaleCoordinate + vec2(-0.075 * (scale - 1.0), - 0.075 *(scale - 1.0)));

            // 计算蓝色偏移值
            vec4 shiftBlueColor = texture2D(inputTexture,
                                            scaleCoordinate + vec2(-0.05 * (scale - 1.0), - 0.05 *(scale - 1.0)));

            vec3 resultColor = vec3(shiftRedColor.r, shiftGreenColor.g, shiftBlueColor.b);

            gl_FragColor = vec4(resultColor, smoothColor.a);
        }
        );

GLEffectShakeFilter::GLEffectShakeFilter() : scale(1.0f), offset(0.0), scaleHandle(-1) {

}

void GLEffectShakeFilter::initProgram() {
    initProgram(kDefaultVertexShader.c_str(), kShakeFragmentShader.c_str());
}

void GLEffectShakeFilter::initProgram(const char *vertexShader, const char *fragmentShader) {
    GLFilter::initProgram(vertexShader, fragmentShader);
    if (isInitialized()) {
        scaleHandle = glGetUniformLocation(programHandle, "scale");
    }
}

void GLEffectShakeFilter::setTimeStamp(double timeStamp) {
    GLFilter::setTimeStamp(timeStamp);
    double interval = fmod(timeStamp, 40);
    offset += (float)(interval * 0.0050f);
    if (offset > 1.0f) {
        offset = 0.0f;
    }
    scale = 1.0f + 0.3f * (float)(cos((offset + 1) * PI) / 2.0f + 0.5f);
}

void GLEffectShakeFilter::onDrawBegin() {
    GLFilter::onDrawBegin();
    if (isInitialized()) {
        glUniform1f(scaleHandle, scale);
    }
}
