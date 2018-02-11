//
// Created by cain on 2018/2/11.
//

#ifndef CAINCAMERA_BASEIMAGEFILTER_H
#define CAINCAMERA_BASEIMAGEFILTER_H

#include "caingles/GlUtils.h"

// Texture类型，输入的视频数据是yuv还是RGB类型
typedef enum TextureType {
    TYPE_YUV,
    TYPE_RGB,
    TYPE_RGBA,
    TYPE_NONE
} TextureType;

class BaseImageFilter {
private:
    // 输入宽度
    int textureWidth;
    // 输入高度
    int textureHeight;
    // 显示宽度
    int displayWidth;
    // 显示高度
    int displayHeight;
    TextureType type;

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

    // 获取VertexShader
    const char *getVertexShader(void);
    // 获取FragmentShader
    const char *getFragmentShader(void);
    // 初始化句柄
    void initHandle(void);
    // 绑定数据
    void bindValue(GLint texture, GLfloat vertices[], GLfloat textureCoords[]);
    // 解除绑定
    void unbindValue(void);
    // 获取Texture类型
    TextureType getTextureType();
    // 实际绘制之前
    virtual void onDrawBegin(void);
    // 绘制之后
    virtual void onDrawAfter(void);
    // 释放资源
    virtual void release(void);
    // 初始化坐标
    void initCoordinates();

public:
    BaseImageFilter(void);
    BaseImageFilter(const char *vertexShader, const char *fragmentShader);
    ~BaseImageFilter();
    // 输入大小发生变化
    virtual void onInputSizeChanged(int width, int height);
    // 界面大小发生变化
    virtual void onDisplayChanged(int width, int height);
    // 绘制视频帧
    virtual bool drawFrame(int texture);
    // 绘制视频帧
    virtual bool drawFrame(int texture, GLfloat vertices[], GLfloat textureCoords[]);
    // 设置单位矩阵
    void initIdentityMatrix();
    // 设置总变换
    void setMVPMatrix(ESMatrix *matrix);
    // 设置Texture的类型
    void setTextureType(TextureType type);

};


#endif //CAINCAMERA_BASEIMAGEFILTER_H

