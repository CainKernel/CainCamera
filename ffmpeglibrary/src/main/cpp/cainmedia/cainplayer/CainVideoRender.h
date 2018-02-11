//
// Created by Administrator on 2018/2/7.
//

#ifndef CAINCAMERA_CAINVIDEORENDER_H
#define CAINCAMERA_CAINVIDEORENDER_H

#include "caingles/GlUtils.h"

// Texture类型，输入的视频数据是yuv还是RGB类型
typedef enum TextureType {
    TYPE_YUV,
    TYPE_RGB,
    TYPE_RGBA,
    TYPE_NONE
} TextureType;

// 视频渲染器
class CainVideoRender {
private:
    // 视频宽度
    int videoWidth;
    // 视频高度
    int videoHeight;
    // 显示宽度
    int displayWidth;
    // 显示高度
    int displayHeight;
    // Texture类型
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

protected:
    // 获取VertexShader
    static const char *getVertexShader(void);
    // 获取FragmentShader
    static const char *getFragmentShader(void);
    // 初始化句柄
    void initHandle(void);
    // 绑定数据
    void bindValue(GLint texture, GLfloat vertices[], GLfloat textureCoords[]);
    // 解除绑定
    void unbindValue(void);
    // 获取Texture类型
    TextureType getTextureType();
    // 实际绘制之前
    void onDrawBegin(void);
    // 绘制之后
    void onDrawAfter(void);
    // 释放资源
    void release(void);
    // 初始化坐标
    void initCoordinates();

public:
    CainVideoRender(void);
    CainVideoRender(const char *vertexShader, const char *fragmentShader);
    ~CainVideoRender();
    // 输入大小发生改变
    void onInputSizeChanged(int width, int height);
    // 显示大小发生改变
    void onDisplaySizeChanged(int width, int height);
    // 渲染视频帧
    bool drawFrame(int textureId);
    // 渲染视频帧
    bool drawFrame(int textureId, GLfloat verticex[], GLfloat textureCoords[]);
    // 初始化单位矩阵
    void initIdentityMatrix();
    // 设置总变换矩阵
    void setMVPMatrix(ESMatrix *matrix);
    // 设置Texture类型
    void setTextureType(TextureType type);
};


#endif //CAINCAMERA_CAINVIDEORENDER_H
