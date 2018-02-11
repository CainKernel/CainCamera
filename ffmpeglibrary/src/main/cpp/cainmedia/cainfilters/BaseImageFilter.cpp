//
// Created by cain on 2018/2/11.
//

#include "BaseImageFilter.h"

const char* BaseImageFilter::getVertexShader() {
    return vertex_shader;
}

const char* BaseImageFilter::getFragmentShader() {
    return fragment_shader;
}

/**
 * 初始化句柄
 */
void BaseImageFilter::initHandle() {
    if (programHandle == GL_NONE) {
        ALOGE("program is empty!");
        return;
    }
    positionHandle = glGetAttribLocation(programHandle, "aPosition");
    textureCoordsHandle = glGetAttribLocation(programHandle, "aTextureCoord");
    mvpMatrixHandle = glGetAttribLocation(programHandle, "uMVPMatrix");
    inputTextureHandle = glGetAttribLocation(programHandle, "inputTexture");
}

/**
 * 绑定值
 * @param texture
 * @param vertices
 * @param textureCoords
 */
void BaseImageFilter::bindValue(GLint texture, GLfloat *vertices, GLfloat *textureCoords) {
    glVertexAttribPointer(positionHandle, 2, GL_FLOAT, GL_FALSE, 0, vertices);
    glEnableVertexAttribArray(positionHandle);

    glVertexAttribPointer(textureCoordsHandle, 2, GL_FLOAT, GL_FALSE, 0, textureCoords);
    glEnableVertexAttribArray(textureCoordsHandle);

    glUniform4fv(mvpMatrixHandle, 1, mvpMatrix->m);

    glActiveTexture(GL_TEXTURE0);
    glUniform1i(inputTextureHandle, 0);
}

/**
 * 解绑
 */
void BaseImageFilter::unbindValue() {
    glDisableVertexAttribArray(positionHandle);
    glDisableVertexAttribArray(textureCoordsHandle);
    glBindTexture(GL_TEXTURE_2D, 0);
}

TextureType BaseImageFilter::getTextureType() {
    return type;
}

/**
 * 渲染之前的操作
 */
void BaseImageFilter::onDrawBegin() {

}

/**
 * 渲染之后的操作
 */
void BaseImageFilter::onDrawAfter() {

}

/**
 * 释放资源
 */
void BaseImageFilter::release() {
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
void BaseImageFilter::initCoordinates() {
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
 * 构造器
 */
BaseImageFilter::BaseImageFilter() {
    BaseImageFilter(getVertexShader(), getFragmentShader());
}

/**
 * 构造器
 * @param vertexShader
 * @param fragmentShader
 */
BaseImageFilter::BaseImageFilter(const char *vertexShader, const char *fragmentShader) {
    programHandle = createProgram(vertexShader, fragmentShader);
    initHandle();
    initIdentityMatrix();
    initCoordinates();
}

/**
 * 析构
 */
BaseImageFilter::~BaseImageFilter() {
    release();
}

/**
 * 输入大小发生变化
 * @param width
 * @param height
 */
void BaseImageFilter::onInputSizeChanged(int width, int height) {
    textureWidth = width;
    textureHeight = height;
}

/**
 * 界面大小发生变化
 * @param width
 * @param height
 */
void BaseImageFilter::onDisplayChanged(int width, int height) {
    displayWidth = width;
    displayHeight = height;
}

/**
 * 渲染
 * @param texture
 * @return
 */
bool BaseImageFilter::drawFrame(int texture) {
    return drawFrame(texture, vertexCoordinates, textureCoordinates);
}

/**
 * 渲染
 * @param texture
 * @param vertices
 * @param textureCoords
 * @return
 */
bool BaseImageFilter::drawFrame(int texture, GLfloat *vertices, GLfloat *textureCoords) {
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
void BaseImageFilter::initIdentityMatrix() {
    setIdentityM(mvpMatrix);
}

/**
 * 设置总变换矩阵
 * @param matrix
 */
void BaseImageFilter::setMVPMatrix(ESMatrix *matrix) {
    ESMatrix *temp = mvpMatrix;
    mvpMatrix = matrix;
    free(temp);
}

/**
 * 设置渲染类型
 * @param type
 */
void BaseImageFilter::setTextureType(TextureType type) {
    this->type = type;
}