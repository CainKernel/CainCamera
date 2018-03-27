//
// Created by Administrator on 2018/3/21.
//

#include "YUV420PImageFilter.h"
#include "caingles/GlShaders.h"


YUV420PImageFilter::YUV420PImageFilter() {
    YUV420PImageFilter(getVertexShader(), getFragmentShader());
}

YUV420PImageFilter::YUV420PImageFilter(const char *vertexShader, const char *fragmentShader) {
    programHandle = createProgram(vertexShader, fragmentShader);
    initHandle();
    initIdentityMatrix();
    initCoordinates();
}

YUV420PImageFilter::~YUV420PImageFilter() {
    release();
}

/**
 * 输入大小发生变化
 * @param width
 * @param height
 */
void YUV420PImageFilter::onInputSizeChanged(int width, int height) {
    videoWidth = width;
    videoHeight = height;
    initTexture(width, height);
}

/**
 * 初始化Texture
 * @param width
 * @param height
 */
void YUV420PImageFilter::initTexture(int width, int height) {
    glGenTextures(1, &mTextureId[0]);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, mTextureId[0]);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,
                    GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, width, height, 0,
                 GL_LUMINANCE, GL_UNSIGNED_BYTE, NULL);
    glUniform1i(inputTextureHandle[0], 0);

    glGenTextures(1, &mTextureId[1]);
    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D, mTextureId[1]);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,
                    GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, width/2, height/2, 0,
                 GL_LUMINANCE, GL_UNSIGNED_BYTE, NULL);
    glUniform1i(inputTextureHandle[1], 1);


    glGenTextures(1, &mTextureId[2]);
    glActiveTexture(GL_TEXTURE2);
    glBindTexture(GL_TEXTURE_2D, mTextureId[2]);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,
                    GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, width/2, height/2, 0,
                 GL_LUMINANCE, GL_UNSIGNED_BYTE, NULL);
    glUniform1i(inputTextureHandle[2], 2);
}

/**
 * 显示发生改变
 * @param width
 * @param height
 */
void YUV420PImageFilter::onDisplayChanged(int width, int height) {
    screenWidth = width;
    screenHeight = height;
    // 重新计算屏幕位置
    viewport();
}

/**
 * 绘制yuv
 * @param bufY
 * @param bufU
 * @param bufV
 * @return
 */
bool YUV420PImageFilter::drawFrame(void *bufY, void *bufU, void *bufV) {
    return drawFrame(bufY, bufU, bufV, vertexCoordinates, textureCoordinates);
}

/**
 * 绘制YUV
 * @param bufY
 * @param bufU
 * @param bufV
 * @param vertices
 * @param textureCoords
 * @return
 */
bool YUV420PImageFilter::drawFrame(void *bufY, void *bufU, void *bufV, GLfloat vertices[],
                                   GLfloat textureCoords[]) {

    if (programHandle < 0 || bufY == NULL || bufU == NULL || bufV == NULL) {
        return false;
    }
    glUseProgram(programHandle);

    glVertexAttribPointer(positionHandle, 2, GL_FLOAT, GL_FALSE, 0, vertices);
    glEnableVertexAttribArray(positionHandle);

    glVertexAttribPointer(textureCoordsHandle, 2, GL_FLOAT, GL_FALSE, 0, textureCoords);
    glEnableVertexAttribArray(textureCoordsHandle);

    glUniform4fv(mvpMatrixHandle, 1, mvpMatrix->m);

    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, mTextureId[0]);
    glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, videoWidth, videoHeight,
                    GL_LUMINANCE, GL_UNSIGNED_BYTE, bufY);

    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D, mTextureId[1]);
    glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, videoWidth / 2, videoWidth / 2,
                    GL_LUMINANCE, GL_UNSIGNED_BYTE, bufU);

    glActiveTexture(GL_TEXTURE2);
    glBindTexture(GL_TEXTURE_2D, mTextureId[2]);
    glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, videoWidth / 2, videoWidth / 2,
                    GL_LUMINANCE, GL_UNSIGNED_BYTE, bufV);

    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

    glDisableVertexAttribArray(positionHandle);
    glDisableVertexAttribArray(textureCoordsHandle);
    glBindTexture(GL_TEXTURE_2D, 0);

    glUseProgram(0);
    return true;
}

/**
 * 将视频帧绘制到FBO
 * @param bufY
 * @param bufU
 * @param bufV
 * @return
 */
int YUV420PImageFilter::drawFrameBuffer(void *bufY, void *bufU, void *bufV) {
    return drawFrameBuffer(bufY, bufU, bufV, vertexCoordinates, textureCoordinates);
}

/**
 * 将视频帧绘制到FBO
 * @param bufY
 * @param bufU
 * @param bufV
 * @param vertices
 * @param textureCoords
 * @return
 */
