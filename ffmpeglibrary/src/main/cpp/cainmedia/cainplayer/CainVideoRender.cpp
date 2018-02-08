//
// Created by Administrator on 2018/2/7.
//

#include "CainVideoRender.h"


/**
 * 获取ViertexShader
 * @return
 */
const char* CainVideoRender::getVertexShader() {
    return vertex_shader;
}

/**
 * 获取FragmentShader
 * @return
 */
const char* CainVideoRender::getFragmentShader() {
    return fragment_shader;
}

/**
 * 初始化句柄
 */
void CainVideoRender::initHandle() {
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
void CainVideoRender::bindValue(GLint texture, GLfloat *vertices, GLfloat *textureCoords) {
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
void CainVideoRender::unbindValue() {
    glDisableVertexAttribArray(positionHandle);
    glDisableVertexAttribArray(textureCoordsHandle);
    glBindTexture(GL_TEXTURE_2D, 0);
}

/**
 * 获取Texture类型,YUV/RGB
 * @return
 */
TextureType CainVideoRender::getTextureType() {
    return type;
}

/**
 * 渲染之前
 */
void CainVideoRender::onDrawBegin() {

}

/**
 * 渲染之后
 */
void CainVideoRender::onDrawAfter() {

}

/**
 * 释放
 */
void CainVideoRender::release() {
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
 * 初始化坐标
 */
void CainVideoRender::initCoordinates() {
    // bottom left
    vertexCoordinates[0] = -1.0f;
    vertexCoordinates[1] = -1.0f;
    // bottom right
    vertexCoordinates[2] = 1.0f;
    vertexCoordinates[3] = -1.0f;
    // top left
    vertexCoordinates[4] = -1.0f;
    vertexCoordinates[5] = 1.0f;
    // top right
    vertexCoordinates[6] = 1.0f;
    vertexCoordinates[7] = 1.0f;

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
CainVideoRender::CainVideoRender() {
    CainVideoRender(getVertexShader(), getFragmentShader());
}

/**
 * 构造器
 * @param vertexShader
 * @param fragmentShader
 */
CainVideoRender::CainVideoRender(const char *vertexShader, const char *fragmentShader) {
    programHandle = createProgram(vertexShader, fragmentShader);
    initHandle();
    initIdentityMatrix();
    initCoordinates();
}

/**
 * 析构
 */
CainVideoRender::~CainVideoRender() {
    release();
}

/**
 * 输入大小发生变化
 * @param width
 * @param height
 */
void CainVideoRender::onInputSizeChanged(int width, int height) {
    videoWidth = width;
    videoHeight = height;
}

/**
 * 预览大小发生变化
 * @param width
 * @param height
 */
void CainVideoRender::onDisplaySizeChanged(int width, int height) {
    displayWidth = width;
    displayHeight = height;
}

/**
 * 渲染视频帧
 * @param textureId
 * @return
 */
bool CainVideoRender::drawFrame(int textureId) {
    return drawFrame(textureId, vertexCoordinates, textureCoordinates);
}

/**
 * 渲染视频
 * @return
 */
bool CainVideoRender::drawFrame(int textureId, GLfloat verticex[], GLfloat textureCoords[]) {
    if (textureId < 0 || programHandle < 0) {
        return false;
    }
    glUseProgram(programHandle);
    bindValue(textureId, verticex, textureCoords);
    onDrawBegin();
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    onDrawAfter();
    glUseProgram(0);
    return true;
}

/**
 * 初始化单位矩阵
 */
void CainVideoRender::initIdentityMatrix() {
    setIdentityM(mvpMatrix);
}

/**
 * 设置总变换矩阵
 * @param matrix
 */
void CainVideoRender::setMVPMatrix(ESMatrix *matrix) {
    mvpMatrix = matrix;
}

/**
 * 设置渲染器类型
 * @param type
 */
void CainVideoRender::setTextureType(TextureType type) {
    this->type = type;
}
