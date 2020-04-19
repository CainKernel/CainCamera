//
// Created by CainHuang on 2019/3/13.
//

#include "base/OpenGLUtils.h"
#include <AndroidLog.h>
#include "GLInputYUV420PFilter.h"

const std::string kYUV420PFragmentShader = SHADER_TO_STRING(
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

GLInputYUV420PFilter::GLInputYUV420PFilter() {
    for (int i = 0; i < GLES_MAX_PLANE; ++i) {
        inputTextureHandle[i] = 0;
        textures[i] = 0;
    }
}

GLInputYUV420PFilter::~GLInputYUV420PFilter() {

}

void GLInputYUV420PFilter::initProgram() {
    initProgram(kDefaultVertexShader.c_str(), kYUV420PFragmentShader.c_str());
}

void GLInputYUV420PFilter::initProgram(const char *vertexShader, const char *fragmentShader) {
    if (vertexShader && fragmentShader) {
        programHandle = OpenGLUtils::createProgram(vertexShader, fragmentShader);
        OpenGLUtils::checkGLError("createProgram");
        positionHandle = glGetAttribLocation(programHandle, "aPosition");
        texCoordinateHandle = glGetAttribLocation(programHandle, "aTextureCoord");
        inputTextureHandle[0] = glGetUniformLocation(programHandle, "inputTextureY");
        inputTextureHandle[1] = glGetUniformLocation(programHandle, "inputTextureU");
        inputTextureHandle[2] = glGetUniformLocation(programHandle, "inputTextureV");

        // 4字节对齐
        glPixelStorei(GL_UNPACK_ALIGNMENT, 4);
        glUseProgram(programHandle);

        if (textures[0] == 0) {
            glGenTextures(3, textures);
        }
        for (int i = 0; i < 3; ++i) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, textures[i]);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glUniform1i(inputTextureHandle[i], i);
        }
        setInitialized(true);
    } else {
        positionHandle = -1;
        inputTextureHandle[0] = -1;
        inputTextureHandle[1] = -1;
        inputTextureHandle[2] = -1;
        setInitialized(false);
    }
}

GLboolean GLInputYUV420PFilter::uploadTexture(Texture *texture) {
    // 需要设置4字节对齐
    glPixelStorei(GL_UNPACK_ALIGNMENT, 4);
    glUseProgram(programHandle);

    // 更新绑定纹理的数据
    const GLsizei heights[3] = { texture->height, texture->height / 2, texture->height / 2};
    for (int i = 0; i < 3; ++i) {
        glActiveTexture(GL_TEXTURE0 + i);
        glBindTexture(GL_TEXTURE_2D, textures[i]);
        glTexImage2D(GL_TEXTURE_2D,
                     0,
                     GL_LUMINANCE,
                     texture->pitches[i],
                     heights[i],
                     0,
                     GL_LUMINANCE,
                     GL_UNSIGNED_BYTE,
                     texture->pixels[i]);
        glUniform1i(inputTextureHandle[i], i);
    }
    return GL_TRUE;
}

GLboolean GLInputYUV420PFilter::renderTexture(Texture *texture, float *vertices, float *textureVertices) {
    if (!isInitialized() || !texture) {
        return GL_FALSE;
    }
    // 绑定属性值
    bindAttributes(vertices, textureVertices);
    // 绘制前处理
    onDrawBegin();
    // 绘制纹理
    onDrawFrame();
    // 绘制后处理
    onDrawAfter();
    // 解绑属性
    unbindAttributes();
    // 解绑纹理
    unbindTextures();
    // 解绑program
    glUseProgram(0);
    return GL_TRUE;
}

