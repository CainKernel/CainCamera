//
// Created by CainHuang on 2019/3/22.
//

#include "GLSaturationFilter.h"

const std::string kSaturationFragmentShader = SHADER_TO_STRING(

        precision mediump float;
        varying highp vec2 textureCoordinate;
        uniform sampler2D inputTexture;
        uniform lowp float intensity; // 饱和程度
        const mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);
        void main() {
            lowp vec4 textureColor = texture2D(inputTexture, textureCoordinate);
            lowp float luminance = dot(textureColor.rgb, luminanceWeighting);
            lowp vec3 greyScaleColor = vec3(luminance);
            gl_FragColor = vec4(mix(greyScaleColor, textureColor.rgb, intensity), textureColor.w);
        }

        );

void GLSaturationFilter::initProgram() {
    GLIntensityFilter::initProgram(kDefaultVertexShader.c_str(), kSaturationFragmentShader.c_str());
}
