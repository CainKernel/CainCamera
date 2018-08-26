//
// Created by cain on 2018/5/6.
//

#include "YUV420PImageInputFilter.h"

YUV420PImageInputFilter::YUV420PImageInputFilter() {
    programHandle = GL_NONE;
    videoWidth = 0;
    videoHeight = 0;
    surfacWidth = 0;
    surfaceHeight = 0;
    left = 0;
    top = 0;
    viewWidth = 0;
    videoHeight = 0;
    initCoordinates();
}

YUV420PImageInputFilter::~YUV420PImageInputFilter() {

}

int YUV420PImageInputFilter::initHandle(void) {
    programHandle = createProgram(GlShader_GetShader(VERTEX_REVERSE), GlShader_GetShader(FRAGMENT_I420));
    positionHandle = glGetAttribLocation(programHandle, "aPosition");
    textureCoordsHandle = glGetAttribLocation(programHandle, "aTextureCoord");
    yTextureHandle = glGetUniformLocation(programHandle, "inputTextureY");
    uTextureHandle = glGetUniformLocation(programHandle, "inputTextureU");
    vTextureHandle = glGetUniformLocation(programHandle, "inputTextureV");
    return 0;
}

void YUV420PImageInputFilter::initTexture() {
    glGenTextures(1, &yTextureId);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, yTextureId);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER, GL_LINEAR);

    glUniform1i(yTextureHandle, 0);

    glGenTextures(1,&uTextureId);
    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D,uTextureId);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER, GL_LINEAR);

    glUniform1i(uTextureHandle, 1);

    glGenTextures(1,&vTextureId);
    glActiveTexture(GL_TEXTURE2);
    glBindTexture(GL_TEXTURE_2D,vTextureId);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER, GL_LINEAR);

    glUniform1i(vTextureHandle, 2);
}

void YUV420PImageInputFilter::onInputSizeChanged(int width, int height) {
    videoWidth = width;
    videoHeight = height;
}

void YUV420PImageInputFilter::onSurfaceChanged(int width, int height) {
    // 如果跟原来的大小相等，则不需要重新调整大小了
    if (surfacWidth != 0 && surfaceHeight != 0
        && surfacWidth == width && surfaceHeight == height) {
        return;
    }
    surfacWidth = width;
    surfaceHeight = height;
    // 重新计算屏幕大小
    if (surfaceHeight > surfacWidth) {
        left = 0;
        viewWidth = surfacWidth;
        viewHeight = (int)(videoHeight * 1.0f / videoWidth * viewWidth);
        top = (surfaceHeight - viewHeight) / 2;
    } else {
        top = 0;
        viewHeight = surfaceHeight;
        viewWidth = (int)(videoWidth * 1.0f / videoHeight * viewHeight);
        left = (surfacWidth - viewWidth) / 2;
    }
    glViewport(left, top, viewWidth, viewHeight);
}

bool YUV420PImageInputFilter::drawFrame(AVFrame *yuvFrame) {
    if (programHandle < 0 || yuvFrame == NULL) {
        return false;
    }
    glUseProgram(programHandle);

    glEnableVertexAttribArray(positionHandle);
    glVertexAttribPointer(positionHandle, 3, GL_FLOAT, GL_FALSE, 12, vertexCoordinates);

    glEnableVertexAttribArray(textureCoordsHandle);
    glVertexAttribPointer(textureCoordsHandle, 2, GL_FLOAT, GL_FALSE, 8, textureCoordinates);

    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, yTextureId);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, yuvFrame->linesize[0], yuvFrame->height, 0,
                 GL_LUMINANCE, GL_UNSIGNED_BYTE, yuvFrame->data[0]);

    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D, uTextureId);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, yuvFrame->linesize[1], yuvFrame->height / 2, 0,
                 GL_LUMINANCE, GL_UNSIGNED_BYTE, yuvFrame->data[1]);

    glActiveTexture(GL_TEXTURE2);
    glBindTexture(GL_TEXTURE_2D, vTextureId);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, yuvFrame->linesize[2], yuvFrame->height / 2, 0,
                 GL_LUMINANCE, GL_UNSIGNED_BYTE, yuvFrame->data[2]);

    glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

    glDisableVertexAttribArray(positionHandle);
    glDisableVertexAttribArray(textureCoordsHandle);

    glBindTexture(GL_TEXTURE_2D, 0);

    glUseProgram(0);
    return true;
}

