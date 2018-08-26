//
// Created by cain on 2018/5/6.
//

#ifndef CAINPLAYER_YUV420PIMAGEINPUTFILTER_H
#define CAINPLAYER_YUV420PIMAGEINPUTFILTER_H


#include <GLImageInputFilter.h>

class YUV420PImageInputFilter : public GLImageInputFilter {
public:
    YUV420PImageInputFilter();

    virtual ~YUV420PImageInputFilter();

    int initHandle(void) override;

    void initTexture() override;

    void onInputSizeChanged(int width, int height) override;

    void onSurfaceChanged(int width, int height) override;

    bool drawFrame(AVFrame *yuvFrame) override;

    int drawFrameBuffer(AVFrame *yuvFrame) override;

    void initFrameBuffer(int width, int height) override;

    void destroyFrameBuffer() override;

    void release(void) override;

protected:
    void initCoordinates() override;

    GLint yTextureHandle;
    GLint uTextureHandle;
    GLint vTextureHandle;

    GLuint yTextureId;
    GLuint uTextureId;
    GLuint vTextureId;
};


#endif //CAINPLAYER_YUV420PIMAGEINPUTFILTER_H
