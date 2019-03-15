//
// Created by CainHuang on 2019/3/15.
//

#include "GLFrameTwoFilter.h"

const std::string kFrameTwoFragmentShader = SHADER_TO_STRING(
        precision highp float;
        uniform sampler2D inputTexture;
        varying highp vec2 textureCoordinate;

        void main() {
            // 纹理坐标
            vec2 uv = textureCoordinate.xy;
            float y;
            if (uv.y >= 0.0 && uv.y <= 0.5) {
                y = uv.y + 0.25;
            } else {
                y = uv.y - 0.25;
            }

            gl_FragColor = texture2D(inputTexture, vec2(uv.x, y));
        }
);

void GLFrameTwoFilter::initProgram() {
    GLFilter::initProgram(kDefaultVertexShader.c_str(), kFrameTwoFragmentShader.c_str());
}
