//
// Created by Administrator on 2018/3/21.
//

#ifndef CAINCAMERA_YUV420PIMAGEFILTER_H
#define CAINCAMERA_YUV420PIMAGEFILTER_H

#include "caingles/GlUtils.h"

class YUV420PImageFilter {

public:
    YUV420PImageFilter();
    YUV420PImageFilter(const char *vertexShader, const char *fragmentShader);
    virtual ~YUV420PImageFilter();
    // 输入大小发生变化
    void onInputSizeChanged(int width, int height);
    // 界面大小发生变化
    void onDisplayChanged(int width, int height);
    // 绘制视频帧
    bool drawFrame(void *bufY, void *bufU, void *bufV);
    // 绘制视频帧
    bool drawFrame(void *bufY, void *bufU, void *bufV, GLfloat vertices[], GLfloat textureCoords[]);
    // 将视频帧绘制到FBO
    int drawFrameBuffer(void *bufY, void *bufU, void *bufV);
    // 将视频帧绘制到FBO
    int drawFrameBuffer(void *bufY, void *bufU, void *bufV, GLfloat vertices[], GLfloat textureCoords[]);
    // 设置单位矩阵
    void initIdentityMatrix();
    // 设置总变换
    void setMVPMatrix(ESMatrix *matrix);
    // 初始化FBO
    void initFrameBuffer(int width, int height);
    // 销毁FBO
    void destroyFrameBuffer();

private:
    const char *getVertexShader(void);
    const char *getFragmentShader(void);
    // 初始化句柄
    void initHandle(void);
    // 句柄
    void viewport();
    // 初始化Texture
    void initTexture(int width, int height);
    // 释放资源
    void release(void);
    // 初始化坐标
    void initCoordinates();
    // 顶点坐标
    GLfloat vertexCoordinates[8] = {0};
    // 纹理坐标
    GLfloat textureCoordinates[8] = {0};
    // 句柄
    GLint programHandle;
    GLint mvpMatrixHandle;
    GLint positionHandle;
    GLint textureCoordsHandle;
    GLint inputTextureHandle[3] = {0};

    // 矩阵对象
    ESMatrix *mvpMatrix;
    // yuv的Texture
    GLuint mTextureId[3];

    // 输入宽度
    int videoWidth;
    // 输入高度
    int videoHeight;
    // Surface的宽度
    int screenWidth;
    // Surface的高度
    int screenHeight;
    // 实际显示的宽度
    int viewWidth;
    // 实际显示的高度
    int viewHeight;

    // FBO
    GLuint mFrameBuffers[1];
    GLuint mFrameBufferTextures[1];
    int mFrameWidth;
    int mFrameHeight;
};


#endif //CAINCAMERA_YUV420PIMAGEFILTER_H
