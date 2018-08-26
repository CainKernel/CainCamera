//
// Created by cain on 2018/2/8.
//

#include "GlShaders.h"

#ifdef __cplusplus
extern "C" {
#endif

#include <stdio.h>

// vertex shader
const char *vertex_shader_default = SHADER_STRING(
        uniform mat4 uMVPMatrix;
        attribute vec4 aPosition;
        attribute vec4 aTextureCoord;
        varying vec2 textureCoordinate;
        void main() {
            gl_Position = uMVPMatrix * aPosition;
            textureCoordinate = vec2(aTextureCoord.x, aTextureCoord.y);
        }
);


// vertex shader reverse image
const char *vertex_shader_reverse = SHADER_STRING(
//        uniform mat4 uMVPMatrix;
        attribute vec4 aPosition;
        attribute vec4 aTextureCoord;
        varying vec2 textureCoordinate;
        void main() {
//            gl_Position = uMVPMatrix * aPosition;
            gl_Position = aPosition;
            textureCoordinate = vec2(aTextureCoord.x, 1.0 - aTextureCoord.y);
        }
);

// fragment shader solid color
const char *fragment_shader_solid = SHADER_STRING(
        precision mediump float;
        varying highp vec2 textureCoordinate;
        uniform vec4 aColor;
        void main() {
            gl_FragColor = aColor;
        }
);

// fragment shader(BGR to ABGR conversion)
const char *fragment_shader_bgr = SHADER_STRING(
        precision highp float;

        varying highp vec2 textureCoordinate;
        uniform lowp sampler2D inputTexture;

        void main()
        {
            gl_FragColor = vec4(texture2D(inputTexture, textureCoordinate).rgb, 1);
        }
);

// fragment shader(ABGR)
const char *fragment_shader_abgr = SHADER_STRING(
        precision highp float;

        varying highp vec2 textureCoordinate;
        uniform lowp sampler2D inputTexture;

        void main()
        {
            gl_FragColor = texture2D(inputTexture, textureCoordinate);
        }
);

// fragment shader ( ARGB to ABGR conversion)
const char *fragment_shader_argb = SHADER_STRING(
        precision highp float;

        varying highp vec2 textureCoordinate;
        uniform lowp sampler2D inputTexture;

        void main()
        {
            vec4 abgr = texture2D(inputTexture, textureCoordinate);

            gl_FragColor = abgr;
            gl_FragColor.r = abgr.b;
            gl_FragColor.b = abgr.r;
        }
);

// fragment shader (RGB to ABGR conversion)
const char *fragment_shader_rgb = SHADER_STRING(
        precision highp float;

        varying highp vec2 textureCoordinate;
        uniform lowp sampler2D inputTexture;

        void main()
        {
            vec4 abgr = texture2D(inputTexture, textureCoordinate);

            gl_FragColor = abgr;
            gl_FragColor.r = abgr.b;
            gl_FragColor.b = abgr.r;
            gl_FragColor.a = 1.0;
        }
);

// fragment shader RGBA
const char *fragment_shader_rgba = SHADER_STRING(
        precision highp float;

        varying highp vec2 textureCoordinate;
        uniform lowp sampler2D inputTexture;

        void main()
        {
            gl_FragColor = texture2D(inputTexture, textureCoordinate);
        }
);

// fragment shader (YUV420P/I420 to ARGB conversion)
const char *fragment_shader_i420 = SHADER_STRING(
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

// fragment shader (NV12 to ARGB conversion)
const char *fragment_shader_nv12 = SHADER_STRING(
        precision mediump float;
        varying highp vec2 textureCoordinate;
        uniform lowp sampler2D inputTextureY;
        uniform lowp sampler2D inputTextureUV;

        void main() {
            mediump vec3 yuv;
            lowp vec3 rgb;
            yuv.x = texture2D(inputTextureY, textureCoordinate).r;
            yuv.yz = texture2D(inputTextureUV, textureCoordinate).ra - 0.5;
            rgb = mat3(1.0,          1.0,      1.0,
                       0.0,     -0.39465,  2.03211,
                       1.13983, -0.58060,      0.0) * yuv;
            gl_FragColor = vec4(rgb, 1.0);
        }
);

// fragment shader (NV21 to ARGB conversion)
const char *fragment_shader_nv21 = SHADER_STRING(
        precision mediump float;
        varying highp vec2 textureCoordinate;
        uniform lowp sampler2D inputTextureY;
        uniform lowp sampler2D inputTextureUV;

        void main() {
            mediump vec3 yuv;
            lowp vec3 rgb;
            yuv.x = texture2D(inputTextureY, textureCoordinate).r;
            yuv.yz = texture2D(inputTextureUV, textureCoordinate).ar - 0.5;
            rgb = mat3(1.0,          1.0,      1.0,
                       0.0,     -0.39465,  2.03211,
                       1.13983, -0.58060,      0.0) * yuv;
            gl_FragColor = vec4(rgb, 1.0);
        }
);

/**
 * 获取shader程序
 * @param type
 * @return
 */
const char *GlShader_GetShader(ShaderType type) {
    switch(type) {
        case VERTEX_DEFAULT:
            return vertex_shader_default;
        case VERTEX_REVERSE:
            return vertex_shader_reverse;
        case FRAGMENT_SOLID:
            return fragment_shader_solid;
        case FRAGMENT_ABGR:
            return fragment_shader_abgr;
        case FRAGMENT_ARGB:
            return fragment_shader_argb;
        case FRAGMENT_BGR:
            return fragment_shader_bgr;
        case FRAGMENT_RGB:
            return fragment_shader_rgb;
        case FRAGMENT_RGBA:
            return fragment_shader_rgba;
        case FRAGMENT_I420:
            return fragment_shader_i420;
        case FRAGMENT_NV12:
            return fragment_shader_nv12;
        case FRAGMENT_NV21:
            return fragment_shader_nv21;
        default:
            return NULL;
    }
}


#ifdef __cplusplus
};
#endif