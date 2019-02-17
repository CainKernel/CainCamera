//
// Created by cain on 2019/1/14.
//

#include "YUV420PRenderer.h"
#include "GLUtils.h"
#include "RenderShaders.h"

YUV420PRenderer::YUV420PRenderer() {
    programHandle = 0;
    positionHandle = 0;
    texCoordHandle = 0;
    mvpMatrixHandle = 0;
    for (int i = 0; i < GLES_MAX_PLANE; ++i) {
        textureHandle[i] = 0;
        textures[i] = 0;
    }
    reset();
}

YUV420PRenderer::~YUV420PRenderer() {

}

void YUV420PRenderer::reset() {
    resetVertices();
    resetTexVertices();
    mInited = false;
}

void YUV420PRenderer::resetVertices() {
    vertices[0] = -1.0f;
    vertices[1] = -1.0f;
    vertices[2] =  1.0f;
    vertices[3] = -1.0f;
    vertices[4] = -1.0f;
    vertices[5] =  1.0f;
    vertices[6] =  1.0f;
    vertices[7] =  1.0f;
}

void YUV420PRenderer::resetTexVertices() {
    texVetrices[0] = 0.0f;
    texVetrices[1] = 1.0f;
    texVetrices[2] = 1.0f;
    texVetrices[3] = 1.0f;
    texVetrices[4] = 0.0f;
    texVetrices[5] = 0.0f;
    texVetrices[6] = 1.0f;
    texVetrices[7] = 0.0f;
}

void YUV420PRenderer::cropTexVertices(Texture *texture) {
    // 帧宽度和linesize宽度不一致，需要裁掉多余的地方，否则会出现绿屏的情况
    if (texture->frameWidth != texture->width) {
        GLsizei padding = texture->width - texture->frameWidth;
        GLfloat normalized = ((GLfloat)padding + 0.5f) / (GLfloat)texture->width;
        texVetrices[0] = 0.0f;
        texVetrices[1] = 1.0f;
        texVetrices[2] = 1.0f - normalized;
        texVetrices[3] = 1.0f;
        texVetrices[4] = 0.0f;
        texVetrices[5] = 0.0f;
        texVetrices[6] = 1.0f - normalized;
        texVetrices[7] = 0.0f;
    } else {
        resetTexVertices();
    }
}


int YUV420PRenderer::onInit(Texture *texture) {
    // 判断是否已经初始化
    if (mInited && programHandle != 0) {
        return 0;
    }
    programHandle = GLUtils::createProgram(GetDefaultVertexShader(), GetFragmentShader_YUV420P());
    GLUtils::checkGLError("createProgram");
    positionHandle = glGetAttribLocation(programHandle, "aPosition");
    texCoordHandle = glGetAttribLocation(programHandle, "aTexCoord");
    textureHandle[0] = glGetUniformLocation(programHandle, "inputTextureY");
    textureHandle[1] = glGetUniformLocation(programHandle, "inputTextureU");
    textureHandle[2] = glGetUniformLocation(programHandle, "inputTextureV");

    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
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
        glUniform1i(textureHandle[i], i);
    }

    mInited = true;
    return 0;
}

GLboolean YUV420PRenderer::uploadTexture(Texture *texture) {
    if (!texture || programHandle == 0) {
        return GL_FALSE;
    }
    // 需要设置4字节对齐
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
    glUseProgram(programHandle);
    glClear(GL_COLOR_BUFFER_BIT);

    // 更新纹理数据
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
        glUniform1i(textureHandle[i], i);
    }

    return 0;
}

GLboolean YUV420PRenderer::renderTexture(Texture *texture) {
    if (!texture || programHandle < 0) {
        return GL_FALSE;
    }
    cropTexVertices(texture);
    // 绑定顶点坐标
    glVertexAttribPointer(positionHandle, 2, GL_FLOAT, GL_FALSE, 0, vertices);
    glEnableVertexAttribArray(positionHandle);
    // 绑定纹理坐标
    glVertexAttribPointer(texCoordHandle, 2, GL_FLOAT, GL_FALSE, 0, texVetrices);
    glEnableVertexAttribArray(texCoordHandle);
    // 绘制
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    // 解绑
    glDisableVertexAttribArray(texCoordHandle);
    glDisableVertexAttribArray(positionHandle);
    glUseProgram(0);
    return GL_TRUE;
}

