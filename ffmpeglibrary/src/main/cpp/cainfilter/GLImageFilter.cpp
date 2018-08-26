//
// Created by cain on 2018/2/11.
//

#include "GLImageFilter.h"

/**
 * 构造器
 */
GLImageFilter::GLImageFilter() {
    GLImageFilter(getVertexShader(), getFragmentShader());
}

/**
 * 构造器
 * @param vertexShader
 * @param fragmentShader
 */
GLImageFilter::GLImageFilter(const char *vertexShader, const char *fragmentShader) {
    programHandle = createProgram(vertexShader, fragmentShader);
    initHandle();
    initIdentityMatrix();
    initCoordinates();
    textureWidth = 0;
    textureHeight = 0;
    displayWidth = 0;
    displayHeight = 0;
}

/**
 * 析构
 */
GLImageFilter::~GLImageFilter() {
    release();
}

const char* GLImageFilter::getVertexShader() {
    return GlShader_GetShader(VERTEX_DEFAULT);
}

const char* GLImageFilter::getFragmentShader() {
    return GlShader_GetShader(FRAGMENT_ABGR);
}

/**
 * 初始化句柄
 */
void GLImageFilter::initHandle() {
    if (programHandle == GL_NONE) {
        ALOGE("program is empty!");
        return;
    }
    positionHandle = glGetAttribLocation(programHandle, "aPosition");
    textureCoordsHandle = glGetAttribLocation(programHandle, "aTextureCoord");
    mvpMatrixHandle = glGetAttribLocation(programHandle, "uMVPMatrix");
    inputTextureHandle = glGetUniformLocation(programHandle, "inputTexture");
}

/**
 * 绑定值
 * @param texture
 * @param vertices
 * @param textureCoords
 */
void GLImageFilter::bindValue(GLint texture, GLfloat *vertices, GLfloat *textureCoords) {
    glVertexAttribPointer(positionHandle, 2, GL_FLOAT, GL_FALSE, 0, vertices);
    glEnableVertexAttribArray(positionHandle);

    glVertexAttribPointer(textureCoordsHandle, 2, GL_FLOAT, GL_FALSE, 0, textureCoords);
    glEnableVertexAttribArray(textureCoordsHandle);

    glUniform4fv(mvpMatrixHandle, 1, mvpMatrix->m);

    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, texture);
    glUniform1i(inputTextureHandle, 0);
}

/**
 * 解绑
 */
void GLImageFilter::unbindValue() {
    glDisableVertexAttribArray(positionHandle);
    glDisableVertexAttribArray(textureCoordsHandle);
    glBindTexture(GL_TEXTURE_2D, 0);
}

/**
 * 渲染之前的操作
 */
void GLImageFilter::onDrawBegin() {

}

/**
 * 渲染之后的操作
 */
void GLImageFilter::onDrawAfter() {

}

/**
 * 释放资源
 */
void GLImageFilter::release() {
    // MTK有些设备的创建得到的program是从0开始的
    if (programHandle >= 0) {
        glDeleteProgram(programHandle);
        programHandle = -1;
    }
    if (mvpMatrix) {
        free(mvpMatrix);
        mvpMatrix = NULL;
    }
}

/**
 * 初始化坐标缓冲
 */
void GLImageFilter::initCoordinates() {
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
 * 输入大小发生变化
 * @param width
 * @param height
 */
void GLImageFilter::onInputSizeChanged(int width, int height) {
    textureWidth = width;
    textureHeight = height;
}

/**
 * 界面大小发生变化
 * @param width
 * @param height
 */
void GLImageFilter::onDisplaySizeChanged(int width, int height) {
    displayWidth = width;
    displayHeight = height;
}

/**
 * 渲染
 * @param texture
 * @return
 */
bool GLImageFilter::drawFrame(int texture) {
    return drawFrame(texture, vertexCoordinates, textureCoordinates);
}

/**
 * 渲染
 * @param texture
 * @param vertices
 * @param textureCoords
 * @return
 */
bool GLImageFilter::drawFrame(int texture, GLfloat *vertices, GLfloat *textureCoords) {
    if (texture < 0 || programHandle < 0) {
        return false;
    }
    glUseProgram(programHandle);
    bindValue(texture, vertices, textureCoords);
    onDrawBegin();
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    onDrawAfter();
    unbindValue();
    glUseProgram(0);
    return true;
}

/**
 * 初始化单位矩阵
 */
void GLImageFilter::initIdentityMatrix() {
    setIdentityM(mvpMatrix);
}

/**
 * 设置总变换矩阵
 * @param matrix
 */
void GLImageFilter::setMVPMatrix(ESMatrix *matrix) {
    ESMatrix *temp = mvpMatrix;
    mvpMatrix = matrix;
    free(temp);
}