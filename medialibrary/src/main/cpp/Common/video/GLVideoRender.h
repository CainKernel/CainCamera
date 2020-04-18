//
// Created by CainHuang on 2020-04-18.
//

#ifndef GLVIDEORENDER_H
#define GLVIDEORENDER_H

#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <node/DisplayRenderNode.h>
#include <node/InputRenderNode.h>
#include <node/RenderNodeList.h>
#include <Filter.h>
#include <Condition.h>

class GLVideoRender {
public:
    GLVideoRender();

    virtual ~GLVideoRender();

    // 更新Surface
    void surfaceCreated(ANativeWindow *window);

    // 更新显示宽高
    void surfaceChanged(int width, int height);

    // 结束
    void terminate(bool releaseContext);

    // 设置时间戳
    void setTimeStamp(double timeStamp);

    // 初始化纹理
    void initTexture(int width, int height, int rotate);

    // 上载纹理
    int uploadData(uint8_t *yData, int yPitch, uint8_t *uData, int uPitch,
                    uint8_t *vData, int vPitch);

    // 渲染一帧帧数据
    int renderFrame();

    // 切换滤镜
    void changeFilter(RenderNodeType type, const char *filterName);

    // 切换滤镜
    void changeFilter(RenderNodeType type, const int id);

private:
    void resetVertices();

    void resetTexVertices();

private:
    ANativeWindow *mWindow;             // Surface窗口
    int mSurfaceWidth;                  // 窗口宽度
    int mSurfaceHeight;                 // 窗口高度
    EGLSurface eglSurface;              // eglSurface
    EglHelper *eglHelper;               // EGL帮助器
    bool mSurfaceReset;                 // 重新设置Surface
    bool mHasSurface;                   // 是否存在Surface
    bool mHaveEGLSurface;               // EGLSurface
    bool mHaveEGlContext;               // 释放资源

    Texture *mVideoTexture;             // 视频纹理
    InputRenderNode *mRenderNode;       // 输入渲染结点
    RenderNodeList *nodeList;           // 滤镜链
    FilterInfo filterInfo;              // 滤镜信息
    bool filterChange;                  // 切换滤镜
    GLfloat vertices[8];                // 顶点坐标
    GLfloat textureVertices[8];         // 纹理坐标
};


#endif //GLVIDEORENDER_H
