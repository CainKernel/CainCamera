//
// Created by cain on 2019/1/16.
//

#include "BGRARenderer.h"
#include "GLUtils.h"
#include "RenderShaders.h"

BGRARenderer::BGRARenderer() {
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

BGRARenderer::~BGRARenderer() {

}

void BGRARenderer::reset() {
    resetVertices();
    resetTexVertices();
    mInited = false;
}

void BGRARenderer::resetVertices() {
    vertices[0] = -1.0f;
    vertices[1] = -1.0f;
    vertices[2] =  1.0f;
    vertices[3] = -1.0f;
    vertices[4] = -1.0f;
    vertices[5] =  1.0f;
    vertices[6] =  1.0f;
    vertices[7] =  1.0f;
}

void BGRARenderer::resetTexVertices() {
    texVetrices[0] = 0.0f;
    texVetrices[1] = 1.0f;
    texVetrices[2] = 1.0f;
    texVetrices[3] = 1.0f;
    texVetrices[4] = 0.0f;
    texVetrices[5] = 0.0f;
    texVetrices[6] = 1.0f;
    texVetrices[7] = 0.0f;
}

int BGRARenderer::onInit(Texture *texture) {
    // 判断是否已经初始化
    if (mInited && programHandle != 0) {
        return 0;
    }
    programHandle = GLUtils::createProgram(GetDefaultVertexShader(), GetFragmentShader_BGRA());
    GLUtils::checkGLError("createProgram");
    positionHandle = glGetAttribLocation(programHandle, "aPosition");
    texCoordHandle = glGetAttribLocation(programHandle, "aTexCoord");
    textureHandle[0] = glGetUniformLocation(programHandle, "inputTexture");

    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
    glUseProgram(programHandle);

    if (textures[0] == 0) {
        glGenTextures(1, textures);
    }
    for (int i = 0; i < 1; ++i) {
        glActiveTexture(GL_TEXTURE0 + i);
        glBindTexture(GL_TEXTURE_2D, textures[i]);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    }

    mInited = true;
    return 0;
}

GLboolean BGRARenderer::uploadTexture(Texture *texture) {
    if (!texture || programHandle == 0) {
        return GL_FALSE;
    }
    // 需要设置4字节对齐
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
    glUseProgram(programHandle);
    glClear(GL_COLOR_BUFFER_BIT);

    // 更新纹理数据
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, textures[0]);
    glTexImage2D(GL_TEXTURE_2D,
                 0,
                 GL_RGBA,                 // 对于YUV来说，数据格式是GL_LUMINANCE亮度值，而对于BGRA来说，这个则是颜色通道值
                 texture->pitches[0] / 4, // pixels中存放的数据是BGRABGRABGRA方式排列的，这里除4是为了求出对齐后的宽度，也就是每个颜色通道的数值
                 texture->height,
                 0,
                 GL_RGBA,
                 GL_UNSIGNED_BYTE,
                 texture->pixels[0]);
    glUniform1i(textureHandle[0], 0);

    return GL_TRUE;
}

GLboolean BGRARenderer::renderTexture(Texture *texture) {
    if (!texture || programHandle < 0) {
        return GL_FALSE;
    }

    // TODO 后续添加缩放裁剪处理
    if (texture->viewWidth != 0 && texture->viewHeight != 0) {
        glViewport(0, 0, texture->viewWidth, texture->viewHeight);
    }
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
