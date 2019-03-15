//
// Created by CainHuang on 2019/3/13.
//

#ifndef GLFILTER_H
#define GLFILTER_H

#if defined(__ANDROID__)

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <GLES2/gl2platform.h>

#endif

#include <string>
#include "macros.h"

/**
 * 默认的 vertex shader
 */
const std::string kDefaultVertexShader = SHADER_TO_STRING(
        precision mediump float;
        attribute highp vec4 aPosition;
        attribute highp vec2 aTextureCoord;
        varying vec2 textureCoordinate;
        void main() {
            gl_Position  = aPosition;
            textureCoordinate = aTextureCoord.xy;
        }
);

/**
 * 默认的 fragment shader
 */
const std::string kDefaultFragmentShader = SHADER_TO_STRING(
        precision mediump float;
        uniform sampler2D inputTexture;
        varying vec2 textureCoordinate;

        void main() {
            gl_FragColor = texture2D(inputTexture, textureCoordinate.xy);
        }
);


/**
 * 滤镜基类
 */
class GLFilter {
public:
    GLFilter();

    virtual ~GLFilter();

    // 初始化program
    virtual void initProgram();

    // 初始化program
    virtual void initProgram(const char *vertexShader, const char *fragmentShader);

    // 销毁program
    virtual void destroyProgram();

    // 绘制纹理
    virtual void drawTexture(GLuint texture, float *vertices, float *textureVertices);

    // 设置是否初始化
    void setInitialized(bool initialized);

    // 是否已经初始化
    bool isInitialized() const;

    // 设置时间戳
    virtual void setTimeStamp(double timeStamp);

    // 设置强度
    void setIntensity(float intensity);

protected:

    // 绑定attribute属性
    virtual void bindAttributes(float *vertices, float *textureVertices);

    // 绑定纹理
    virtual void bindTexture(GLuint texture);

    // 绘制之前处理
    virtual void onDrawBegin();

    // 绘制之后处理
    virtual void onDrawAfter();

    // 绘制方法
    virtual void onDrawFrame();

    // 解绑attribute属性
    virtual void unbindAttributes();

    //
    virtual void unbindTextures();

    // 绑定的纹理类型，默认为GL_TEXTURE_2D
    virtual GLenum getTextureType();

protected:
    bool initialized;       // shader program 初始化标志
    int programHandle;      // 程序句柄
    int positionHandle;     // 顶点坐标句柄
    int texCoordHandle;     // 纹理坐标句柄
    int inputTextureHandle; // 纹理句柄
    int vertexCount = 4;    // 绘制的顶点个数，默认为4
    double timeStamp;        // 时间戳
    float intensity;        // 强度 0.0 ~ 1.0，默认为1.0
};


#endif //GLFILTER_H
