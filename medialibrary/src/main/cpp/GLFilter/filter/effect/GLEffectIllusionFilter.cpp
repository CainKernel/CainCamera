//
// Created by CainHuang on 2019/3/26.
//

#include "GLEffectIllusionFilter.h"

const std::string kIllusionFragmentShader =
        "#extension GL_EXT_shader_framebuffer_fetch : require\n"
        SHADER_TO_STRING(
        precision mediump float;
        varying vec2 textureCoordinate;
        uniform sampler2D inputTexture;     // 当前输入纹理

        // 分RGB通道混合，不同颜色通道混合值不一样
        const lowp vec3 blendValue = vec3(0.1, 0.3, 0.6);

        void main() {
            // 当前纹理颜色
            vec4 currentColor = texture2D(inputTexture, textureCoordinate);
            // 提取上一轮纹理颜色
            vec4 lastColor = gl_LastFragData[0];
            // 将纹理与上一轮的纹理进行线性混合
            gl_FragColor = vec4(mix(lastColor.rgb, currentColor.rgb, blendValue), currentColor.a);
        }
        );

void GLEffectIllusionFilter::initProgram() {
    GLFilter::initProgram(kDefaultVertexShader.c_str(), kIllusionFragmentShader.c_str());
}
