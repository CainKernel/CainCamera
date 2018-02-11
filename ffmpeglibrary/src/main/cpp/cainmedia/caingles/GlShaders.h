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
            textureCoordinate = aTextureCoord.xy;
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

#endif //CAINCAMERA_GLSHADERS_H
