//
// Created by Administrator on 2018/2/7.
//

#ifndef CAINCAMERA_GLSHADERS_H
#define CAINCAMERA_GLSHADERS_H

// 转成字符串
#define SHADER_STRING(s) #s

// vertex shader
static const char vertex_shader[] = SHADER_STRING(
        uniform mat4 uMVPMatrix;
        attribute vec4 aPosition;
        attribute vec4 aTextureCoord;
        varying vec2 textureCoordinate;
        void main() {
            gl_Position = uMVPMatrix * aPosition;
            // 将图像倒过来，因为OpenGLES 的格式是RGBA的，而图像的格式则是ARGB
            textureCoordinate = vec2(aTextureCoord.x, 1.0 - aTextureCoord.y);
        }
);

// fragment shader
static const char fragment_shader[] = SHADER_STRING(
        precision highp float;

        varying highp vec2 textureCoordinate;
        uniform lowp sampler2D inputTexture;

        void main()
        {
            gl_FragColor = vec4(texture2D(inputTexture, textureCoordinate).rgb, 1);
        }
);

// YUV420P fragment shader
static const char fragment_shader_yuv420p[] = SHADER_STRING(
        precision mediump float;
        varying highp vec2 textureCoordinate;
        uniform lowp sampler2D inputTextureY;
        uniform lowp sampler2D inputTextureU;
        uniform lowp sampler2D inputTextureV;

        void main() {
            vec3 yuv;
            vec3 rgb;
            yuv.r = texture2D(inputTextureY, textureCoordinate).r;
            yuv.g = texture2D(inputTextureU, textureCoordinate).r - 0.5;
            yuv.b = texture2D(inputTextureV, textureCoordinate).r - 0.5;
            rgb = mat3(1.0,          1.0,      1.0,
                       0.0,     -0.39465,  2.03211,
                       1.13983, -0.58060,      0.0) * yuv;
            gl_FragColor = vec4(rgb, 1.0);
        }
);

#endif //CAINCAMERA_GLSHADERS_H
