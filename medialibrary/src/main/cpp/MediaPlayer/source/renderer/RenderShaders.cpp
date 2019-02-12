//
// Created by cain on 2019/1/14.
//

#include "RenderShaders.h"

static const char vertex_shader[] = SHADER_STRING(
        precision mediump float;
        attribute highp vec4 aPosition;
        attribute highp vec2 aTexCoord;
        varying vec2 textureCoordinate;
        void main()
        {
            gl_Position  = aPosition;
            textureCoordinate = aTexCoord.xy;
        }
);

const char *GetDefaultVertexShader() {
    return vertex_shader;
}

static const char fragment_shader_bgra[] = SHADER_STRING(
        precision mediump float;
        uniform sampler2D inputTexture;
        varying vec2 textureCoordinate;

        void main()
        {
            vec4 abgr = texture2D(inputTexture, textureCoordinate);
            gl_FragColor = abgr;
            gl_FragColor.r = abgr.b;
            gl_FragColor.b = abgr.r;
        }
);

const char *GetFragmentShader_BGRA() {
    return fragment_shader_bgra;
}

// 这是电视机的YUV420P的shader，色彩空间在16 ~ 235之间
static const char fragment_shader_yuv420p[] = SHADER_STRING(
        precision mediump float;
        varying highp vec2 textureCoordinate;
        uniform lowp sampler2D inputTextureY;
        uniform lowp sampler2D inputTextureU;
        uniform lowp sampler2D inputTextureV;

        void main() {
            vec3 yuv;
            vec3 rgb;
            yuv.r = texture2D(inputTextureY, textureCoordinate).r - (16.0 / 255.0);
            yuv.g = texture2D(inputTextureU, textureCoordinate).r - 0.5;
            yuv.b = texture2D(inputTextureV, textureCoordinate).r - 0.5;
            rgb = mat3(1.164,  1.164,  1.164,
                       0.0,   -0.213,  2.112,
                       1.793, -0.533,    0.0) * yuv;
            gl_FragColor = vec4(rgb, 1.0);
        }
);

// 这是显示器的YUVJ420P的shader，色彩空间在0 ~ 255
static const char fragment_shader_yuvj420p[] = SHADER_STRING(
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

const char *GetFragmentShader_YUV420P() {
    return fragment_shader_yuv420p;
}