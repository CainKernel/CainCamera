//
// Created by cain on 2018/5/6.
//

#ifndef CAINPLAYER_GLIMAGEINPUTFILTER_H
#define CAINPLAYER_GLIMAGEINPUTFILTER_H
#ifdef __cplusplus
extern "C" {
#endif

#include "../caingles/GlUtils.h"
#include <libavutil/frame.h>

#ifdef __cplusplus
};
#endif

class GLImageInputFilter {
public:
    GLImageInputFilter();

    virtual ~GLImageInputFilter();

    // 初始化句柄
    virtual int initHandle(void);

    // 初始化Texture
    virtual void initTexture();

    // 输入大小变化
    virtual void onInputSizeChanged(int width, int height);

    // 视频大小
    virtual void onSurfaceChanged(int width, int height);

    // 绘制YUV图像数据
    virtual bool drawFrame(AVFrame *yuvFrame);

    // 将YUV图像数据绘制到FBO
    virtual int drawFrameBuffer(AVFrame *yuvFrame);

    // 初始化FBO
    virtual void initFrameBuffer(int width, int height);

    // 销毁FBO
    virtual void destroyFrameBuffer();

    // 释放资源
    virtual void release(void);

protected:

    // 初始化坐标
    virtual void initCoordinates();

    // 顶点坐标
    GLfloat *vertexCoordinates;

    // 纹理坐标
    GLfloat *textureCoordinates;

    // 句柄
    GLint programHandle;
    GLint positionHandle;
    GLint textureCoordsHandle;

    // 输入宽度
    int videoWidth;
    // 输入高度
    int videoHeight;
    // Surface的宽度
    int surfacWidth;
    // Surface的高度
    int surfaceHeight;
    // 起始位置
    int left, top;
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


#endif //CAINPLAYER_GLIMAGEINPUTFILTER_H
