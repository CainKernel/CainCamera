//
// Created by CainHuang on 2019/3/22.
//

#include "GLContrastFilter.h"

const std::string kContrastFragmentShader = SHADER_TO_STRING(
        precision mediump float;
        varying highp vec2 textureCoordinate;

        uniform sampler2D inputTexture;
        uniform lowp float intensity; // 对比度程度

        void main()
        {
            lowp vec4 textureColor = texture2D(inputTexture, textureCoordinate);
            gl_FragColor = vec4(((textureColor.rgb - vec3(0.5)) * intensity + vec3(0.5)), textureColor.w);
        }
        );

void GLContrastFilter::initProgram() {
    GLIntensityFilter::initProgram(kDefaultVertexShader.c_str(), kContrastFragmentShader.c_str());
}
