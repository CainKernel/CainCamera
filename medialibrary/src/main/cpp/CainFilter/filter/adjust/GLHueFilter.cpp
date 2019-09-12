//
// Created by CainHuang on 2019/3/22.
//

#include "GLHueFilter.h"

const std::string kHueFragmentShader = SHADER_TO_STRING(

        precision highp float;
        varying highp vec2 textureCoordinate;

        uniform sampler2D inputTexture;
        uniform mediump float intensity; // 色调程度
        const highp vec4 kRGBToYPrime = vec4 (0.299, 0.587, 0.114, 0.0);
        const highp vec4 kRGBToI = vec4 (0.595716, -0.274453, -0.321263, 0.0);
        const highp vec4 kRGBToQ = vec4 (0.211456, -0.522591, 0.31135, 0.0);

        const highp vec4 kYIQToR = vec4 (1.0, 0.9563, 0.6210, 0.0);
        const highp vec4 kYIQToG = vec4 (1.0, -0.2721, -0.6474, 0.0);
        const highp vec4 kYIQToB = vec4 (1.0, -1.1070, 1.7046, 0.0);

        void main ()
        {
            highp vec4 color = texture2D(inputTexture, textureCoordinate);
            // Convert to YIQ
            highp float YPrime = dot (color, kRGBToYPrime);
            highp float I = dot (color, kRGBToI);
            highp float Q = dot (color, kRGBToQ);

            // Calculate the hue and chroma
            highp float hue = atan (Q, I);
            highp float chroma = sqrt (I * I + Q * Q);

            // Make the user's adjustments
            hue += (-intensity); //why negative rotation?

            // Convert back to YIQ
            Q = chroma * sin (hue);
            I = chroma * cos (hue);

            // Convert back to RGB
            highp vec4 yIQ = vec4 (YPrime, I, Q, 0.0);
            color.r = dot (yIQ, kYIQToR);
            color.g = dot (yIQ, kYIQToG);
            color.b = dot (yIQ, kYIQToB);

            // Save the result
            gl_FragColor = color;
        }

        );

void GLHueFilter::initProgram() {
    GLIntensityFilter::initProgram(kDefaultVertexShader.c_str(), kHueFragmentShader.c_str());
}