int YUV420PImageFilter::drawFrameBuffer(void *bufY, void *bufU, void *bufV, GLfloat *vertices,
                                        GLfloat *textureCoords) {
    if (mFrameBuffers[0] == GL_NONE) {
        return GL_NONE;
    }
    glViewport(0, 0, mFrameWidth, mFrameHeight);
    glBindFramebuffer(GL_FRAMEBUFFER, mFrameBuffers[0]);
    glUseProgram(programHandle);

    glVertexAttribPointer(positionHandle, 2, GL_FLOAT, GL_FALSE, 0, vertices);
    glEnableVertexAttribArray(positionHandle);

    glVertexAttribPointer(textureCoordsHandle, 2, GL_FLOAT, GL_FALSE, 0, textureCoords);
    glEnableVertexAttribArray(textureCoordsHandle);

    glUniform4fv(mvpMatrixHandle, 1, mvpMatrix->m);

    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, mTextureId[0]);
    glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, videoWidth, videoHeight,
                    GL_LUMINANCE, GL_UNSIGNED_BYTE, bufY);

    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D, mTextureId[1]);
    glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, videoWidth / 2, videoWidth / 2,
                    GL_LUMINANCE, GL_UNSIGNED_BYTE, bufU);

    glActiveTexture(GL_TEXTURE2);
    glBindTexture(GL_TEXTURE_2D, mTextureId[2]);
    glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, videoWidth / 2, videoWidth / 2,
                    GL_LUMINANCE, GL_UNSIGNED_BYTE, bufV);

    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

    glDisableVertexAttribArray(positionHandle);
    glDisableVertexAttribArray(textureCoordsHandle);
    glBindTexture(GL_TEXTURE_2D, 0);

    glUseProgram(0);
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    return mFrameBufferTextures[0];
}

/**
 * 计算viewport
 */
void YUV420PImageFilter::viewport() {
    int left,top;
    if (screenHeight > screenWidth) {
        left = 0;
        viewWidth = screenWidth;
        viewHeight = (int)(videoHeight * 1.0f / videoWidth * viewWidth);
        top = (screenHeight - viewHeight) / 2;
    } else {
        top = 0;
        viewHeight = screenHeight;
        viewWidth = (int)(videoWidth * 1.0f / videoHeight * viewHeight);
        left = (screenWidth - viewWidth) / 2;
    }
    glViewport(left, top, viewWidth, viewHeight);
}

/**
 * 初始化单位矩阵
 */
void YUV420PImageFilter::initIdentityMatrix() {
    setIdentityM(mvpMatrix);
}

/**
 * 设置MVP矩阵
 * @param matrix
 */
void YUV420PImageFilter::setMVPMatrix(ESMatrix *matrix) {
    ESMatrix *temp = mvpMatrix;
    mvpMatrix = matrix;
    free(temp);
}

/**
 * 获取vertex shader
 * @return
 */
const char *YUV420PImageFilter::getVertexShader(void) {
    return GlShader_GetShader(VERTEX_DEFAULT);
}

/**
 * 获取fragment shader
 * @return
 */
const char *YUV420PImageFilter::getFragmentShader(void) {
    return GlShader_GetShader(FRAGMENT_I420);
}

/**
 * 初始化句柄
 */
void YUV420PImageFilter::initHandle(void) {
    if (programHandle == GL_NONE) {
        ALOGE("program is empty!");
        return;
    }
    positionHandle = glGetAttribLocation(programHandle, "aPosition");
    textureCoordsHandle = glGetAttribLocation(programHandle, "aTextureCoord");
    mvpMatrixHandle = glGetAttribLocation(programHandle, "uMVPMatrix");
    inputTextureHandle[0] = glGetUniformLocation(programHandle, "inputTextureY");
    inputTextureHandle[1] = glGetUniformLocation(programHandle, "inputTextureU");
    inputTextureHandle[2] = glGetUniformLocation(programHandle, "inputTextureV");
}

// 释放资源
void YUV420PImageFilter::release(void) {
    if (programHandle >= 0) {
        glDeleteProgram(programHandle);
        programHandle = -1;
    }
    if (mvpMatrix) {
        free(mvpMatrix);
        mvpMatrix = NULL;
    }
    // 删除Texture
    glDeleteTextures(3, mTextureId);
}

// 初始化坐标
void YUV420PImageFilter::initCoordinates() {
// 初始化顶点坐标
    // 0 bottom left
    vertexCoordinates[0] = -1.0f;
    vertexCoordinates[1] = -1.0f;
    // 1 bottom right
    vertexCoordinates[2] = 1.0f;
    vertexCoordinates[3] = -1.0f;
    // 2 top left
    vertexCoordinates[4] = -1.0f;
    vertexCoordinates[5] = 1.0f;
    // 3 top right
    vertexCoordinates[6] = 1.0f;
    vertexCoordinates[7] = 1.0f;

    // 初始化纹理坐标
    // 0 bottom left
    textureCoordinates[0] = 0.0f;
    textureCoordinates[1] = 0.0f;
    // 1 bottom right
    textureCoordinates[2] = 1.0f;
    textureCoordinates[3] = 0.0f;
    // 2 top left
    textureCoordinates[4] = 0.0f;
    textureCoordinates[5] = 1.0f;
    // 3 top right
    textureCoordinates[6] = 1.0f;
    textureCoordinates[7] = 1.0f;
}


/**
 * 初始化FBO
 * @param width
 * @param height
 */
void YUV420PImageFilter::initFrameBuffer(int width, int height) {
    if (mFrameBuffers[0] != GL_NONE && (mFrameWidth != width || mFrameHeight != height)) {
        destroyFrameBuffer();
    }
    if (mFrameBuffers[0] == GL_NONE) {
        mFrameWidth = width;
        mFrameHeight = height;
        createFrameBuffer(mFrameBuffers, mFrameBufferTextures, width, height);
    }
}

/**
 * 销毁FBO
 */
void YUV420PImageFilter::destroyFrameBuffer() {
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
