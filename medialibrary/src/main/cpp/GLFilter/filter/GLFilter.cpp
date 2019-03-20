//
// Created by CainHuang on 2019/3/13.
//

#include <cstdlib>
#include "../common/OpenGLUtils.h"
#include "GLFilter.h"
#include <AndroidLog.h>

GLFilter::GLFilter() : initialized(false), programHandle(-1), positionHandle(-1), texCoordHandle(-1),
                   inputTextureHandle(-1), vertexCount(4), timeStamp(0), intensity(1.0),
                   textureWidth(0), textureHeight(0), displayWidth(0), displayHeight(0) {

}

GLFilter::~GLFilter() {

}

void GLFilter::initProgram() {
    if (!isInitialized()) {
        initProgram(kDefaultVertexShader.c_str(), kDefaultFragmentShader.c_str());
    }
}

void GLFilter::initProgram(const char *vertexShader, const char *fragmentShader) {
    if (isInitialized()) {
        return;
    }
    if (vertexShader && fragmentShader) {
        programHandle = OpenGLUtils::createProgram(vertexShader, fragmentShader);
        OpenGLUtils::checkGLError("createProgram");
        positionHandle = glGetAttribLocation(programHandle, "aPosition");
        texCoordHandle = glGetAttribLocation(programHandle, "aTextureCoord");
        inputTextureHandle = glGetUniformLocation(programHandle, "inputTexture");
        setInitialized(true);
    } else {
        positionHandle = -1;
        positionHandle = -1;
        inputTextureHandle = -1;
        setInitialized(false);
    }
}

void GLFilter::destroyProgram() {
    if (initialized) {
        glDeleteProgram(programHandle);
    }
    programHandle = -1;
}

void GLFilter::setInitialized(bool initialized) {
    this->initialized = initialized;
}

bool GLFilter::isInitialized() {
    return initialized;
}

void GLFilter::setTextureSize(int width, int height) {
    this->textureWidth = width;
    this->textureHeight = height;
}

void GLFilter::setDisplaySize(int width, int height) {
    this->displayWidth = width;
    this->displayHeight = height;
}

void GLFilter::setTimeStamp(double timeStamp) {
    this->timeStamp = timeStamp;
}

void GLFilter::setIntensity(float intensity) {
    this->intensity = intensity;
}

void GLFilter::updateViewPort() {
    if (displayWidth != 0 && displayHeight != 0) {
        glViewport(0, 0, displayWidth, displayHeight);
    } else {
        glViewport(0, 0, textureWidth, textureHeight);
    }
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);
}

void GLFilter::drawTexture(GLuint texture, float *vertices, float *textureVertices, bool viewPortUpdate) {
    if (!isInitialized() || texture < 0) {
        return;
    }

    if (viewPortUpdate) {
        updateViewPort();
    }

    // 绑定program
    glUseProgram(programHandle);
    // 绑定纹理
    bindTexture(texture);
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
}

void GLFilter::drawTexture(FrameBuffer *frameBuffer, GLuint texture, float *vertices,
                           float *textureVertices) {
    if (frameBuffer) {
        frameBuffer->bindBuffer();
    }
    drawTexture(texture, vertices, textureVertices, false);
    if (frameBuffer) {
        frameBuffer->unbindBuffer();
    }
}

void GLFilter::bindAttributes(float *vertices, float *textureVertices) {
    // 绑定顶点坐标
    glVertexAttribPointer(positionHandle, 2, GL_FLOAT, GL_FALSE, 0, vertices);
    glEnableVertexAttribArray(positionHandle);

    // 绑定纹理坐标
    glVertexAttribPointer(texCoordHandle, 2, GL_FLOAT, GL_FALSE, 0, textureVertices);
    glEnableVertexAttribArray(texCoordHandle);

}

void GLFilter::bindTexture(GLuint texture) {
    // 绑定纹理
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(getTextureType(), texture);
    glUniform1i(inputTextureHandle, 0);
}

void GLFilter::onDrawBegin() {
    // do nothing
}

void GLFilter::onDrawAfter() {
    // do nothing
}

void GLFilter::onDrawFrame() {
    glDrawArrays(GL_TRIANGLE_STRIP, 0, vertexCount);
}

void GLFilter::unbindAttributes() {
    glDisableVertexAttribArray(texCoordHandle);
    glDisableVertexAttribArray(positionHandle);
}

void GLFilter::unbindTextures() {
    glBindTexture(getTextureType(), 0);
}

GLenum GLFilter::getTextureType() {
    return GL_TEXTURE_2D;
}


