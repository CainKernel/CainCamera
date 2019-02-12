//
// Created by cain on 2019/1/9.
//

#ifndef GLUTILS_H
#define GLUTILS_H

#include <stdio.h>
#include <stdlib.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <GLES2/gl2platform.h>

#include <AndroidLog.h>



class GLUtils {
public:
    // 创建program
    static GLuint createProgram(const char *vertexShader, const char *fragShader);

    // 加载shader
    static GLuint loadShader(GLenum type, const char* shaderSrc);

    // 查询活动的统一变量uniform
    static void checkActiveUniform(GLuint program);

    // 创建texture
    static GLuint createTexture(GLenum type);

    // 创建texture
    static GLuint createTextureWithBytes(unsigned char* bytes, int width, int height);

    // 使用旧的Texture 创建新的Texture
    static GLuint createTextureWithOldTexture(GLuint texture, unsigned char* bytes, int width, int height);

    // 创建一个FBO和Texture
    static void createFrameBuffer(GLuint *framebuffer, GLuint* texture, int width, int height);

    // 创建FBO和Texture
    static void createFrameBuffers(GLuint* frambuffers, GLuint* textures, int width, int height, int size);

    // 检查是否出错
    static void checkGLError(const char * op);

private:
    GLUtils() = default;
    virtual ~GLUtils(){}
};


#endif //GLUTILS_H
