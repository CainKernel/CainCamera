//
// Created by cain on 2019/1/9.
//

#include "GLUtils.h"

GLuint GLUtils::createProgram(const char *vertexShader, const char *fragShader) {
    GLuint vertex;
    GLuint fragment;
    GLuint program;
    GLint linked;

    //加载顶点shader
    vertex = loadShader(GL_VERTEX_SHADER, vertexShader);
    if (vertex == 0) {
        return 0;
    }
    // 加载片元着色器
    fragment = loadShader(GL_FRAGMENT_SHADER, fragShader);
    if (fragment == 0) {
        glDeleteShader(vertex);
        return 0;
    }
    // 创建program
    program = glCreateProgram();
    if (program == 0) {
        glDeleteShader(vertex);
        glDeleteShader(fragment);
        return 0;
    }
    // 绑定shader
    glAttachShader(program, vertex);
    glAttachShader(program, fragment);

    // 链接program程序
    glLinkProgram(program);
    // 检查链接状态
    glGetProgramiv(program, GL_LINK_STATUS, &linked);
    if (!linked) {
        GLint infoLen = 0;
        // 检查日志信息长度
        glGetProgramiv(program, GL_INFO_LOG_LENGTH, &infoLen);
        if (infoLen > 1) {
            // 分配一个足以存储日志信息的字符串
            char *infoLog = (char *) malloc(sizeof(char) * infoLen);
            // 检索日志信息
            glGetProgramInfoLog(program, infoLen, NULL, infoLog);
            ALOGE("Error linking program:\n%s\n", infoLog);
            // 使用完成后需要释放字符串分配的内存
            free(infoLog);
        }
        // 删除着色器释放内存
        glDeleteShader(vertex);
        glDeleteShader(fragment);
        glDeleteProgram(program);
        return 0;
    }
    // 删除着色器释放内存
    glDeleteShader(vertex);
    glDeleteShader(fragment);

    return program;
}

GLuint GLUtils::loadShader(GLenum type, const char *shaderSrc) {
    GLuint shader;
    GLint compiled;
    // 创建shader
    shader = glCreateShader(type);
    if (shader == 0) {
        return 0;
    }
    // 加载着色器的源码
    glShaderSource(shader, 1, &shaderSrc, NULL);

    // 编译源码
    glCompileShader(shader);

    // 检查编译状态
    glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);

    if (!compiled) {
        GLint infoLen = 0;
        // 查询日志的长度判断是否有日志产生
        glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);

        if (infoLen > 1) {
            // 分配一个足以存储日志信息的字符串
            char *infoLog = (char *) malloc(sizeof(char) * infoLen);
            // 检索日志信息
            glGetShaderInfoLog(shader, infoLen, NULL, infoLog);
            ALOGE("Error compiling shader:\n%s\n", infoLog);
            // 使用完成后需要释放字符串分配的内存
            free(infoLog);
        }
        // 删除编译出错的着色器释放内存
        glDeleteShader(shader);
        return 0;
    }
    return shader;
}

void GLUtils::checkActiveUniform(GLuint program) {
    GLint maxLen;
    GLint numUniforms;
    char *uniformName;

    glGetProgramiv(program, GL_ACTIVE_UNIFORMS, &numUniforms);
    glGetProgramiv(program, GL_ACTIVE_ATTRIBUTE_MAX_LENGTH, &maxLen);

    uniformName = (char *) malloc(sizeof(char) * maxLen);

    for (int i = 0; i < numUniforms; ++i) {
        GLint size;
        GLenum type;
        GLint location;

        glGetActiveUniform(program, i, maxLen, NULL, &size, &type, uniformName);

        location = glGetUniformLocation(program, uniformName);

        ALOGD("location:", location);

        switch (type) {
            case GL_FLOAT: {
                ALOGD("type : GL_FLOAT");
                break;
            }
            case GL_FLOAT_VEC2: {
                ALOGD("type : GL_FLOAT_VEC2");
                break;
            }
            case GL_FLOAT_VEC3: {
                ALOGD("type : GL_FLOAT_VEC3");
                break;
            }
            case GL_FLOAT_VEC4: {
                ALOGD("type : GL_FLOAT_VEC4");
                break;
            }
            case GL_INT: {
                ALOGD("type : GL_INT");
                break;
            }
        }
    }
}

GLuint GLUtils::createTexture(GLenum type) {
    GLuint textureId;
    // 设置解包对齐
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
    // 创建纹理
    glGenTextures(1, &textureId);
    // 绑定纹理
    glBindTexture(type, textureId);
    // 设置放大缩小模式
    glTexParameterf(type, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameterf(type, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(type, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameterf(type, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    return textureId;
}

GLuint GLUtils::createTextureWithBytes(unsigned char *bytes, int width, int height) {
    GLuint textureId;
    if (bytes == NULL) {
        return 0;
    }
    // 创建Texture
    glGenTextures(1, &textureId);
    // 绑定类型
    glBindTexture(GL_TEXTURE_2D, textureId);
    // 利用像素创建texture
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, bytes);
    // 设置放大缩小模式
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    return textureId;
}

GLuint
GLUtils::createTextureWithOldTexture(GLuint texture, unsigned char *bytes, int width, int height) {
    if (texture == 0) {
        return createTextureWithBytes(bytes, width, height);
    }
    // 绑定到当前的Texture
    glBindTexture(GL_TEXTURE_2D, texture);
    // 更新Texture数据
    glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, GL_NONE, GL_TEXTURE_2D, bytes);
    return texture;
}

void GLUtils::createFrameBuffer(GLuint *framebuffer, GLuint *texture, int width, int height) {
    createFrameBuffers(framebuffer, texture, width, height, 1);
}

void GLUtils::createFrameBuffers(GLuint *frambuffers, GLuint *textures, int width, int height,
                                 int size) {
    // 创建FBO
    glGenFramebuffers(size, frambuffers);
    // 创建Texture
    glGenTextures(size, textures);
    for (int i = 0; i < size; ++i) {
        // 绑定Texture
        glBindTexture(GL_TEXTURE_2D, textures[i]);
        // 创建一个没有像素的的Texture
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, NULL);
        // 设置放大缩小模式
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        // 创建完成后需要解绑
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
}

void GLUtils::checkGLError(const char *op) {
    for (GLint error = glGetError(); error; error = glGetError()) {
        ALOGE("[GLES2] after %s() glError (0x%x)\n", op, error);
    }
}
