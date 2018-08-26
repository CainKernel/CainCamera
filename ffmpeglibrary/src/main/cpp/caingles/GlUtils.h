//
// Created by Administrator on 2018/2/7.
//

#ifndef CAINCAMERA_GLUTILS_H
#define CAINCAMERA_GLUTILS_H

#ifdef __cplusplus
extern "C" {
#endif

#include <stdio.h>
#include <stdlib.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <GLES2/gl2platform.h>

#include "GlShaders.h"
#include "../common/AndroidLog.h"

#define PI 3.1415926535897932384626433832795f
// 矩阵
typedef struct {
    GLfloat m[16];
} ESMatrix;


// 创建program
GLuint createProgram(const char *vertexShader, const char *fragShader);

// 加载shader
GLuint loadShader(GLenum type, const char* shaderSrc);

// 查询活动的统一变量uniform
void checkActiveUniform(GLuint program);

// 创建texture
GLuint createTexture(GLenum type);

// 创建texture
GLuint createTextureWithBytes(unsigned char* bytes, int width, int height);

// 使用旧的Texture 创建新的Texture
GLuint createTextureWithOldTexture(GLuint texture, unsigned char* bytes, int width, int height);

// 创建一个FBO和Texture
void createFrameBuffer(GLuint *framebuffer, GLuint* texture, int width, int height);

// 创建FBO和Texture
void createFrameBuffers(GLuint* frambuffers, GLuint* textures, int width, int height, int size);

// 检查是否出错
void checkGLError(const char * op);
// -------------------------------------------- matrix部分 -----------------------------------------
// 缩放
void scaleM(ESMatrix *result, int offset, GLfloat sx, GLfloat sy, GLfloat sz);

// 平移
void translateM(ESMatrix *result, int offset, GLfloat x, GLfloat y, GLfloat z);


// 旋转
void rotateM(ESMatrix *result, GLfloat angle, GLfloat x, GLfloat y, GLfloat z);

// 正交投影矩阵
int orthoM(ESMatrix *result, int mOffset, float left, float right,
           float bottom, float top, float near, float far);

// 视锥体
int frustumM(ESMatrix *result, int offset, float left, float right,
             float bottom, float top, float near, float far);

// 透视矩阵
int perspectiveM(ESMatrix *result, int offset,
                 float fovy, float aspect, float zNear, float zFar);

// 产生一个单位矩阵
void setIdentityM(ESMatrix *result);

// 矩阵相乘
void multiplyMM(ESMatrix *result, ESMatrix *lhs, ESMatrix * rhs);

#ifdef __cplusplus
}
#endif

#endif //CAINCAMERA_GLUTILS_H
