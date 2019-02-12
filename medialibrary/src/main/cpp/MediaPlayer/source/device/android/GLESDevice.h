//
// Created by cain on 2018/12/30.
//

#ifndef GLESDEVICE_H
#define GLESDEVICE_H

#include <device/VideoDevice.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>

#include <renderer/GLUtils.h>
#include <renderer/EglHelper.h>

class GLESDevice : public VideoDevice {
public:
    GLESDevice();

    virtual ~GLESDevice();

    void surfaceCreated(ANativeWindow *window);

    void surfaceChanged(int width, int height);

    void surfaceDestroyed();

    void terminate() override;

    void terminate(bool releaseContext);

    void onInitTexture(int width, int height, TextureFormat format, BlendMode blendMode) override;

    int onUpdateYUV(uint8_t *yData, int yPitch, uint8_t *uData, int uPitch,
                    uint8_t *vData, int vPitch) override;

    int onUpdateARGB(uint8_t *rgba, int pitch) override;

    int onRequestRender(FlipDirection direction) override;


private:
    Mutex mMutex;
    Condition mCondition;

    ANativeWindow *mWindow;             // Surface窗口
    EGLSurface eglSurface;              // eglSurface
    EglHelper *eglHelper;               // EGL帮助器
    bool mSurfaceReset;                 // 重新设置Surface
    bool mHasSurface;                   // 是否存在Surface
    bool mHaveEGLSurface;               // EGLSurface
    bool mHaveEGlContext;               // 释放资源

    Texture *mVideoTexture;             // 视频纹理
    Renderer *mRenderer;                // 渲染器
};

#endif //GLESDEVICE_H
