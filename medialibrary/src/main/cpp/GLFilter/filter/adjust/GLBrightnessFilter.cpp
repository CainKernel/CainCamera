//
// Created by CainHuang on 2019/3/22.
//

#include "GLBrightnessFilter.h"

const std::string kBrightnessFragmentShader = SHADER_TO_STRING(

        precision mediump float;
        varying highp vec2 textureCoordinate;
        uniform sampler2D inputTexture;
        uniform lowp float intensity;  // 亮度程度

        void main()
        {
            lowp vec4 textureColor = texture2D(inputTexture, textureCoordinate);
            gl_FragColor = vec4((textureColor.rgb + vec3(intensity)), textureColor.w);
        }

        );


void GLBrightnessFilter::initProgram() {
    GLIntensityFilter::initProgram(kDefaultVertexShader.c_str(), kBrightnessFragmentShader.c_str());
}
