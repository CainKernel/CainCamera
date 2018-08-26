//
// Created by cain on 2018/2/11.
//

#ifndef CAINPLAYER_GLIMAGEFILTER_H
#define CAINPLAYER_GLIMAGEFILTER_H

#include "../caingles/GlUtils.h"
#include "../caingles/GlShaders.h"
#include "AndroidLog.h"

class GLImageFilter {
public:
    GLImageFilter(void);
    GLImageFilter(const char *vertexShader, const char *fragmentShader);
    virtual ~GLImageFilter();

    // 输入大小发生变化
    virtual void onInputSizeChanged(int width, int height);

    // 界面大小发生变化
    virtual void onDisplaySizeChanged(int width, int height);

    // 绘制视频帧
    virtual bool drawFrame(int texture);

    // 绘制视频帧
    virtual bool drawFrame(int texture, GLfloat vertices[], GLfloat textureCoords[]);

    // 设置单位矩阵
    void initIdentityMatrix();

    // 设置总变换
    void setMVPMatrix(ESMatrix *matrix);

    // 释放资源
    virtual void release(void);

protected:

    // 获取VertexShader
    virtual const char *getVertexShader(void);

    // 获取FragmentShader
    virtual const char *getFragmentShader(void);

    // 初始化句柄
    virtual void initHandle(void);

    // 绑定数据
    virtual void bindValue(GLint texture, GLfloat vertices[], GLfloat textureCoords[]);

    // 解除绑定
    virtual void unbindValue(void);

    // 实际绘制之前
    virtual void onDrawBegin(void);

    // 绘制之后
    virtual void onDrawAfter(void);

    // 初始化坐标
    void initCoordinates();

protected:
    // 顶点坐标
    GLfloat vertexCoordinates[8] = {0};
    // 纹理坐标
    GLfloat textureCoordinates[8] = {0};
    // 句柄
    GLint programHandle;
    GLint mvpMatrixHandle;
    GLint positionHandle;
    GLint textureCoordsHandle;
    GLint inputTextureHandle;
    ESMatrix *mvpMatrix;
    // 输入宽度
    int textureWidth;
    // 输入高度
    int textureHeight;
    // 显示宽度
    int displayWidth;
    // 显示高度
    int displayHeight;

};


#endif //CAINPLAYER_GLIMAGEFILTER_H

