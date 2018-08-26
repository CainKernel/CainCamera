//
// Created by cain on 2018/4/30.
//

#ifndef CAINPLAYER_AVVIDEOOUTPUT_H
#define CAINPLAYER_AVVIDEOOUTPUT_H


#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <GLES2/gl2platform.h>
#include <assert.h>
#include <android/native_window.h>
#include <EglCore.h>
#include <WindowSurface.h>
#include <GLImageFilter.h>
#include <Handler.h>
#include <GLImageInputFilter.h>
#include <input/YUV420PImageInputFilter.h>
#include "../decoder/AVDecoder.h"

#ifdef __cplusplus
extern "C" {
#endif

#include <libavutil/frame.h>
#include <libswscale/swscale.h>
#include <libavutil/imgutils.h>

#ifdef __cplusplus
};
#endif

class AVVideoOutput {
public:
    AVVideoOutput(AVDecoder *decoder);

    virtual ~AVVideoOutput();

    // 创建Egl
    void surfaceCreated(ANativeWindow *window);

    // 窗口大小改变
    void surfaceChanged(int width, int height);

    // 销毁窗口
    void surfaceDestroyed(void);

    // 显示画面
    void displayVideo(AVFrame *frame);

private:
    // 重新创建Texture
    int reallocTexture(AVFrame *frame);

    // 渲染视频
    void renderFrame(AVFrame *frame);

    EglCore *mEglCore;                  // EGL封装类
    WindowSurface *mWindowSurface;      // EGL窗口
    GLImageInputFilter *mInputFilter;   // 输入Texture

    int mFormat;                        // 视频格式

    AVFrame *mFrameRGBA;                // 用于转码上下文
    struct SwsContext *img_convert_ctx; // 转码上下文
    uint8_t *mBuffer;                   // 缓冲区
    AVDecoder *mDecoder;                // 视频解码器
};


#endif //CAINPLAYER_AVVIDEOOUTPUT_H
