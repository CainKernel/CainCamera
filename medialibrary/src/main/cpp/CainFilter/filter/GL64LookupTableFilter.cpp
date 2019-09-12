//
// Created by CainHuang on 2019/3/21.
//

#include <common/OpenGLUtils.h>
#include "GL64LookupTableFilter.h"

const std::string k64LutFragmentShader = SHADER_TO_STRING(
        precision mediump float;
        varying highp vec2 textureCoordinate;

        uniform sampler2D inputTexture;         // 输入图像纹理
        uniform sampler2D lutTexture;   // lut纹理

        uniform lowp float intensity;            // 滤镜强度值，0 ~ 1.0f

        void main() {
            lowp vec4 textureColor = texture2D(inputTexture, textureCoordinate);

            mediump float blueColor = textureColor.b * 15.0;

            mediump vec2 quad1;
            quad1.y = floor(blueColor / 4.0);
            quad1.x = floor(blueColor) - (quad1.y * 4.0);

            mediump vec2 quad2;
            quad2.y = floor(ceil(blueColor) / 4.0);
            quad2.x = ceil(blueColor) - (quad2.y * 4.0);

            highp vec2 texPos1;
            texPos1.x = (quad1.x * 0.25) + 0.5/64.0 + ((0.25 - 1.0/64.0) * textureColor.r);
            texPos1.y = (quad1.y * 0.25) + 0.5/64.0 + ((0.25 - 1.0/64.0) * textureColor.g);

            highp vec2 texPos2;
            texPos2.x = (quad2.x * 0.25) + 0.5/64.0 + ((0.25 - 1.0/64.0) * textureColor.r);
            texPos2.y = (quad2.y * 0.25) + 0.5/64.0 + ((0.25 - 1.0/64.0) * textureColor.g);

            lowp vec4 newColor1 = texture2D(lutTexture, texPos1);
            lowp vec4 newColor2 = texture2D(lutTexture, texPos2);

            lowp vec4 newColor = mix(newColor1, newColor2, fract(blueColor));
            gl_FragColor = mix(textureColor, vec4(newColor.rgb, textureColor.w), intensity);
        }
);

GL64LookupTableFilter::GL64LookupTableFilter() : lutTexture(-1) {

}

void GL64LookupTableFilter::initProgram() {
    initProgram(kDefaultVertexShader.c_str(), k64LutFragmentShader.c_str());
}

void GL64LookupTableFilter::initProgram(const char *vertexShader, const char *fragmentShader) {
    GLIntensityFilter::initProgram(vertexShader, fragmentShader);
    if (isInitialized()) {
        inputTextureHandle[1] = glGetUniformLocation(programHandle, "lutTexture");
    }
}

void GL64LookupTableFilter::setLutTexture(int lutTexture) {
    this->lutTexture = lutTexture;
}

void GL64LookupTableFilter::bindTexture(GLuint texture) {
    GLFilter::bindTexture(texture);
    if (isInitialized()) {
        OpenGLUtils::bindTexture(inputTextureHandle[1], lutTexture, 1);
    }
}