int YUV420PImageInputFilter::drawFrameBuffer(AVFrame *yuvFrame) {
    if (mFrameBuffers[0] == GL_NONE) {
        return GL_NONE;
    }
    glViewport(0, 0, mFrameWidth, mFrameHeight);
    glBindFramebuffer(GL_FRAMEBUFFER, mFrameBuffers[0]);
    glUseProgram(programHandle);

    glVertexAttribPointer(positionHandle, 2, GL_FLOAT, GL_FALSE, 0, vertexCoordinates);
    glEnableVertexAttribArray(positionHandle);

    glVertexAttribPointer(textureCoordsHandle, 2, GL_FLOAT, GL_FALSE, 0, textureCoordinates);
    glEnableVertexAttribArray(textureCoordsHandle);

    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, yTextureId);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, yuvFrame->linesize[0], yuvFrame->height, 0,
                 GL_LUMINANCE, GL_UNSIGNED_BYTE, yuvFrame->data[0]);

    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D, uTextureId);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, yuvFrame->linesize[1], yuvFrame->height / 2, 0,
                 GL_LUMINANCE, GL_UNSIGNED_BYTE, yuvFrame->data[1]);

    glActiveTexture(GL_TEXTURE2);
    glBindTexture(GL_TEXTURE_2D, vTextureId);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, yuvFrame->linesize[2], yuvFrame->height / 2, 0,
                 GL_LUMINANCE, GL_UNSIGNED_BYTE, yuvFrame->data[2]);

    glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

    glDisableVertexAttribArray(positionHandle);
    glDisableVertexAttribArray(textureCoordsHandle);
    glBindTexture(GL_TEXTURE_2D, 0);

    glUseProgram(0);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    return mFrameBufferTextures[0];
}

void YUV420PImageInputFilter::initFrameBuffer(int width, int height) {
    if (mFrameBuffers[0] != GL_NONE && (mFrameWidth != width || mFrameHeight != height)) {
        destroyFrameBuffer();
    }
    if (mFrameBuffers[0] == GL_NONE) {
        mFrameWidth = width;
        mFrameHeight = height;
        createFrameBuffer(mFrameBuffers, mFrameBufferTextures, width, height);
    }
}

void YUV420PImageInputFilter::destroyFrameBuffer() {
    if (mFrameBufferTextures[0] != GL_NONE) {
        glDeleteTextures(1, mFrameBufferTextures);
        mFrameBufferTextures[0] = GL_NONE;
    }
    if (mFrameBuffers[0] != GL_NONE) {
        glDeleteFramebuffers(1, mFrameBuffers);
        mFrameBuffers[0] = GL_NONE;
    }
    mFrameWidth = -1;
    mFrameHeight = -1;
}

void YUV420PImageInputFilter::release(void) {
    if (vertexCoordinates != NULL) {
        delete vertexCoordinates;
        vertexCoordinates = NULL;
    }

    if (textureCoordinates != NULL) {
        delete textureCoordinates;
        textureCoordinates = NULL;
    }

    // 释放program句柄
    if (programHandle >= 0) {
        glDeleteProgram(programHandle);
        programHandle = -1;
    }

    // 删除Texture
    glDeleteTextures(1, &yTextureId);
    glDeleteTextures(1, &uTextureId);
    glDeleteTextures(1, &vTextureId);
}

void YUV420PImageInputFilter::initCoordinates() {
    vertexCoordinates = new float[12] {
            1.0f,  -1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f,
            1.0f,   1.0f, 0.0f,
            -1.0f,  1.0f, 0.0f
    };

    textureCoordinates = new float[8] {
            1.0f, 0.0f, // 右下
            0.0f, 0.0f, // 左下
            1.0f, 1.0f, // 右上
            0.0f, 1.0f  // 左上
    };
}
