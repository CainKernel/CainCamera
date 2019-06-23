//
// Created by CainHuang on 2019/3/15.
//

#include "GLFrameThreeFilter.h"

const std::string kFrameThreeFragmentShader = SHADER_TO_STRING(
        precision highp float;
        uniform sampler2D inputTexture;
        varying highp vec2 textureCoordinate;

        void main() {
            highp vec2 uv = textureCoordinate;
            if (uv.y < 1.0 / 3.0) {
                uv.y = uv.y + 1.0 / 3.0;
            } else if (uv.y > 2.0 / 3.0) {
                uv.y = uv.y - 1.0 / 3.0;
            }
            gl_FragColor = texture2D(inputTexture, uv);
        }
        );

void GLFrameThreeFilter::initProgram() {
    GLFilter::initProgram(kDefaultVertexShader.c_str(), kFrameThreeFragmentShader.c_str());
}
