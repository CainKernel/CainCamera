//
// Created by CainHuang on 2019/3/22.
//

#include "GLExposureFilter.h"

const std::string kExposureFragmentShader = SHADER_TO_STRING(
        precision mediump float;
        varying highp vec2 textureCoordinate;

        uniform sampler2D inputTexture;
        uniform highp float intensity; // 曝光程度

        void main()
        {
            highp vec4 textureColor = texture2D(inputTexture, textureCoordinate);
            gl_FragColor = vec4(textureColor.rgb * pow(2.0, intensity), textureColor.w);
        }
        );


void GLExposureFilter::initProgram() {
    GLIntensityFilter::initProgram(kDefaultVertexShader.c_str(), kExposureFragmentShader.c_str());
}
