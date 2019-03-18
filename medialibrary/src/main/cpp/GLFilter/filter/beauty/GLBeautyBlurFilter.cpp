//
// Created by CainHuang on 2019/3/18.
//

#include "GLBeautyBlurFilter.h"

const std::string kBeautyBlurVertexShader = SHADER_TO_STRING(
        attribute vec4 aPosition;
        attribute vec4 aTextureCoord;

        // 高斯算子左右偏移值，当偏移值为5时，高斯算子为 11 x 11
        const int SHIFT_SIZE = 5;

        uniform highp float texelWidthOffset;
        uniform highp float texelHeightOffset;

        varying vec2 textureCoordinate;
        varying vec4 blurShiftCoordinates[SHIFT_SIZE];

        void main() {
            gl_Position = aPosition;
            textureCoordinate = aTextureCoord.xy;
            // 偏移步距
            vec2 singleStepOffset = vec2(texelWidthOffset, texelHeightOffset);
            // 记录偏移坐标
            for (int i = 0; i < SHIFT_SIZE; i++) {
                blurShiftCoordinates[i] = vec4(textureCoordinate.xy - float(i + 1) * singleStepOffset,
                                               textureCoordinate.xy + float(i + 1) * singleStepOffset);
            }
        }
        );

const std::string kBeautyBlurFragmentShader = SHADER_TO_STRING(
        precision mediump float;
        varying vec2 textureCoordinate;
        uniform sampler2D inputTexture;
        const int SHIFT_SIZE = 5; // 高斯算子左右偏移值
        varying vec4 blurShiftCoordinates[SHIFT_SIZE];
        void main() {
            // 计算当前坐标的颜色值
            vec4 currentColor = texture2D(inputTexture, textureCoordinate);
            mediump vec3 sum = currentColor.rgb;
            // 计算偏移坐标的颜色值总和
            for (int i = 0; i < SHIFT_SIZE; i++) {
                sum += texture2D(inputTexture, blurShiftCoordinates[i].xy).rgb;
                sum += texture2D(inputTexture, blurShiftCoordinates[i].zw).rgb;
            }
            // 求出平均值
            gl_FragColor = vec4(sum * 1.0 / float(2 * SHIFT_SIZE + 1), currentColor.a);
        }
        );

GLBeautyBlurFilter::GLBeautyBlurFilter()
        : GLGaussianBlurFilter(kBeautyBlurVertexShader.c_str(), kBeautyBlurFragmentShader.c_str()) {

}

GLBeautyBlurFilter::GLBeautyBlurFilter(const char *vertexShader, const char *fragmentShader)
        : GLGaussianBlurFilter(vertexShader, fragmentShader) {

}
