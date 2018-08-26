//
// Created by Administrator on 2018/2/2.
//

#include "GlUtils.h"

#ifdef __cplusplus
extern "C" {
#endif

#include <string.h>
#include <math.h>

/**
 * 创建program
 * @param vertexShader
 * @param fragShader
 * @return
 */
GLuint createProgram(const char *vertexShader, const char *fragShader) {
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

/**
 * 加载shader文件
 * @param type
 * @param shaderSrc
 * @return
 */
GLuint loadShader(GLenum type, const char *shaderSrc) {
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

/**
 * 查询活动的统一变量uniform
 * @param program
 */
void checkActiveUniform(const GLuint program) {
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
            case GL_FLOAT:
                ALOGD("type : GL_FLOAT");
                break;
            case GL_FLOAT_VEC2:
                ALOGD("type : GL_FLOAT_VEC2");
                break;
            case GL_FLOAT_VEC3:
                ALOGD("type : GL_FLOAT_VEC3");
                break;
            case GL_FLOAT_VEC4:
                ALOGD("type : GL_FLOAT_VEC4");
                break;
            case GL_INT:
                ALOGD("type : GL_INT");
                break;
        }
    }
}

/**
 * 创建Texture
 * @param type texture类型，OES或者sampler2D
 * @return
 */
GLuint createTexture(GLenum type) {
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

/**
 * 创建texture
 * @param bytes
 * @param width
 * @param height
 * @return
 */
GLuint createTextureWithBytes(unsigned char *bytes, int width, int height) {
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

/**
 * 使用旧的Texture 创建新的Texture，宽高应小于等于旧的texture，最好是相等
 * 一般用于刷新视频帧这样的情形
 * @param texture
 * @param bytes
 * @return
 */
GLuint createTextureWithOldTexture(GLuint texture, unsigned char *bytes, int width, int height) {
    if (texture == 0) {
        return createTextureWithBytes(bytes, width, height);
    }
    // 绑定到当前的Texture
    glBindTexture(GL_TEXTURE_2D, texture);
    // 更新Texture数据
    glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, GL_NONE, GL_TEXTURE_2D, bytes);
    return texture;
}

// 创建一个FBO和Texture
void createFrameBuffer(GLuint *framebuffer, GLuint *texture, int width, int height) {
    createFrameBuffers(framebuffer, texture, width, height, 1);
}

/**
 * 创建size 个 FBO和Texture
 * @param frambuffers
 * @param textures
 * @param width
 * @param height
 * @param size
 */
void createFrameBuffers(GLuint *frambuffers, GLuint *textures, int width, int height, int size) {
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

/**
 * 检查是否出错
 * @param op
 */
void checkGLError(const char *op) {
    for (GLint error = glGetError(); error; error = glGetError()) {
        ALOGE("[GLES2] after %s() glError (0x%x)\n", op, error);
    }
}

// -------------------------------------------- matrix部分 -----------------------------------------
/**
 *  缩放
 * @param result
 * @param offset
 * @param sx
 * @param sy
 * @param sz
 */
void scaleM(ESMatrix *result, int offset, GLfloat sx, GLfloat sy, GLfloat sz) {
    if (result == NULL) {
        return;
    }
    for (int i = 0; i < 4; ++i) {
        int mi = offset + i;
        result->m[mi] *= sx;
        result->m[mi + 4] *= sy;
        result->m[mi + 8] *= sz;
    }
}

/**
 * 平移
 * @param result
 * @param offset
 * @param x
 * @param y
 * @param z
 */
void translateM(ESMatrix *result, int offset, GLfloat x, GLfloat y, GLfloat z) {
    for (int i = 0; i < 4; i++) {
        int mi = offset + i;
        result->m[12 + mi] += result->m[mi] * x + result->m[4 + mi] * y + result->m[8 + mi] * z;
    }
}


/**
 * 计算向量长度
 * @param x
 * @param y
 * @param z
 * @return
 */
static float length(float x, float y, float z) {
    return (float) sqrt(x * x + y * y + z * z);
}


/**
 * 设置旋转矩阵
 * @param result
 * @param rmOffset
 * @param a
 * @param x
 * @param y
 * @param z
 */
static void setRotateM(ESMatrix *result, int rmOffset,
                       float a, float x, float y, float z) {
    result->m[rmOffset + 3] = 0;
    result->m[rmOffset + 7] = 0;
    result->m[rmOffset + 11] = 0;
    result->m[rmOffset + 12] = 0;
    result->m[rmOffset + 13] = 0;
    result->m[rmOffset + 14] = 0;
    result->m[rmOffset + 15] = 1;
    a *= (float) (PI / 180.0f);
    float s = (float) sin(a);
    float c = (float) cos(a);
    if (1.0f == x && 0.0f == y && 0.0f == z) {
        result->m[rmOffset + 5] = c;
        result->m[rmOffset + 10] = c;
        result->m[rmOffset + 6] = s;
        result->m[rmOffset + 9] = -s;
        result->m[rmOffset + 1] = 0;
        result->m[rmOffset + 2] = 0;
        result->m[rmOffset + 4] = 0;
        result->m[rmOffset + 8] = 0;
        result->m[rmOffset + 0] = 1;
    } else if (0.0f == x && 1.0f == y && 0.0f == z) {
        result->m[rmOffset + 0] = c;
        result->m[rmOffset + 10] = c;
        result->m[rmOffset + 8] = s;
        result->m[rmOffset + 2] = -s;
        result->m[rmOffset + 1] = 0;
        result->m[rmOffset + 4] = 0;
        result->m[rmOffset + 6] = 0;
        result->m[rmOffset + 9] = 0;
        result->m[rmOffset + 5] = 1;
    } else if (0.0f == x && 0.0f == y && 1.0f == z) {
        result->m[rmOffset + 0] = c;
        result->m[rmOffset + 5] = c;
        result->m[rmOffset + 1] = s;
        result->m[rmOffset + 4] = -s;
        result->m[rmOffset + 2] = 0;
        result->m[rmOffset + 6] = 0;
        result->m[rmOffset + 8] = 0;
        result->m[rmOffset + 9] = 0;
        result->m[rmOffset + 10] = 1;
    } else {
        float len = length(x, y, z);
        if (1.0f != len) {
            float recipLen = 1.0f / len;
            x *= recipLen;
            y *= recipLen;
            z *= recipLen;
        }
        float nc = 1.0f - c;
        float xy = x * y;
        float yz = y * z;
        float zx = z * x;
        float xs = x * s;
        float ys = y * s;
        float zs = z * s;
        result->m[rmOffset + 0] = x * x * nc + c;
        result->m[rmOffset + 4] = xy * nc - zs;
        result->m[rmOffset + 8] = zx * nc + ys;
        result->m[rmOffset + 1] = xy * nc + zs;
        result->m[rmOffset + 5] = y * y * nc + c;
        result->m[rmOffset + 9] = yz * nc - xs;
        result->m[rmOffset + 2] = zx * nc - ys;
        result->m[rmOffset + 6] = yz * nc + xs;
        result->m[rmOffset + 10] = z * z * nc + c;
    }
}

/**
 * 旋转
 * @param result
 * @param angle
 * @param x
 * @param y
 * @param z
 */
void rotateM(ESMatrix *result, GLfloat angle, GLfloat x, GLfloat y, GLfloat z) {
    // 如果当前的矩阵为空，则重新创建一个
    if (result == NULL) {
        result = (ESMatrix *) malloc(sizeof(ESMatrix));
        memset(result, 0, sizeof(ESMatrix));
    }
    // 创建临时对象
    ESMatrix *temp = (ESMatrix *) malloc(sizeof(ESMatrix));
    memset(temp, 0, sizeof(ESMatrix));
    ESMatrix *ret = (ESMatrix *) malloc(sizeof(ESMatrix));
    memset(ret, 0, sizeof(ESMatrix));
    // 设置旋转矩阵
    setRotateM(temp, 0, angle, x, y, z);
    // 矩阵相乘
    multiplyMM(ret, result, temp);
    // 将得到的旋转结果复制给result
    for (int i = 0; i < 16; ++i) {
        result->m[i] = ret->m[i];
    }
    // 释放临时对象
    free(temp);
    free(ret);
}

/**
 * 正交投影矩阵
 * @param result
 * @param mOffset
 * @param left
 * @param right
 * @param bottom
 * @param top
 * @param near
 * @param far
 * @return
 */
int orthoM(ESMatrix *result, int mOffset,
           float left, float right, float bottom, float top,
           float near, float far) {
    if (result == NULL) {
        return -1;
    }
    if (left == right) {
        return -1;
    }
    if (bottom == top) {
        return -1;
    }
    if (near == far) {
        return -1;
    }

    float r_width = 1.0f / (right - left);
    float r_height = 1.0f / (top - bottom);
    float r_depth = 1.0f / (far - near);
    float x = 2.0f * (r_width);
    float y = 2.0f * (r_height);
    float z = -2.0f * (r_depth);
    float tx = -(right + left) * r_width;
    float ty = -(top + bottom) * r_height;
    float tz = -(far + near) * r_depth;
    result->m[mOffset + 0] = x;
    result->m[mOffset + 5] = y;
    result->m[mOffset + 10] = z;
    result->m[mOffset + 12] = tx;
    result->m[mOffset + 13] = ty;
    result->m[mOffset + 14] = tz;
    result->m[mOffset + 15] = 1.0f;
    result->m[mOffset + 1] = 0.0f;
    result->m[mOffset + 2] = 0.0f;
    result->m[mOffset + 3] = 0.0f;
    result->m[mOffset + 4] = 0.0f;
    result->m[mOffset + 6] = 0.0f;
    result->m[mOffset + 7] = 0.0f;
    result->m[mOffset + 8] = 0.0f;
    result->m[mOffset + 9] = 0.0f;
    result->m[mOffset + 11] = 0.0f;

    return 0;
}


/**
 * 视锥体
 * @param result
 * @param left
 * @param top
 * @param right
 * @param bottom
 * @param nearz
 * @param farz
 */
int frustumM(ESMatrix *result, int offset, float left, float right,
             float bottom, float top, float near, float far) {
    if (result == NULL) {
        return -1;
    }
    if (left == right) {
        return -1;
    }
    if (top == bottom) {
        return -1;
    }
    if (near == far) {
        return -1;
    }
    if (near <= 0.0f) {
        return -1;
    }
    if (far <= 0.0f) {
        return -1;
    }

    float r_width = 1.0f / (right - left);
    float r_height = 1.0f / (top - bottom);
    float r_depth = 1.0f / (near - far);
    float x = 2.0f * (near * r_width);
    float y = 2.0f * (near * r_height);
    float A = (right + left) * r_width;
    float B = (top + bottom) * r_height;
    float C = (far + near) * r_depth;
    float D = 2.0f * (far * near * r_depth);
    result->m[offset + 0] = x;
    result->m[offset + 5] = y;
    result->m[offset + 8] = A;
    result->m[offset + 9] = B;
    result->m[offset + 10] = C;
    result->m[offset + 14] = D;
    result->m[offset + 11] = -1.0f;
    result->m[offset + 1] = 0.0f;
    result->m[offset + 2] = 0.0f;
    result->m[offset + 3] = 0.0f;
    result->m[offset + 4] = 0.0f;
    result->m[offset + 6] = 0.0f;
    result->m[offset + 7] = 0.0f;
    result->m[offset + 12] = 0.0f;
    result->m[offset + 13] = 0.0f;
    result->m[offset + 15] = 0.0f;

    return 0;
}

/**
 * 透视矩阵
 * @param result
 * @param offset
 * @param fovy
 * @param aspect
 * @param zNear
 * @param zFar
 * @return
 */
int perspectiveM(ESMatrix *result, int offset,
                 float fovy, float aspect, float zNear, float zFar) {
    if (result == NULL) {
        return -1;
    }

    float f = 1.0f / tanf((float) (fovy * (PI / 360.0)));
    float rangeReciprocal = 1.0f / (zNear - zFar);

    result->m[offset + 0] = f / aspect;
    result->m[offset + 1] = 0.0f;
    result->m[offset + 2] = 0.0f;
    result->m[offset + 3] = 0.0f;

    result->m[offset + 4] = 0.0f;
    result->m[offset + 5] = f;
    result->m[offset + 6] = 0.0f;
    result->m[offset + 7] = 0.0f;

    result->m[offset + 8] = 0.0f;
    result->m[offset + 9] = 0.0f;
    result->m[offset + 10] = (zFar + zNear) * rangeReciprocal;
    result->m[offset + 11] = -1.0f;

    result->m[offset + 12] = 0.0f;
    result->m[offset + 13] = 0.0f;
    result->m[offset + 14] = 2.0f * zFar * zNear * rangeReciprocal;
    result->m[offset + 15] = 0.0f;

    return 0;
}

/**
 * 设置视图矩阵
 * @param result
 * @param rmOffset
 * @param eyeX
 * @param eyeY
 * @param eyeZ
 * @param centerX
 * @param centerY
 * @param centerZ
 * @param upX
 * @param upY
 * @param upZ
 * @return
 */
int setLookAtM(ESMatrix *result, int rmOffset,
               float eyeX, float eyeY, float eyeZ,
               float centerX, float centerY, float centerZ, float upX, float upY,
               float upZ) {

    if (result == NULL) {
        return -1;
    }

    float fx = centerX - eyeX;
    float fy = centerY - eyeY;
    float fz = centerZ - eyeZ;

    // Normalize f
    float rlf = 1.0f / length(fx, fy, fz);
    fx *= rlf;
    fy *= rlf;
    fz *= rlf;

    // compute s = f x up (x means "cross product")
    float sx = fy * upZ - fz * upY;
    float sy = fz * upX - fx * upZ;
    float sz = fx * upY - fy * upX;

    // and normalize s
    float rls = 1.0f / length(sx, sy, sz);
    sx *= rls;
    sy *= rls;
    sz *= rls;

    // compute u = s x f
    float ux = sy * fz - sz * fy;
    float uy = sz * fx - sx * fz;
    float uz = sx * fy - sy * fx;


    result->m[rmOffset + 0] = sx;
    result->m[rmOffset + 1] = ux;
    result->m[rmOffset + 2] = -fx;
    result->m[rmOffset + 3] = 0.0f;

    result->m[rmOffset + 4] = sy;
    result->m[rmOffset + 5] = uy;
    result->m[rmOffset + 6] = -fy;
    result->m[rmOffset + 7] = 0.0f;

    result->m[rmOffset + 8] = sz;
    result->m[rmOffset + 9] = uz;
    result->m[rmOffset + 10] = -fz;
    result->m[rmOffset + 11] = 0.0f;

    result->m[rmOffset + 12] = 0.0f;
    result->m[rmOffset + 13] = 0.0f;
    result->m[rmOffset + 14] = 0.0f;
    result->m[rmOffset + 15] = 1.0f;

    translateM(result, rmOffset, -eyeX, -eyeY, -eyeZ);

    return 0;
}

/**
 * 产生一个单位矩阵
 * @param result
 */
void setIdentityM(ESMatrix *result) {
    if (result == NULL) {
        result = (ESMatrix *) malloc(sizeof(ESMatrix));
    }
    memset(result, 0x0, sizeof(ESMatrix));
    for (int i = 0; i < 16; i += 5) {
        result->m[i] = 1.0f;
    }
}

#define I(_i, _j) ((_j)+ 4*(_i))

/**
 * 矩阵相乘
 * @param result
 * @param lhs
 * @param rhs
 */
void multiplyMM(ESMatrix *result, ESMatrix *lhs, ESMatrix *rhs) {

    for (int i = 0; i < 4; i++) {
        register const float rhs_i0 = rhs->m[I(i, 0)];
        register float ri0 = lhs->m[I(0, 0)] * rhs_i0;
        register float ri1 = lhs->m[I(0, 1)] * rhs_i0;
        register float ri2 = lhs->m[I(0, 2)] * rhs_i0;
        register float ri3 = lhs->m[I(0, 3)] * rhs_i0;
        for (int j = 1; j < 4; j++) {
            register const float rhs_ij = rhs->m[I(i, j)];
            ri0 += lhs->m[I(j, 0)] * rhs_ij;
            ri1 += lhs->m[I(j, 1)] * rhs_ij;
            ri2 += lhs->m[I(j, 2)] * rhs_ij;
            ri3 += lhs->m[I(j, 3)] * rhs_ij;
        }
        result->m[I(i, 0)] = ri0;
        result->m[I(i, 1)] = ri1;
        result->m[I(i, 2)] = ri2;
        result->m[I(i, 3)] = ri3;
    }

}

#ifdef __cplusplus
}
#endif