//
// Created by CainHuang on 2019/3/22.
//

#include "GLSharpenFilter.h"

const std::string kSharpenVertexShader = SHADER_TO_STRING(

        attribute vec4 aPosition;
        attribute vec4 aTextureCoord;

        uniform float imageWidthFactor;
        uniform float imageHeightFactor;
        uniform float intensity; // 锐化程度

        varying vec2 textureCoordinate;
        varying vec2 leftTextureCoordinate;
        varying vec2 rightTextureCoordinate;
        varying vec2 topTextureCoordinate;
        varying vec2 bottomTextureCoordinate;

        varying float centerMultiplier;
        varying float edgeMultiplier;
        void main()
        {
            gl_Position = aPosition;

            mediump vec2 widthStep = vec2(imageWidthFactor, 0.0);
            mediump vec2 heightStep = vec2(0.0, imageHeightFactor);

            textureCoordinate = aTextureCoord.xy;
            leftTextureCoordinate = aTextureCoord.xy - widthStep;
            rightTextureCoordinate = aTextureCoord.xy + widthStep;
            topTextureCoordinate = aTextureCoord.xy + heightStep;
            bottomTextureCoordinate = aTextureCoord.xy - heightStep;

            centerMultiplier = 1.0 + 4.0 * intensity;
            edgeMultiplier = intensity;
        }

        );

const std::string kSharpenFragmentShader = SHADER_TO_STRING(

        precision highp float;

        varying highp vec2 textureCoordinate;
        varying highp vec2 leftTextureCoordinate;
        varying highp vec2 rightTextureCoordinate;
        varying highp vec2 topTextureCoordinate;
        varying highp vec2 bottomTextureCoordinate;

        varying highp float centerMultiplier;
        varying highp float edgeMultiplier;

        uniform sampler2D inputTexture;

        void main()
        {
            mediump vec3 textureColor = texture2D(inputTexture, textureCoordinate).rgb;
            mediump vec3 leftTextureColor = texture2D(inputTexture, leftTextureCoordinate).rgb;
            mediump vec3 rightTextureColor = texture2D(inputTexture, rightTextureCoordinate).rgb;
            mediump vec3 topTextureColor = texture2D(inputTexture, topTextureCoordinate).rgb;
            mediump vec3 bottomTextureColor = texture2D(inputTexture, bottomTextureCoordinate).rgb;

            gl_FragColor = vec4((textureColor * centerMultiplier
                                 - (leftTextureColor * edgeMultiplier
                                    + rightTextureColor * edgeMultiplier
                                    + topTextureColor * edgeMultiplier
                                    + bottomTextureColor * edgeMultiplier)),
                                texture2D(inputTexture, bottomTextureCoordinate).w);
        }

        );

void GLSharpenFilter::initProgram() {
    initProgram(kSharpenVertexShader.c_str(), kSharpenFragmentShader.c_str());
}

void GLSharpenFilter::initProgram(const char *vertexShader, const char *fragmentShader) {
    GLIntensityFilter::initProgram(vertexShader, fragmentShader);
    if (isInitialized()) {
        widthFactorHandle = glGetUniformLocation(programHandle, "imageWidthFactor");
        heightFactorHandle = glGetUniformLocation(programHandle, "imageHeightFactor");
    }
}

void GLSharpenFilter::onDrawBegin() {
    GLIntensityFilter::onDrawBegin();
    if (isInitialized() && textureWidth > 0 && textureHeight > 0) {
        glUniform1f(widthFactorHandle, 1.0f/textureWidth);
        glUniform1f(heightFactorHandle, 1.0f/textureHeight);
    }
}
